/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 * http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.fabric.fab;

import org.fusesource.fabric.fab.util.Filter;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;

public class DependencyTreeFilterTest extends DependencyTestSupport {
    @Test
    public void testFilters() throws Exception {
        DependencyTreeResult node = collectDependencies("test-override-spring.pom");

        assertShareFilter(node, "commons-logging", "commons-logging", false, "");
        assertShareFilter(node, "commons-logging", "commons-logging", true,
                "*", "commons-logging:*", "*:*", "*:commons-logging", "commons-logging:commons-logging",
                "foo:bar commons-logging:commons-logging",
                "foo:bar commons-logging:commons-logging xyz:bar",
                "commons-logging:commons-logging foo:bar ");

        assertShareFilter(node, "org.springframework", "spring-core", true,
                "*", "org.springframework:*", "org.springframework:spring-core");
    }

    @Test
    public void testExcludedDependencies() throws Exception {
        DependencyTreeResult node = collectDependencies("test-osgi-provided.pom");

        // we exclude optional dependencies by default
        DependencyTree camelSpring = assertExcludeFilter(node, "org.apache.camel", "camel-spring", true, "", "");

        // we exclude provided dependencies by default
        DependencyTree osgi = assertExcludeFilter(node, "org.osgi", "org.osgi.core", true, "", "");

        assertEquals("getBundleId", "osgi.core", osgi.getBundleId());
    }

    @Test
    public void testExcludedTransitiveSharedDependencies() throws Exception {
        DependencyTreeResult node = collectDependencies("test-normal.pom");

        DependencyTree camelSpring = assertExcludeFilter(node, "org.springframework", "spring-context", true, "", "");
    }

    protected DependencyTree assertShareFilter(DependencyTreeResult result, String groupId, String artifactId, boolean expected, String... filterTexts) throws MalformedURLException {
        DependencyTree tree = assertFindDependencyTree(result, groupId, artifactId);

        for (String filterText : filterTexts) {
            Filter<DependencyTree> filter = DependencyTreeFilters.parseShareFilter(filterText);
            boolean actual = filter.matches(tree);
            assertEquals("Filter failed for " + filterText, expected, actual);
            //System.out.println("Testing " + tree + " for filter: " + filterText + " = " + actual);
        }
        return tree;
    }


    protected DependencyTree assertExcludeFilter(DependencyTreeResult result, String groupId, String artifactId, boolean expected, String filterText, String includeOptionalDependencyText) throws MalformedURLException {
        DependencyTree tree = assertFindDependencyTree(result, groupId, artifactId);
        Filter<DependencyTree> filter = DependencyTreeFilters.parseExcludeFilter(filterText, includeOptionalDependencyText);
        boolean actual = filter.matches(tree);
        assertEquals("Filter failed for " + filterText, expected, actual);
        //System.out.println("Testing " + tree + " for filter: " + filterText + " = " + actual);
        return tree;
    }

}
