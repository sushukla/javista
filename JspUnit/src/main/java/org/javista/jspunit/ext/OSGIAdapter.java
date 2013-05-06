package org.javista.jspunit.ext;



public class OSGIAdapter {

    /*public static OSGIAdapter instance = new OSGIAdapter();

    public static OSGIAdapter getInstance() {
        return instance;
    }
    
    public void addSearchPathEntriesFromOSGIManifests() throws IOException {
        Enumeration<URL> manifestUrls = null;
        
        ClassLoader cl = OSGIAdapter.class.getClassLoader();

        try {
            manifestUrls = cl.getResources("META-INF/MANIFEST.MF");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        if (manifestUrls != null) {
            ResourceManager resourceManager = ResourceManager.getInstance();
            
            while(manifestUrls.hasMoreElements()) {
                URL manifestUrl = manifestUrls.nextElement();
                InputStream in = null;
                try {
                	in = manifestUrl.openStream();
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
                Manifest manifest = null;
                try {
                    manifest = new Manifest(in);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to get input stream reader for manifest with URL \"" + manifestUrl + "\". Exception: " + e, e);
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                }

                if (manifest != null) {
                    
                    Attributes attributes = manifest.getMainAttributes();
                    if (attributes != null) {
                        String raptorSearchPath = attributes.getValue("X-Raptor-Resource-Search-Path");//manifest.getHeader("X-Raptor-Resource-Search-Path");
                        if (raptorSearchPath != null) {
                            String[] parts = raptorSearchPath.split("\\s*,\\s*");
                            for (String basePath : parts) {
                                resourceManager.addClasspathSearchPathEntry(OSGIAdapter.class, basePath);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            OSGIAdapter.getInstance().addSearchPathEntriesFromOSGIManifests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
