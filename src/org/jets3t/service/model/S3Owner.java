/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 *
 * Copyright 2006 James Murty
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

import java.io.Serializable;

/**
 * Represents an S3 owner object with a canonical ID and, optionally, a display name.
 *
 * @author James Murty
 */
public class S3Owner implements Serializable {
    private static final long serialVersionUID = -8916731456944569115L;

    private String displayName;
    private String id;

    public S3Owner() {
    }

    public S3Owner(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String toString() {
    	return "S3Owner ["
    		+ (getDisplayName() != null ? "name=" + getDisplayName() + ", " : "")
    		+ "id=" + getId() + "]";
    }

    public String getId() {
    	return id;
    }

    public void setId(String id) {
    	this.id = id;
    }

    public String getDisplayName() {
    	return displayName;
    }

    public void setDisplayName(String name) {
    	this.displayName = name;
    }

}
