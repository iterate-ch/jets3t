/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2012 James Murty
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
package org.jets3t.service.model;

import com.jamesmurty.utils.XMLBuilder;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the ownership controls configuration of a bucket.
 *
 * @author Yves Langisch
 */
public class OwnershipControlsConfig {

    private List<Rule> rules = new ArrayList<Rule>();

    public List<Rule> getRules() {
        return rules;
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    /**
     *
     * @return
     * An XML representation of the object suitable for use as an input to the REST/HTTP interface.
     *
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public String toXml()
            throws ParserConfigurationException, FactoryConfigurationError, TransformerException
    {
        XMLBuilder builder = XMLBuilder.create("OwnershipControls");
        for (Rule rule: this.getRules()) {
            builder.elem("Rule").t(rule.ownership.textValue);
        }
        return builder.asString();
    }

    public class Rule {
        protected ObjectOwnership ownership;

        public ObjectOwnership getOwnership() {
            return ownership;
        }

        public void setOwnership(ObjectOwnership ownership) {
            this.ownership = ownership;
        }
    }

    public enum ObjectOwnership {
        BUCKET_OWNER_PREFERRED ("BucketOwnerPreferred"),
        OBJECT_WRITER ("ObjectWriter"),
        BUCKET_OWNER_ENFORCED ("BucketOwnerEnforced");

        private final String textValue;

        ObjectOwnership(String textValue) {
            this.textValue = textValue;
        }

        public String toText() {
            return textValue;
        }

        public static ObjectOwnership fromText(String text) {
            for (ObjectOwnership e: ObjectOwnership.values()) {
                if (e.toText().equalsIgnoreCase(text)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Invalid ObjectOwnership: " + text);
        }
    }
}
