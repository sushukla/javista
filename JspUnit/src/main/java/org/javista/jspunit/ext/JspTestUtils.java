package org.javista.jspunit.ext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javista.jspunit.BrowserType;
import org.javista.jspunit.TestLauncher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;


public class JspTestUtils {

	static Logger log = Logger.getLogger(JspTestUtils.class.getName());

	public static final String REMOTE_WEB_GRID_URL = "REMOTE_WEB_GRID_URL";
	private static final int SERVER_PORT = 8090;
	private static int retries = 20;
	private static int usedPort = SERVER_PORT;

	public static WebDriver getDriver(BrowserType type, DesiredCapabilities capabilities) {
		if (capabilities == null) {
			capabilities = new DesiredCapabilities();
		}
		try {
			switch (type) {
				case IE:
					DesiredCapabilities local = DesiredCapabilities.internetExplorer();
					local.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
					capabilities.merge(local);
					return new InternetExplorerDriver(capabilities);
				case FIREFOX :
					return new FirefoxDriver(capabilities);
				case CHROME :
					return new ChromeDriver(capabilities);
				case HTMLUNIT :
					return new HtmlUnitDriver(capabilities);
				case REMOTE :
					return new RemoteWebDriver(new URL(getRemoteGridURL()),
							DesiredCapabilities.firefox());
				default: 
					return new HtmlUnitDriver(capabilities);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new HtmlUnitDriver(capabilities);
		}
	}
	
	/**
	 * returns the remote grid url. resolves according to following rules in order
	 * 1. from system.getenv("REMOTE_WEB_GRID_URL")
	 * 2. from java runtime property , -DREMOTE_WEB_GRID_URL
	 * 3. from testconfig.properties
	 * 4. defaults to http://qa-ci002.qa.ebay.com:8080/wd/hub
	 * @return String the configured remote grid url
	 */
	public static String getRemoteGridURL() {
		String remoteUrl = "http://qa-ci002.qa.ebay.com:8080/wd/hub";
		Properties props = new Properties();
		InputStream in = JspTestUtils.class.getResourceAsStream("/testconfig.properties");
		try {
			props.load(in);
			remoteUrl = props.getProperty(JspTestUtils.REMOTE_WEB_GRID_URL);
		} catch (Exception e) {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					//Ignore
				}
			}
		}
		String envUrl = System.getenv(JspTestUtils.REMOTE_WEB_GRID_URL);
		if (envUrl == null) {
			envUrl = System.getProperty(JspTestUtils.REMOTE_WEB_GRID_URL);
			if (envUrl != null) {
				log.log(Level.INFO,
						"Using REMOTE_WEB_GRID_URL from -DREMOTE_WEB_GRID_URL "
								+ envUrl);				
				return envUrl;
			}
		} else {
			log.log(Level.INFO,
					"Using REMOTE_WEB_GRID_URL from system environment "
							+ envUrl);	
			return envUrl;
		}
		return remoteUrl;
	}
	
	public static String getHostName() {
		String hostname = "localhost";
		try {
			hostname = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return hostname;
	}
	
	public static int allocatePort() {
		int serverPort = SERVER_PORT;
		String envPort = System.getenv("jetty.server.port");
		if (envPort == null) {
			envPort = System.getProperty("jetty.server.port");
			log.log(Level.INFO,
					"Using server port for jetty from -Djetty.server.port "
							+ envPort);
		}
		try {
			serverPort = Integer.parseInt(envPort);
		} catch (NumberFormatException n) {
			log.log(Level.WARNING,
					"NumberFormatException for Jetty Port " + n.getMessage());
			serverPort = SERVER_PORT;
		}
		usedPort = getFreePort(serverPort);
		log.log(Level.INFO, "Using server port for jetty " + usedPort);
		return usedPort;
	}	
	
	public static int getServerPort() {
		log.log(Level.INFO, "Returning opened server port for jetty " + usedPort);
		return usedPort;
	}
	
	public synchronized static int getFreePort(int seed) {
		try {
			retries--;
			ServerSocket ss = new ServerSocket(seed);
			ss.close();
			return seed;
		} catch (Exception e) {
			if (retries > 0) {
				log.log(Level.WARNING,
						"Retrying due to non availability of port " + seed + " retry: "+retries);
				return getFreePort(seed+1);
			} else {
				e.printStackTrace();
				return seed;
			}
		}
	}
	
	/**
	 * returns attribute name and attribute type after parsing the tag file
	 * @param tagFilePath
	 * @return
	 */
	public static Map<String, String> getTagAttributes(String tagFilePath) {
		Map<String, String> attrMap = new HashMap<String, String>();
		int webInfIdx = tagFilePath.indexOf("/WEB-INF");
		if (webInfIdx != -1) {
			tagFilePath = tagFilePath.substring(webInfIdx);
			tagFilePath = TestLauncher.getWarRoot() + tagFilePath;
		}
		File file = new File(tagFilePath);
		if (!file.exists()) {
			log.log(Level.SEVERE, "input tagfilepath should be valid "
					+ tagFilePath);
			throw new RuntimeException("input tagfilepath is invalid "
					+ tagFilePath);
		}	
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new FileReader(file));
			String line = null;
			List<String> tempList = new ArrayList<String>();
			boolean attributeLine = false;
			while ((line = bReader.readLine()) != null) {
				if (line.indexOf("attribute") != -1 && line.indexOf("<%@") != -1) {
					String attrName = null;
					String attrType = null;
					int in1 = line.indexOf("name=");
					if (in1 != -1) {
						int in2 = line.indexOf("\"", in1);
						int in3 = line.indexOf("\"", in2+1);
						attrName = line.substring(in2+1, in3);
					}
					int in4 = line.indexOf("type=");
					if (in4 != -1) {
						int in5 = line.indexOf("\"", in4);
						int in6 = line.indexOf("\"", in5+1);
						attrType = line.substring(in5+1, in6);
					} else if (attrName != null){
						tempList.add(attrName);
						attributeLine = true;
					}
					if (attrName != null && attrType != null) {
						attrMap.put(attrName, attrType);
						attributeLine = true;
					} 

				} 
				else if ( attributeLine && line.trim().startsWith("type")) {
					String attrType = null;
					int in4 = line.indexOf("type=");
					if (in4 != -1) {
						int in5 = line.indexOf("\"", in4);
						int in6 = line.indexOf("\"", in5+1);
						attrType = line.substring(in5+1, in6);
					} 					
					if (attrType !=null && tempList.size() > 0) {
						attrMap.put(tempList.get(0), attrType);
						tempList.clear();
					}
					attributeLine = false;
				}
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bReader != null) {
				try {
					bReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return attrMap;
			
	}
	
	public static boolean matchClass(String className, Class<?> classtoMatch) {
		try {
			Class<?> baseClass = Class.forName(className);
			if (baseClass.isAssignableFrom(classtoMatch)) {
				return true;
			}
		} catch (Exception e) {
			
		}
		return false;
	}

}
