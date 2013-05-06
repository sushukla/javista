package org.javista.jspunit.ext;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class BlockJUnit4RunnerIgnoreAfters extends BlockJUnit4ClassRunner {

	public BlockJUnit4RunnerIgnoreAfters(Class<?> klass)
			throws InitializationError {
		super(klass);
	}

	/**
	 * Ignore individual test @AfterClass and @After method since clean should happen only from suite afterclass
	 */
	@Override
	protected Statement withAfterClasses(Statement statement) {
		return statement; 
	}
	
	@Override
	protected Statement withAfters(FrameworkMethod method, Object target,
			Statement statement) {
		return statement;
	}	

}
