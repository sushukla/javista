package org.javista.jspunit.ext;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * custom runner for Jsp Test Suite. It ignores <code>@AfterClass</code> annotations on individual test classes
 * since we do not want server to be stopped per test when running from a Suite.
 * When creating a suite, Use this as <code>@RunWith(JspSuite.class)</code>
 * @author sushukla
 *
 */
public class JspSuite extends Suite {

	public JspSuite(Class<?> testClass, RunnerBuilder builder)
			throws InitializationError {
		super(testClass, new SuiteBuilder());
	}

}
