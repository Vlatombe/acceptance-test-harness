package plugins;

import org.hamcrest.Matcher;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 Feature: Adds Warnings collection support
   In order to be able to collect and analyze warnings
   As a Jenkins user
   I want to install and configure Warnings plugin
 */
@WithPlugins("warnings")
public class WarningsPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }

    /**
     Scenario: Detect no errors in console log and workspace when there are none
       Given I have installed the "warnings" plugin
       And a job
       When I configure the job
       And I add "Scan for compiler warnings" post-build action
       And I add console parser for "Maven"
       And I add workspace parser for "Java Compiler (javac)" applied at "** / *"
       And I save the job
       And I build the job
       Then build should have 0 "Java" warnings
       Then build should have 0 "Maven" warnings
     */
    @Test
    public void detect_no_errors_in_console_log_and_workspace_when_there_are_none() {
        job.configure();
        WarningsPublisher pub = job.addPublisher(WarningsPublisher.class);
        pub.addConsoleScanner("Maven");
        pub.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        job.save();

        Build b = job.queueBuild().shouldSucceed();

        assertThatBuildHasWarnings(b, 0,"Java");
        assertThatBuildHasWarnings(b, 0, "Maven");
    }

    private void assertThatBuildHasWarnings(Build b, int i, String kind) {

//        job.open();
//        find(by.xpath("div[@class='test-trend-caption'][text()='%s Warnings Trend']",kind));

        Matcher<PageObject> m = hasAction(String.format("%s Warnings", kind));
        if (i==0)   m = not(m);

        assertThat(job, m);
        assertThat(b, m);
        b.open();
        assertThat(driver, hasContent(String.format("%s Warnings: %d",kind,i)));
    }
}
