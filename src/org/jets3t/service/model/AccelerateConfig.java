/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2016 David Kocher
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
import org.jets3t.service.Constants;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Objects;

public class AccelerateConfig {
    private boolean enabled;

    public AccelerateConfig(boolean enabled) {
        this.enabled = enabled;
    }

    public AccelerateConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        XMLBuilder builder = XMLBuilder.create("AccelerateConfiguration")
                .attr("xmlns", Constants.XML_NAMESPACE)
                .elem("Status").t(enabled ? "Enabled" : "Suspended");
        return builder.asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccelerateConfig)) return false;
        AccelerateConfig that = (AccelerateConfig) o;
        return enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled);
    }
}
