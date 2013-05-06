package org.javista.jspunit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.javista.jspunit.ext.JspTestUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public class TestLauncher {

	private static Server server;
	public final static String JSP_BASE = "jspbase";
	public final static String JSP_NAME = "jspName";
	private final static String JSP_BASE_PATH = "/test";
	public static final String DEFAULT_WEB_CONTEXT = "/jsptest";

	/*
	 * relative path of war root starting from project base dir e.g. WebContent
	 * or src/main/webapp
	 */
	private static String warRoot = "src/main/webapp";
	private static Map<String, ConfigHolder> jspMap = new HashMap<String, ConfigHolder>();
	static Logger log = Logger.getLogger(TestLauncher.class.getName());

	/**
	 * Adds the test JSP for invocation from tests This method could be called
	 * from @BeforeClass method
	 * 
	 * @param testJspname
	 * @param attributeMap
	 */
	public static void addJsp(String testJspname,
			Map<String, Object> attributeMap) {
		ConfigHolder holder = new ConfigHolder(JSP_BASE_PATH, testJspname,
				attributeMap);
		jspMap.put(testJspname, holder);
	}

	/**
	 * Creates a wrapper jsp for the passed in tag and adds the jsp to the list
	 * of deployable jsp's
	 * 
	 * @param tagFilePath
	 *            absolute or relative path to tag file. relative path should
	 *            start from webcontent directory e.g.
	 *            /WebContent/WEB-INF/tags/abc.tag or for maven projects
	 *            src/main/webapp/WEB-INF/tags/abc.tag
	 * @param model
	 *            The single model required by tag
	 */
	public static void addTag(String tagFilePath, Object model) {
		Map<String, Object> attrMap = new HashMap<String, Object>();
		attrMap.put("model", model);
		File testJsp = generateTestJsp(tagFilePath, attrMap);
		ConfigHolder holder = new ConfigHolder(JSP_BASE_PATH,
				testJsp.getName(), attrMap);
		holder.setAutoGen(true);
		jspMap.put(testJsp.getName(), holder);
	}

	/**
	 * Creates a wrapper jsp for the passed in tag and adds the jsp to the list
	 * of deployable jsp's
	 * 
	 * @param tagFilePath
	 *            absolute or relative path to tag file. relative path should
	 *            start from webcontent directory e.g.
	 *            /WebContent/WEB-INF/tags/abc.tag or for maven projects
	 *            src/main/webapp/WEB-INF/tags/abc.tag
	 * 
	 * @param attributeMap
	 *            The attributes required by tag in form of key value pairs in a
	 *            Map
	 */
	public static void addTag(String tagFilePath,
			Map<String, Object> attributeMap) {
		File testJsp = generateTestJsp(tagFilePath, attributeMap);
		ConfigHolder holder = new ConfigHolder(JSP_BASE_PATH,
				testJsp.getName(), attributeMap);
		holder.setAutoGen(true);
		jspMap.put(testJsp.getName(), holder);
	}

	/**
	 * Creates a wrapper jsp for the passed in tag, deploys it to jetty server
	 * and sends the webdriver response
	 * 
	 * @param tagFilePath
	 *            absolute or relative path to tag file. relative path should
	 *            start from webcontent directory e.g.
	 *            /WebContent/WEB-INF/tags/abc.tag or for maven projects
	 *            src/main/webapp/WEB-INF/tags/abc.tag
	 * @param model
	 *            The model required by tag
	 * @return WebDriver object containing web response
	 */
	public static WebDriver executeTagTest(String tagFilePath, Object model) {
		Map<String, Object> attrMap = new HashMap<String, Object>();
		// Reading the tagfile and putting model object against correct
		// attribute
		Map<String, String> declaredAttrs = JspTestUtils
				.getTagAttributes(tagFilePath);
		for (Map.Entry<String, String> entry : declaredAttrs.entrySet()) {
			if (JspTestUtils.matchClass(entry.getValue(), model.getClass())) {
				attrMap.put(entry.getKey(), model);
			}
		}
		return executeTagTestBrowserType(tagFilePath, attrMap,
				BrowserVersion.getDefault());
	}

	/**
	 * Creates a wrapper jsp for the passed in tag, deploys it to jetty server
	 * and sends the webdriver response as HtmlUnitDriver instance
	 * 
	 * @param tagFilePath
	 *            absolute or relative path to tag file. relative path should
	 *            start from webcontent directory e.g.
	 *            /WebContent/WEB-INF/tags/abc.tag or for maven projects
	 *            src/main/webapp/WEB-INF/tags/abc.tag
	 * @param attributeMap
	 *            attributes required by the tag passed as map. Key is attribute
	 *            name and value is corresponding object. Can be null if no
	 *            attributes required for tag
	 * @return WebDriver object containing web response. Returns HtmlUnitDriver
	 *         if no Browser specified
	 */
	public static WebDriver executeTagTest(String tagFilePath,
			Map<String, Object> attributeMap) {
		return executeTagTestBrowserType(tagFilePath, attributeMap,
				BrowserVersion.getDefault());
	}

	public static WebDriver executeTagTestBrowserType(String tagFilePath,
			Map<String, Object> attributeMap, BrowserVersion version) {
		File testJsp = generateTestJsp(tagFilePath, attributeMap);
		ConfigHolder holder = new ConfigHolder(JSP_BASE_PATH,
				testJsp.getName(), attributeMap);
		holder.setAutoGen(true);
		jspMap.put(testJsp.getName(), holder);
		if (server == null || !server.isRunning()) {
			setupServer();
			startServer();
		} else if (server != null && server.isRunning()) {
			updateServer(testJsp.getName(), holder);
		}
		String baseUrl = "http://localhost:" + JspTestUtils.getServerPort()
				+ TestLauncher.DEFAULT_WEB_CONTEXT;
		String url = baseUrl + holder.getUrlMapping();
		WebDriver driver = new HtmlUnitDriver(version);
		try {
			driver.get(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return driver;
	}

	public static WebDriver executeTagTestInBrowser(String tagFilePath,
			Object model, BrowserType type) {
		Map<String, Object> attrMap = new HashMap<String, Object>();
		// Reading the tagfile and putting model object against correct
		// attribute
		Map<String, String> declaredAttrs = JspTestUtils
				.getTagAttributes(tagFilePath);
		for (Map.Entry<String, String> entry : declaredAttrs.entrySet()) {
			if (JspTestUtils.matchClass(entry.getValue(), model.getClass())) {
				attrMap.put(entry.getKey(), model);
			}
		}
		return executeTagTestInBrowser(tagFilePath, attrMap, type, null);
	}

	public static WebDriver executeTagTestInBrowser(String tagFilePath,
			Map<String, Object> attributeMap, BrowserType type) {
		return executeTagTestInBrowser(tagFilePath, attributeMap, type, null);
	}

	/**
	 * returns WebDriver instance with passed in browser type
	 * 
	 * @param tagFilePath
	 * @param attributeMap
	 * @param type
	 *            FIREFOX, IE, CHROME, HTMLUNIT or REMOTE
	 * @return
	 */
	public static WebDriver executeTagTestInBrowser(String tagFilePath,
			Map<String, Object> attributeMap, BrowserType type,
			DesiredCapabilities dc) {
		String hostname = (type == BrowserType.REMOTE) ? JspTestUtils
				.getHostName() : "localhost";
		File testJsp = generateTestJsp(tagFilePath, attributeMap);
		ConfigHolder holder = new ConfigHolder(JSP_BASE_PATH,
				testJsp.getName(), attributeMap);
		holder.setAutoGen(true);
		jspMap.put(testJsp.getName(), holder);
		if (server == null || !server.isRunning()) {
			setupServer();
			startServer();
		} else if (server != null && server.isRunning()) {
			updateServer(testJsp.getName(), holder);
		}
		String baseUrl = "http://" + hostname + ":"
				+ JspTestUtils.getServerPort()
				+ TestLauncher.DEFAULT_WEB_CONTEXT;
		String url = baseUrl + holder.getUrlMapping();
		WebDriver driver = JspTestUtils.getDriver(type, dc);
		try {
			driver.get(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return driver;
	}

	/**
	 * Sets up the Jetty server. Configures Test Dispatcher Servlet for each JSP
	 * added to the test. The servlet forwards the response to particular jsp
	 * based on the urlMapping provided. Uses default "/jsptest" as test context
	 * if no webcontext is specified Also starts the server after completing
	 * configuration This method could be called from @BeforeClass method of
	 * JUnit test
	 */
	public static void setupServer() {
		// System.setProperty("DEBUG", "true");
		server = new Server(JspTestUtils.allocatePort());
		generateTestWebXml();
		WebAppContext webCtx = new WebAppContext();
		webCtx.setContextPath(DEFAULT_WEB_CONTEXT);
		// dummy web.xml for jetty
		webCtx.setDescriptor(getWarRoot() + "/WEB-INF/web-test.xml");
		webCtx.setResourceBase(getWarRoot());

		for (String key : jspMap.keySet()) {
			ConfigHolder holder = jspMap.get(key);
			TestDispatcher dpatcher = new TestDispatcher();
			dpatcher.setAttributes(holder.getAttributeMap());
			ServletHolder sHolder = new ServletHolder(dpatcher);
			sHolder.setInitParameter(JSP_BASE, holder.getJspBasePath());
			sHolder.setInitParameter(JSP_NAME, key);
			webCtx.addServlet(sHolder, holder.getUrlMapping());
		}
		addEventListeners(webCtx);
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { webCtx });
		server.setHandler(contexts);
	}

	public static void startServer() {
		try {
			server.start();
			// if user wants to see visual test result they can hit the browser
			// while server is waiting
			if (System.getProperty("server.wait") != null) {
				server.join();
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "exception in starting Jetty test server "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	public static void updateServer(String jspName, ConfigHolder holder) {
		if (server == null) {
			log.log(Level.WARNING,
					"running instance of server not found, cannot add jsp handler");
			return;
		}
		ContextHandlerCollection ctxHandler = (ContextHandlerCollection) server
				.getHandler();
		Handler[] handlerArray = ctxHandler.getHandlers();
		WebAppContext webContext = (WebAppContext) handlerArray[0];
		/**
		 * hack to reinitialize taglib cache for newly added servlet.
		 */
		try {
			ServletHolder jspServletHolder = webContext.getServletHandler()
					.getServlet("jsp");
			Servlet jspServlet = jspServletHolder.getServlet();
			ServletConfig jspServletConfig = jspServlet.getServletConfig();
			jspServlet.destroy();
			jspServlet.init(jspServletConfig);
		} catch (ServletException e) {
			log.log(Level.WARNING, "error in reinitializing JspServlet", e);
			e.printStackTrace();
		}

		TestDispatcher dpatcher = new TestDispatcher();
		dpatcher.setAttributes(holder.getAttributeMap());
		ServletHolder sHolder = new ServletHolder(dpatcher);
		sHolder.setInitParameter(JSP_BASE, holder.getJspBasePath());
		sHolder.setInitParameter(JSP_NAME, jspName);

		webContext.addServlet(sHolder, holder.getUrlMapping());

	}

	public static void addEventListeners(WebAppContext webContext) {
		webContext.addEventListener(new JspServletRequestListener());
		webContext.addFilter(EmptyFilter.class, "/*",
				EnumSet.allOf(DispatcherType.class));
	}

	/**
	 * stop jetty
	 */
	public static void stopServer() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Generates wrapper JSP for passed in tag and saves it temporarily under
	 * WAR_ROOT/test folder
	 * 
	 * @param tagFilePath
	 * @return
	 */
	private static File generateTestJsp(String tagFilePath,
			Map<String, Object> attributeMap) {
		int webInfIdx = tagFilePath.indexOf("/WEB-INF");
		if (webInfIdx != -1) {
			tagFilePath = tagFilePath.substring(webInfIdx);
			tagFilePath = getWarRoot() + tagFilePath;
		}
		File file = new File(tagFilePath);
		if (!file.exists()) {
			log.log(Level.SEVERE, "input tagfilepath should be valid "
					+ tagFilePath);
			throw new RuntimeException("input tagfilepath is invalid "
					+ tagFilePath);
		}
		String tagName = file.getName();
		String basePath = file.getParent();
		String jspFileName = tagName.replace(".tag", ".jsp");
		log.log(Level.INFO, "jspFileName " + jspFileName);
		String b1 = getWarRoot() + File.separator + JSP_BASE_PATH;
		new File(b1).mkdirs();
		int index2 = basePath.indexOf("WEB-INF");
		String tagdir = "/WEB-INF/tags";
		if (index2 != -1) {
			tagdir = basePath.substring(index2 - 1).replace('\\', '/');
		}
		String tagPrefix = "lists";
		int index3 = tagdir.lastIndexOf('/');
		if (index3 != -1) {
			tagPrefix = tagdir.substring(index3 + 1);
		}
		log.log(Level.INFO, "b1 = " + b1 + " tag prefix " + tagPrefix);
		String jspTestFileName = b1 + File.separator + jspFileName;
		FileWriter writer = null;
		try {
			// Construct the string for tag invocation from jsp
			StringBuilder sBuilder = new StringBuilder();

			if (attributeMap != null) {
				for (String key : attributeMap.keySet()) {
					sBuilder.append(" ").append(key);
					sBuilder.append("=\"${").append(key).append("}\" ");
				}
			}
			writer = new FileWriter(new File(jspTestFileName));
			writer.write("<%@ page language=\"java\" contentType=\"text/html; charset=ISO-8859-1\" pageEncoding=\"ISO-8859-1\"%>\n");
			writer.write("<%@ taglib prefix=\"" + tagPrefix + "\" tagdir=\""
					+ tagdir + "\" %>\n");
			// writer.write("<lists:"+tagName.replace(".tag","").trim()+" model=\"${model}\" />");
			writer.write("<" + tagPrefix + ":"
					+ tagName.replace(".tag", "").trim() + sBuilder.toString()
					+ " />");
		} catch (IOException e) {
			log.log(Level.WARNING,
					"io exception while writing test JSP " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException e) {
			}
		}
		log.log(Level.INFO, "Created test JSP successfully "
				+ new File(jspTestFileName).getPath());
		return new File(jspTestFileName);
	}

	/**
	 * non-xml version not used
	 * 
	 * @return
	 */
	public static String getJspConfigWebXml() {
		File file = new File(getWarRoot() + "/WEB-INF/web.xml");
		FileReader reader = null;
		String line = null;
		StringBuilder jspConf = new StringBuilder();
		try {
			boolean start = false;
			reader = new FileReader(file);
			BufferedReader bReader = new BufferedReader(reader);
			while ((line = bReader.readLine()) != null) {
				if (line.trim().startsWith("<jsp-config>")) {
					jspConf.append(line).append("\n");
					start = true;
				} else if (line.trim().endsWith("</jsp-config>")) {
					jspConf.append(line).append("\n");
					start = false;
					break;
				} else if (start) {
					jspConf.append(line).append("\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return jspConf.toString();
	}

	/**
	 * Generate test web.xml with jsp-config from original web.xml if present
	 * for test (since we do not want spring wiring from project web.xml)
	 * 
	 * @return
	 */
	private static void generateTestWebXml() {
		File webXmlFile = new File(getWarRoot() + "/WEB-INF/web.xml");
		Document doc = null;
		FileWriter outputWriter = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(webXmlFile);
			doc.getDocumentElement().normalize();
			log.log(Level.INFO, "Root element "
					+ doc.getDocumentElement().getNodeName());
			Element rootNode = doc.getDocumentElement();
			NodeList jspList = rootNode.getElementsByTagName("jsp-config");
			Node jspConfigNode = jspList.item(0);

			Document newDoc = new DocumentImpl();
			Element webAppNode = newDoc.createElement(rootNode.getNodeName());
			NamedNodeMap nnm = rootNode.getAttributes();
			for (int i = 0; i < nnm.getLength(); i++) {
				webAppNode.setAttribute(nnm.item(i).getNodeName(), nnm.item(i)
						.getNodeValue());
			}
			if (jspConfigNode != null) {
				Node newJspConfig = newDoc.importNode(jspConfigNode, true);
				webAppNode.appendChild(newJspConfig);
			}
			File file = new File(getWarRoot() + "/WEB-INF/web-test.xml");
			outputWriter = new FileWriter(file);
			OutputFormat of = new OutputFormat("XML", "UTF-8", true);
			of.setIndent(1);
			of.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(outputWriter, of);
			serializer.asDOMSerializer();
			serializer.serialize(webAppNode);
			log.log(Level.INFO,
					"Created test Web xml successfully " + file.getPath());
		} catch (Exception e1) {
			log.log(Level.WARNING, "Exception during parsing web.xml", e1);
		} finally {
			try {
				if (outputWriter != null) {
					outputWriter.flush();
					outputWriter.close();
				}
			} catch (IOException e) {
			}
		}

		/*
		 * FileWriter writer = null; try { writer = new FileWriter(file);
		 * writer.
		 * write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
		 * ); writer.write(
		 * "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:web=\"http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" id=\"WebApp_ID\" version=\"3.0\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\">\n"
		 * ); writer.write(getJspConfigWebXml()); writer.write("</web-app>"); }
		 * catch (IOException e) { log.log(Level.WARNING,
		 * "io exception while writing test XML "+e.getMessage());
		 * e.printStackTrace(); } finally { try { if (writer != null) {
		 * writer.flush(); writer.close(); } } catch (IOException e) { } }
		 */
	}

	/**
	 * deletes the temporary test artifacts (test JSP, test web xml) Should be
	 * called from AfterClass or when test assertions are done. Also stops the
	 * Jetty server
	 */
	public static void cleanUp() {
		File jspTestBase = new File(getWarRoot() + File.separator
				+ JSP_BASE_PATH);
		for (String jspI : jspMap.keySet()) {
			if (jspMap.get(jspI).isAutoGen()) {
				File jspTestFile = new File(jspTestBase, jspI);
				jspTestFile.delete();
				log.log(Level.INFO, "deleted test JSP " + jspTestFile);
			}
		}
		if (jspTestBase.isDirectory() && jspTestBase.list().length == 0) {
			jspTestBase.delete();
		}
		File file = new File(getWarRoot() + "/WEB-INF/web-test.xml");
		if (file.exists()) {
			file.delete();
		}
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setWarRoot(String root) {
		if (isValidWebRoot(root)) {
			TestLauncher.warRoot = root;
		} else {
			throw new RuntimeException("Could not find WAR root directory "
					+ root);
		}
	}

	public static boolean isValidWebRoot(String root) {
		File file = new File(root);
		File webInf = new File(root + "/WEB-INF");
		if (file.exists() && file.isDirectory() && webInf.exists()) {
			return true;
		} else {
			log.severe(" war root " + file.getAbsolutePath()
					+ " does not exist");
			return false;
		}
	}

	public static String getWarRoot() {
		return warRoot;
	}

}
