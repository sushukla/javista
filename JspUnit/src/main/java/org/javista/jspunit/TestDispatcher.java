package org.javista.jspunit;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class TestDispatcher extends HttpServlet {
	
	static Logger log = Logger.getLogger(TestLauncher.class.getName());

	private Map<String, Object> attributeMap;
	
	public synchronized void setAttributes (Map<String, Object> modelMap) {
		attributeMap = modelMap;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			String jspBase = getServletConfig().getInitParameter(TestLauncher.JSP_BASE);
			String jspName = getServletConfig().getInitParameter(TestLauncher.JSP_NAME);
			if (attributeMap != null) {
				for (String key: attributeMap.keySet()) {
					Object value = attributeMap.get(key);
					log.log(Level.INFO, "setting key "+key+" value "+value+" for test jsp ");
					request.setAttribute(key, value);
				}
			}
			getServletContext().getRequestDispatcher(jspBase+"/"+jspName).forward(request, response);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

}
