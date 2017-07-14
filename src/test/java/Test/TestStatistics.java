package Test;

import httpServer.booter;

public class TestStatistics {
	public static void main(String[] args) {
		booter booter = new booter();
		 try {
		 System.out.println("GrapeStatistics!");
		 System.setProperty("AppName", "GrapeStatistics");
		 booter.start(1002);
		} catch (Exception e) {
		}
	}
}
