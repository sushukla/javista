package org.javista.jspunit.ext;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jetty.server.Server;
import org.openqa.selenium.WebDriver;

/**
 * Utility to start server in a new thread so that join() call does not block
 * @author sushukla
 *
 */
public class ServerStarter {
	
	static ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
	
	public static void startServer(final Server server) {
		executor.submit(new Callable<Server>() {

			@Override
			public Server call() throws Exception {
				if (server != null) {
					try {
						server.start();
						server.join();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return server;
			}
			
		});
	}
	
	public static void browserTimeout(final WebDriver driver, long timeout) {

		TimerTask testTimer = new TimerTask() {
			@Override
			public void run() {
				try {
					System.out.println("CLOSING DRIVER "+driver);
					driver.close();
				} catch (Exception e) {
				}
			}			
		};
		Timer timer = new Timer();
		timer.schedule(testTimer, timeout, timeout);
	}

}
