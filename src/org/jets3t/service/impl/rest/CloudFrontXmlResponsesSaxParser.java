/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 *
 * Copyright 2008 James Murty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service.impl.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.cloudfront.Distribution;
import org.jets3t.service.model.cloudfront.DistributionConfig;
import org.jets3t.service.model.cloudfront.LoggingStatus;
import org.jets3t.service.model.cloudfront.OriginAccessIdentity;
import org.jets3t.service.model.cloudfront.OriginAccessIdentityConfig;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;
import org.jets3t.service.utils.ServiceUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * XML Sax parser to read XML documents returned by the CloudFront service via
 * the REST interface, and convert these documents into JetS3t objects.
 *
 * @author James Murty
 */
public class CloudFrontXmlResponsesSaxParser {
    private static final Log log = LogFactory.getLog(CloudFrontXmlResponsesSaxParser.class);

    private XMLReader xr = null;
    private Jets3tProperties properties = null;

    /**
     * Constructs the XML SAX parser.
     *
     * @param properties
     * the JetS3t properties that will be applied when parsing XML documents.
     *
     * @throws S3ServiceException
     */
    public CloudFrontXmlResponsesSaxParser(Jets3tProperties properties) throws S3ServiceException {
        this.properties = properties;

        // Ensure we can load the XML Reader.
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            // oops, lets try doing this (needed in 1.4)
            System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");
            try {
                // Try once more...
                xr = XMLReaderFactory.createXMLReader();
            } catch (SAXException e2) {
                throw new S3ServiceException("Couldn't initialize a sax driver for the XMLReader");
            }
        }
    }

    /**
     * Parses an XML document from an input stream using a document handler.
     * @param handler
     *        the handler for the XML document
     * @param inputStream
     *        an input stream containing the XML document to parse
     * @throws S3ServiceException
     *        any parsing, IO or other exceptions are wrapped in an S3ServiceException.
     */
    protected void parseXmlInputStream(DefaultHandler handler, InputStream inputStream)
        throws CloudFrontServiceException
    {
        try {
            if (log.isDebugEnabled()) {
            	log.debug("Parsing XML response document with handler: " + handler.getClass());
            }
            BufferedReader breader = new BufferedReader(new InputStreamReader(inputStream,
                Constants.DEFAULT_ENCODING));
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(breader));
        } catch (Throwable t) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                	log.error("Unable to close response InputStream up after XML parse failure", e);
                }
            }
            throw new CloudFrontServiceException("Failed to parse XML document with handler "
                + handler.getClass(), t);
        }
    }

    /**
     * Parses a ListBucket response XML document from an input stream.
     * @param inputStream
     * XML data input stream.
     * @return
     * the XML handler object populated with data parsed from the XML stream.
     * @throws S3ServiceException
     */
    public ListDistributionListHandler parseDistributionListResponse(InputStream inputStream)
        throws CloudFrontServiceException
    {
        ListDistributionListHandler handler = new ListDistributionListHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public DistributionHandler parseDistributionResponse(InputStream inputStream)
        throws CloudFrontServiceException
    {
        DistributionHandler handler = new DistributionHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public DistributionConfigHandler parseDistributionConfigResponse(InputStream inputStream)
        throws CloudFrontServiceException
    {
        DistributionConfigHandler handler = new DistributionConfigHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public OriginAccessIdentityHandler parseOriginAccessIdentity(
    	InputStream inputStream) throws CloudFrontServiceException
    {
        OriginAccessIdentityHandler handler = new OriginAccessIdentityHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public OriginAccessIdentityConfigHandler parseOriginAccessIdentityConfig(
    	InputStream inputStream) throws CloudFrontServiceException
    {
        OriginAccessIdentityConfigHandler handler = new OriginAccessIdentityConfigHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    public OriginAccessIdentityListHandler parseOriginAccessIdentityListResponse(
    	InputStream inputStream) throws CloudFrontServiceException
    {
        OriginAccessIdentityListHandler handler = new OriginAccessIdentityListHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }


    public ErrorHandler parseErrorResponse(InputStream inputStream)
        throws CloudFrontServiceException
    {
        ErrorHandler handler = new ErrorHandler();
        parseXmlInputStream(handler, inputStream);
        return handler;
    }

    // ////////////
    // Handlers //
    // ////////////

    public class SimpleHandler extends DefaultHandler {
        private StringBuffer textContent = null;
        protected SimpleHandler currentHandler = null;
        protected SimpleHandler parentHandler = null;

        public SimpleHandler() {
            this.textContent = new StringBuffer();
            currentHandler = this;
        }

        public void transferControlToHandler(SimpleHandler toHandler) {
            currentHandler = toHandler;
            toHandler.parentHandler = this;
        }

        public void returnControlToParentHandler() {
            if (isChildHandler()) {
                parentHandler.currentHandler = parentHandler;
                parentHandler.controlReturned(this);
            } else {
                log.debug("Ignoring call to return control to parent handler, as this class has no parent: " +
                    this.getClass().getName());
            }
        }

        public boolean isChildHandler() {
            return parentHandler != null;
        }

        public void controlReturned(SimpleHandler childHandler) {}

        public void startElement(String uri, String name, String qName, Attributes attrs) {
            try {
                Method method = currentHandler.getClass().getMethod("start" + name, new Class[] {});
                method.invoke(currentHandler, new Object[] {});
            } catch (NoSuchMethodException e) {
                log.debug("Skipped non-existent SimpleHandler subclass's startElement method for '" + name + "' in " + this.getClass().getName());
            } catch (Throwable t) {
                log.error("Unable to invoke SimpleHandler subclass's startElement method for '" + name + "' in " + this.getClass().getName(), t);
            }
        }

        public void endElement(String uri, String name, String qName) {
            String elementText = this.textContent.toString().trim();
            try {
                Method method = currentHandler.getClass().getMethod("end" + name, new Class[] {String.class});
                method.invoke(currentHandler, new Object[] {elementText});
            } catch (NoSuchMethodException e) {
                log.debug("Skipped non-existent SimpleHandler subclass's endElement method for '" + name + "' in " + this.getClass().getName());
            } catch (Throwable t) {
                log.error("Unable to invoke SimpleHandler subclass's endElement method for '" + name + "' in " + this.getClass().getName(), t);
            }
            this.textContent = new StringBuffer();
        }

        public void characters(char ch[], int start, int length) {
            this.textContent.append(ch, start, length);
        }


    }

    public class DistributionHandler extends SimpleHandler {
        private Distribution distribution = null;

        private String id = null;
        private String status = null;
        private Date lastModifiedTime = null;
        private String domainName = null;
        private Map activeTrustedSigners = new HashMap();

        private boolean inSignerElement;
        private String lastSignerIdentifier = null;

        public Distribution getDistribution() {
            return distribution;
        }

        public void endId(String text) {
            this.id = text;
        }

        public void endStatus(String text) {
            this.status = text;
        }

        public void endLastModifiedTime(String text) throws ParseException {
            this.lastModifiedTime = ServiceUtils.parseIso8601Date(text);
        }

        public void endDomainName(String text) {
            this.domainName = text;
        }

        // Handle ActiveTrustedSigner elements //
        public void startSigner() {
            inSignerElement = true;
        }

        public void endSigner(String text) {
            inSignerElement = false;
            lastSignerIdentifier = null;
        }

        public void endSelf(String text) {
            if (inSignerElement) {
            	lastSignerIdentifier = "Self";
            }
        }

        public void endAwsAccountNumber(String text) {
            if (inSignerElement) {
            	lastSignerIdentifier = text;
            }
        }

        public void endKeyPairId(String text) {
            if (inSignerElement) {
            	List keypairIdList = (List) activeTrustedSigners.get(lastSignerIdentifier);
            	if (keypairIdList == null) {
            		keypairIdList = new ArrayList();
                	activeTrustedSigners.put(lastSignerIdentifier, keypairIdList);
            	}
            	keypairIdList.add(text);
            }
        }
        // End handle ActiveTrustedSigner elements //

        public void startDistributionConfig() {
            transferControlToHandler(new DistributionConfigHandler());
        }

        public void startStreamingDistributionConfig() {
            transferControlToHandler(new DistributionConfigHandler());
        }

        public void controlReturned(SimpleHandler childHandler) {
            DistributionConfig config =
                ((DistributionConfigHandler) childHandler).getDistributionConfig();
            if (config instanceof StreamingDistributionConfig) {
                this.distribution = new StreamingDistribution(id, status,
                    lastModifiedTime, domainName, config);
            } else {
                this.distribution = new Distribution(id, status,
                    lastModifiedTime, domainName, activeTrustedSigners, config);
            }
        }

        // End of a normal Distribution
        public void endDistribution(String text) {
            returnControlToParentHandler();
        }

        // End of a StreamingDistribution
        public void endStreamingDistribution(String text) {
            returnControlToParentHandler();
        }
    }

    public class DistributionConfigHandler extends SimpleHandler {
        private DistributionConfig distributionConfig = null;

        private String origin = "";
        private String callerReference = "";
        private List cnamesList = new ArrayList();
        private String comment = "";
        private boolean enabled = false;
        private LoggingStatus loggingStatus = null;
        private String originAccessIdentity = null;
        private boolean trustedSignerSelf = false;
        private List trustedSignerAwsAccountNumberList = new ArrayList();

        public DistributionConfig getDistributionConfig() {
            return distributionConfig;
        }

        public void endOrigin(String text) {
            this.origin = text;
        }

        public void endCallerReference(String text) {
            this.callerReference = text;
        }

        public void endCNAME(String text) {
            this.cnamesList.add(text);
        }

        public void endComment(String text) {
            this.comment = text;
        }

        public void endEnabled(String text) {
            this.enabled = "true".equalsIgnoreCase(text);
        }

        public void startLogging() {
            this.loggingStatus = new LoggingStatus();
        }

        public void endBucket(String text) {
            this.loggingStatus.setBucket(text);
        }

        public void endPrefix(String text) {
            this.loggingStatus.setPrefix(text);
        }

        public void endOriginAccessIdentity(String text) {
            this.originAccessIdentity = text;
        }

        public void endSelf(String text) {
            this.trustedSignerSelf = true;
        }

        public void endAwsAccountNumber(String text) {
            this.trustedSignerAwsAccountNumberList.add(text);
        }

        public void endDistributionConfig(String text) {
            this.distributionConfig = new DistributionConfig(
                origin, callerReference,
                (String[]) cnamesList.toArray(new String[cnamesList.size()]),
                comment, enabled, loggingStatus, originAccessIdentity, trustedSignerSelf,
                (String[]) trustedSignerAwsAccountNumberList.toArray(
                	new String[trustedSignerAwsAccountNumberList.size()]));
            returnControlToParentHandler();
        }

        public void endStreamingDistributionConfig(String text) {
            this.distributionConfig = new StreamingDistributionConfig(
                origin, callerReference,
                (String[]) cnamesList.toArray(new String[cnamesList.size()]), comment, enabled);
            returnControlToParentHandler();
        }
    }

    public class DistributionSummaryHandler extends SimpleHandler {
        private Distribution distribution = null;

        private String id = null;
        private String status = null;
        private Date lastModifiedTime = null;
        private String domainName = null;
        private String origin = null;
        private List cnamesList = new ArrayList();
        private String comment = null;
        private boolean enabled = false;

        public Distribution getDistribution() {
            return distribution;
        }

        public void endId(String text) {
            this.id = text;
        }

        public void endStatus(String text) {
            this.status = text;
        }

        public void endLastModifiedTime(String text) throws ParseException {
            this.lastModifiedTime = ServiceUtils.parseIso8601Date(text);
        }

        public void endDomainName(String text) {
            this.domainName = text;
        }

        public void endOrigin(String text) {
            this.origin = text;
        }

        public void endCNAME(String text) {
            this.cnamesList.add(text);
        }

        public void endComment(String text) {
            this.comment = text;
        }

        public void endEnabled(String text) {
            this.enabled = "true".equalsIgnoreCase(text);
        }

        public void endDistributionSummary(String text) {
            this.distribution = new Distribution(id, status,
                lastModifiedTime, domainName, origin,
                (String[]) cnamesList.toArray(new String[cnamesList.size()]),
                comment, enabled);
            returnControlToParentHandler();
        }

        public void endStreamingDistributionSummary(String text) {
            this.distribution = new StreamingDistribution(id, status,
                lastModifiedTime, domainName, origin,
                (String[]) cnamesList.toArray(new String[cnamesList.size()]),
                comment, enabled);
            returnControlToParentHandler();
        }
    }

    public class ListDistributionListHandler extends SimpleHandler {
        private List distributions = new ArrayList();
        private List cnamesList = new ArrayList();
        private String marker = null;
        private String nextMarker = null;
        private int maxItems = 100;
        private boolean isTruncated = false;

        public List getDistributions() {
            return distributions;
        }

        public boolean isTruncated() {
            return isTruncated;
        }

        public String getMarker() {
            return marker;
        }

        public String getNextMarker() {
            return nextMarker;
        }

        public int getMaxItems() {
            return maxItems;
        }

        public void startDistributionSummary() {
            transferControlToHandler(new DistributionSummaryHandler());
        }

        public void startStreamingDistributionSummary() {
            transferControlToHandler(new DistributionSummaryHandler());
        }

        public void controlReturned(SimpleHandler childHandler) {
            distributions.add(
                ((DistributionSummaryHandler) childHandler).getDistribution());
        }

        public void endCNAME(String text) {
            this.cnamesList.add(text);
        }

        public void endMarker(String text) {
            this.marker = text;
        }

        public void endNextMarker(String text) {
            this.nextMarker = text;
        }

        public void endMaxItems(String text) {
            this.maxItems = Integer.parseInt(text);
        }

        public void endIsTruncated(String text) {
            this.isTruncated = "true".equalsIgnoreCase(text);
        }
    }

    public class OriginAccessIdentityHandler extends SimpleHandler {
        private String id = null;
        private String s3CanonicalUserId = null;
        private String comment = null;
        private OriginAccessIdentity originAccessIdentity = null;
        private OriginAccessIdentityConfig originAccessIdentityConfig = null;

        public OriginAccessIdentity getOriginAccessIdentity() {
    		return this.originAccessIdentity;
    	}

    	public void endId(String text) {
        	this.id = text;
        }

        public void endS3CanonicalUserId(String text) {
        	this.s3CanonicalUserId = text;
        }

        public void endComment(String text) {
        	this.comment = text;
        }

        public void startCloudFrontOriginAccessIdentityConfig() {
            transferControlToHandler(new OriginAccessIdentityConfigHandler());
        }

        public void controlReturned(SimpleHandler childHandler) {
            this.originAccessIdentityConfig =
                ((OriginAccessIdentityConfigHandler) childHandler).getOriginAccessIdentityConfig();
        }

        public void endCloudFrontOriginAccessIdentity(String text) {
            this.originAccessIdentity = new OriginAccessIdentity(
        		this.id, this.s3CanonicalUserId, this.originAccessIdentityConfig);
        }

        public void endCloudFrontOriginAccessIdentitySummary(String text) {
            this.originAccessIdentity = new OriginAccessIdentity(
            		this.id, this.s3CanonicalUserId, this.comment);
            returnControlToParentHandler();
        }
    }

    public class OriginAccessIdentityConfigHandler extends SimpleHandler {
        private String callerReference = null;
        private String comment = null;
        private OriginAccessIdentityConfig config = null;

        public OriginAccessIdentityConfig getOriginAccessIdentityConfig() {
    		return this.config;
    	}

        public void endCallerReference(String text) {
            this.callerReference = text;
        }

        public void endComment(String text) {
            this.comment = text;
        }

        public void endCloudFrontOriginAccessIdentityConfig(String text) {
            this.config = new OriginAccessIdentityConfig(this.callerReference, this.comment);
            returnControlToParentHandler();
        }
    }

    public class OriginAccessIdentityListHandler extends SimpleHandler {
        private List originAccessIdentityList = new ArrayList();
        private String marker = null;
        private String nextMarker = null;
        private int maxItems = 100;
        private boolean isTruncated = false;

        public List getOriginAccessIdentityList() {
            return this.originAccessIdentityList;
        }

        public boolean isTruncated() {
            return isTruncated;
        }

        public String getMarker() {
            return marker;
        }

        public String getNextMarker() {
            return nextMarker;
        }

        public int getMaxItems() {
            return maxItems;
        }

        public void startCloudFrontOriginAccessIdentitySummary() {
            transferControlToHandler(new OriginAccessIdentityHandler());
        }

        public void controlReturned(SimpleHandler childHandler) {
            originAccessIdentityList.add(
                ((OriginAccessIdentityHandler) childHandler).getOriginAccessIdentity());
        }

        public void endMarker(String text) {
            this.marker = text;
        }

        public void endNextMarker(String text) {
            this.nextMarker = text;
        }

        public void endMaxItems(String text) {
            this.maxItems = Integer.parseInt(text);
        }

        public void endIsTruncated(String text) {
            this.isTruncated = "true".equalsIgnoreCase(text);
        }
    }

    public class ErrorHandler extends SimpleHandler {
        private String type = null;
        private String code = null;
        private String message = null;
        private String detail = null;
        private String requestId = null;

        public String getCode() {
            return code;
        }

        public String getDetail() {
            return detail;
        }

        public String getMessage() {
            return message;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getType() {
            return type;
        }

        public void endType(String text) {
            this.type = text;
        }

        public void endCode(String text) {
            this.code = text;
        }

        public void endMessage(String text) {
            this.message = text;
        }

        public void endDetail(String text) {
            this.detail = text;
        }

        public void endRequestId(String text) {
            this.requestId = text;
        }

        // Handle annoying case where request id is in
        // the element "RequestID", not "RequestId"
        public void endRequestID(String text) {
            this.requestId = text;
        }
    }

}
