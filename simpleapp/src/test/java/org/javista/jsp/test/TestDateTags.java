package org.javista.jsp.test;

import static org.junit.Assert.assertTrue;

import org.javista.jspunit.TestLauncher;
import org.javista.model.impl.GmtTimeModel;
import org.javista.model.impl.PstTimeModel;
import org.junit.AfterClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class TestDateTags {
	
	@Test
	public void testPstTag() {
		WebDriver driver = TestLauncher.executeTagTest("src/main/webapp/WEB-INF/tags/pst.tag", new PstTimeModel());
		System.out.println("page output: "+driver.getPageSource());
		WebElement element = driver.findElement(By.xpath("//div[@id='tm']"));
		assertTrue("page source= "+driver.getPageSource()+" did not contain PDT ",element.getText().contains("PDT"));
	}
	
	@Test
	public void testGmtTag() {
		WebDriver driver = TestLauncher.executeTagTest("src/main/webapp/WEB-INF/tags/gmt.tag", new GmtTimeModel());
		System.out.println("page output: "+driver.getPageSource());
		WebElement element = driver.findElement(By.xpath("//div[@id='tm']"));
		assertTrue("page source= "+driver.getPageSource()+" did not contain GMT ",element.getText().contains("GMT"));
	}
	
	@Test
	public void testJstlUseTag() {
		WebDriver driver = TestLauncher.executeTagTest("src/main/webapp/WEB-INF/tags/jstluse.tag", new Object());
		System.out.println("page output: "+driver.getPageSource());
	}
	
	@AfterClass
	public static void cleanup() {
		TestLauncher.cleanUp();
		TestLauncher.stopServer();
	}

}
