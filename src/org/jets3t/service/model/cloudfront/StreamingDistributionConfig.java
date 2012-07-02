/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2010 James Murty
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
package org.jets3t.service.model.cloudfront;


public class StreamingDistributionConfig extends DistributionConfig {
    private String[] trustedSignerAwsAccountNumbers;

    /**
     * Construct a distribution configuration compatible with CloudFront API versions
     * 2012-05-05 and later (i.e. including cache behaviors and multiple origins)
     *
     * @param origins
     * @param callerReference
     * @param cnames
     * @param comment
     * @param enabled
     * @param loggingStatus
     * @param trustedSignerAwsAccountNumbers
     */
    public StreamingDistributionConfig(Origin[] origins, String callerReference,
        String[] cnames, String comment, boolean enabled,
        LoggingStatus loggingStatus, String[] trustedSignerAwsAccountNumbers)
    {
        super(origins, callerReference, cnames, comment, enabled, loggingStatus, null, null, null);
        this.trustedSignerAwsAccountNumbers = trustedSignerAwsAccountNumbers;
    }


    /**
     * @deprecated as of 2012-05-05 API version.
     *
     * @param origin
     * @param callerReference
     * @param cnames
     * @param comment
     * @param enabled
     * @param loggingStatus
     * @param trustedSignerSelf
     * @param trustedSignerAwsAccountNumbers
     * @param requiredProtocols
     */
    @Deprecated
    public StreamingDistributionConfig(Origin origin, String callerReference,
        String[] cnames, String comment, boolean enabled,
        LoggingStatus loggingStatus,
        boolean trustedSignerSelf, String[] trustedSignerAwsAccountNumbers,
        String[] requiredProtocols)
    {
        super(origin, callerReference, cnames, comment, enabled, loggingStatus,
            trustedSignerSelf, trustedSignerAwsAccountNumbers,
            requiredProtocols, null, null);
    }

    /**
     * @deprecated as of 2012-05-05 API version.
     *
     * @param origin
     * @param callerReference
     * @param cnames
     * @param comment
     * @param enabled
     * @param loggingStatus
     */
    @Deprecated
    public StreamingDistributionConfig(Origin origin, String callerReference,
        String[] cnames, String comment, boolean enabled, LoggingStatus loggingStatus)
    {
        super(origin, callerReference, cnames, comment, enabled, loggingStatus,
            false, null, null, null, null);
    }

    @Override
    public String[] getTrustedSignerAwsAccountNumbers() {
        return trustedSignerAwsAccountNumbers;
    }

}
