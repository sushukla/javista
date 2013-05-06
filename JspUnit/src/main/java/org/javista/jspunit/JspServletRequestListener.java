package org.javista.jspunit;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class JspServletRequestListener implements ServletRequestListener {
	
	@Override
	public void requestDestroyed(ServletRequestEvent arg0) {
		
	}

	@Override
	/**
	 * Create mock raptor context to be set for jsp tags needing utils:getNextHtmlId method.
	 */
	public void requestInitialized(ServletRequestEvent event) {
		HttpServletRequest req = (HttpServletRequest) event.getServletRequest();
		System.out.println("Log Request "+req.getQueryString());
	}

}
