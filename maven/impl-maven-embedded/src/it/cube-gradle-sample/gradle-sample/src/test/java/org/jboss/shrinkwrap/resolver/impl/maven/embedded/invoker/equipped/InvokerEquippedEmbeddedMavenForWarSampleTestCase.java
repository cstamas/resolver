package org.jboss.shrinkwrap.resolver.impl.maven.embedded.invoker.equipped;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.impl.maven.embedded.pom.equipped.ResolverErrorOutputHandler;
import org.jboss.shrinkwrap.resolver.impl.maven.embedded.pom.equipped.ResolverOutputHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.jboss.shrinkwrap.resolver.impl.maven.embedded.Utils.getPropertiesWithSkipTests;
import static org.jboss.shrinkwrap.resolver.impl.maven.embedded.Utils.pathToWarSamplePom;
import static org.jboss.shrinkwrap.resolver.impl.maven.embedded.Utils.verifyMavenVersion;
import static org.jboss.shrinkwrap.resolver.impl.maven.embedded.Utils.verifyWarSampleWithSources;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
class InvokerEquippedEmbeddedMavenForWarSampleTestCase {

    @Test
    void testWarSampleBuildWithMaven310() {
        InvocationRequest request = new DefaultInvocationRequest();
        Invoker invoker = new DefaultInvoker();

        request.setPomFile(new File(pathToWarSamplePom));
        request.setGoals(Arrays.asList("clean", "package", "source:jar"));

        Properties props = getPropertiesWithSkipTests();
        request.setProperties(props);

        StringBuffer logBuffer = new StringBuffer("");
        invoker.setOutputHandler(new ResolverOutputHandler(logBuffer));
        invoker.setErrorHandler(new ResolverErrorOutputHandler(logBuffer));

        request.setShowVersion(true);

        BuiltProject builtProject = EmbeddedMaven
            .withMavenInvokerSet(request, invoker)
            .useMaven3Version("3.1.0")
            .build();
        builtProject.setMavenLog(logBuffer.toString());

        verifyWarSampleWithSources(builtProject);
        verifyMavenVersion(builtProject, "3.1.0");
    }

    @Test
    void testIfWarSampleBuildFailsWithException() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            InvocationRequest request = new DefaultInvocationRequest();
            Invoker invoker = new DefaultInvoker();

            request.setPomFile(new File(pathToWarSamplePom));
            request.setGoals(Arrays.asList("clean", "package"));

            request.setProfiles(Arrays.asList("failing"));
            request.setProperties(getPropertiesWithSkipTests());

            BuiltProject builtProject = EmbeddedMaven
                    .withMavenInvokerSet(request, invoker)
                    .useMaven3Version("3.3.9")
                    .build();
        });
    }

    @Test
    void testIfWarSampleBuildFailsWithExceptionBecauseOfMissingMaven() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            InvocationRequest request = new DefaultInvocationRequest();
            Invoker invoker = new DefaultInvoker();

            request.setPomFile(new File(pathToWarSamplePom));
            request.setGoals(Arrays.asList("clean", "package"));

            BuiltProject builtProject = EmbeddedMaven
                    .withMavenInvokerSet(request, invoker)
                    .build();
        });
    }

    @Test
    void testIfWarSampleBuildFailsWithoutException() {
        InvocationRequest request = new DefaultInvocationRequest();
        Invoker invoker = new DefaultInvoker();

        request.setPomFile(new File(pathToWarSamplePom));
        request.setGoals(Arrays.asList("clean", "package"));

        request.setProfiles(Arrays.asList("failing"));
        request.setProperties(getPropertiesWithSkipTests());

        BuiltProject builtProject = EmbeddedMaven
                .withMavenInvokerSet(request, invoker)
                .useMaven3Version("3.3.9")
                .ignoreFailure()
                .build();

        Assertions.assertEquals(1, builtProject.getMavenBuildExitCode());
    }

}