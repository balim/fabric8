package org.fusesource.fabric.fab.osgi.itests;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.testing.Helper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.service.blueprint.container.BlueprintContainer;

import static org.apache.karaf.testing.Helper.felixProvisionalApis;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartup;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.workingDirectory;

@RunWith(JUnit4TestRunner.class)
public class FabIntegrationTest extends IntegrationTestSupport {

    @Test
    public void testRun() throws Exception {
        Thread.sleep(10000);

        // Make sure the command services are available
        assertNotNull(getOsgiService(BlueprintContainer.class, "osgi.blueprint.container.symbolicname=org.apache.karaf.shell.obr", 20000));
        assertNotNull(getOsgiService(BlueprintContainer.class, "osgi.blueprint.container.symbolicname=org.apache.karaf.shell.wrapper", 20000));

        CommandProcessor cp = getOsgiService(CommandProcessor.class);
        CommandSession cs = cp.createSession(System.in, System.out, System.err);

        cs.execute("osgi:list");

        assertStartBundle("org.apache.karaf.shell.log");

        assertStartBundle("org.fusesource.fabric.fab.fabric-fab-osgi");

        Thread.sleep(1000);

        // TODO now lets start the new FAB...

        // TODO now lets stop the bundles....

        Thread.sleep(1000);

        cs.execute("osgi:list");

        cs.close();
    }


    @Configuration
    public static Option[] configuration() throws Exception {
        Option[] options = combine(
                // Default karaf environment
                Helper.getDefaultOptions(
                        // this is how you set the default log level when using pax logging (logProfile)
                        //Helper.setLogLevel("TRACE")
                        Helper.setLogLevel("INFO")
                ),

                // add karaf features
                Helper.loadKarafStandardFeatures("obr", "wrapper"),

                // add fab features
                scanFeatures(
                        maven().groupId("org.fusesource.fabric").artifactId("fabric-distro").type("xml").classifier("features").versionAsInProject(),
                        "fabric-fab"
                ),

                workingDirectory("target/paxrunner/core/"),

                waitForFrameworkStartup(),

                // TODO Test on both equinox and felix
                // TODO: pax-exam does not support the latest felix version :-(
                // TODO: so we use the higher supported which should be the same
                // TODO: as the one specified in itests/dependencies/pom.xml
                //equinox(), felix().version("3.0.2")
                felix().version("3.0.2"),

                felixProvisionalApis()
        );
        // Stop the shell log bundle
        //Helper.findMaven(options, "org.apache.karaf.shell", "org.apache.karaf.shell.log").noStart();
        return options;
    }


}
