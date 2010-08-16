/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2006-2010 James Murty
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
package org.jets3t.service.acl;

import com.jamesmurty.utils.XMLBuilder;

import org.jets3t.service.S3ServiceException;

import java.util.Iterator;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Represents a Google Storage Access Control List (ACL), including the ACL's set of grantees and the
 * permissions assigned to each grantee.
 * <p>
 *
 * </p>
 *
 * @author Google Developers
 *
 */
public class GSAccessControlList extends AccessControlList {

    /**
     * Returns a string representation of the ACL contents, useful for debugging.
     */
    @Override
    public String toString() {
        return "GSAccessControlList [owner=" + owner + ", grants=" + getGrants() + "]";
    }

    @Override
    public XMLBuilder toXMLBuilder() throws S3ServiceException, ParserConfigurationException,
        FactoryConfigurationError, TransformerException
    {
        if (owner == null) {
            throw new S3ServiceException("Invalid AccessControlList: missing an S3Owner");
        }
        XMLBuilder builder = XMLBuilder.create("AccessControlList")
            .elem("Owner")
                .elem("ID").text(owner.getId()).up();

        if (owner.getDisplayName() != null) {
          builder.elem("DisplayName").text(owner.getDisplayName()).up();
        }

        builder.up();

        XMLBuilder accessControlList = builder.elem("Entries");
        Iterator grantIter = grants.iterator();
        while (grantIter.hasNext()) {
            GrantAndPermission gap = (GrantAndPermission) grantIter.next();
            GranteeInterface grantee = gap.getGrantee();
            Permission permission = gap.getPermission();
            accessControlList
                .elem("Entry")
                    .importXMLBuilder(grantee.toXMLBuilder())
                    .elem("Permission").text(permission.toString());
        }
        return builder;
    }
}
