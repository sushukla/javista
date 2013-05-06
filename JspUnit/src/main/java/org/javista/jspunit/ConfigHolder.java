package org.javista.jspunit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigHolder {
	
	private String jspBasePath;
	private String jspName;
	private String webContextPath;
	private Map<String, Object> attributeMap;
	private boolean isAutoGen;
	
	public ConfigHolder() {
	}
	
	public ConfigHolder(String basePath, String name, Map<String, Object> attrMap) {
		jspName = name;
		jspBasePath = basePath;
		attributeMap = attrMap;
		if (attributeMap == null) {
			attributeMap = new HashMap<String, Object>();
		}
	}	

	public void setJspBasePath(String jspBasePath) {
		this.jspBasePath = jspBasePath;
	}

	public String getJspBasePath() {
		return jspBasePath;
	}

	public void setJspName(String jspName) {
		this.jspName = jspName;
	}

	public String getJspName() {
		return jspName;
	}

	public String getUrlMapping() {
		int dotIndex = jspName.indexOf(".");
		if (dotIndex != -1) {
			return "/"+jspName.substring(0, dotIndex);
		}
		return "/"+jspName;
	}

	public void setWebContextPath(String webContextPath) {
		this.webContextPath = webContextPath;
	}

	public String getWebContextPath() {
		return webContextPath;
	}

	public void setAutoGen(boolean isAutoGen) {
		this.isAutoGen = isAutoGen;
	}

	public boolean isAutoGen() {
		return isAutoGen;
	}

	public void setAttributeMap(Map<String, Object> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public Map<String, Object> getAttributeMap() {
		return Collections.unmodifiableMap(attributeMap);
	}

}
