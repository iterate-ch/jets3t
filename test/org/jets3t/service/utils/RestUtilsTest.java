package org.jets3t.service.utils;

import junit.framework.TestCase;

/**
 * @version $Id:$
 */
public class RestUtilsTest extends TestCase {

    public void testEncode() throws Exception {
        assertEquals("/p", RestUtils.encodeUrlPath("/p", "/"));
        assertEquals("/p%20d", RestUtils.encodeUrlPath("/p d", "/"));
        assertEquals("/p%2Ad", RestUtils.encodeUrlPath("/p*d", "/"));
        assertEquals("/p%2Bd", RestUtils.encodeUrlPath("/p+d", "/"));
    }

    public void testEncodeTrailingDelimiter() throws Exception {
        assertEquals("/a/p/", RestUtils.encodeUrlPath("/a/p/", "/"));
        assertEquals("/p%20d/", RestUtils.encodeUrlPath("/p d/", "/"));
    }

    public void testEncodeRelativeUri() throws Exception {
        assertEquals("a/p", RestUtils.encodeUrlPath("a/p", "/"));
        assertEquals("a/p/", RestUtils.encodeUrlPath("a/p/", "/"));
    }

    public void testEncodeEmoji() {
        assertEquals("a/%F0%9F%9A%80", RestUtils.encodeUrlPath("a/\uD83D\uDE80", "/"));
    }
}