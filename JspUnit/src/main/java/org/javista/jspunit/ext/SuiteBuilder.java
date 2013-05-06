package org.javista.jspunit.ext;

import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class SuiteBuilder extends RunnerBuilder {

	@Override
	public Runner runnerForClass(Class<?> testClass) throws Throwable {
		return new BlockJUnit4RunnerIgnoreAfters(testClass);
	}

}
