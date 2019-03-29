/**
 *
 * Main.java - Entry point for Distributed Algorithm for SynchBFS
 *
 *
 */

package com.utd.distributed;

import com.utd.distributed.master.Master;
import com.utd.distributed.process.Process;
import com.utd.distributed.util.ReadPropertyFile;

import java.util.Properties;

public class Main {
	
	public static Process[] p;
	public static int processCount;
	
    public static void main(String[] args) {

        try {
            String filePath = "config.properties";
            Properties prop = ReadPropertyFile.readProperties(filePath);
            
            System.out.println("-------Distributed Consensus Algorithm-------");
            processCount = Integer.parseInt(prop.getProperty("processCount"));
            int dropCounter = Integer.parseInt(prop.getProperty("dropCounter"));
            int[] values = buildList(prop.getProperty("values"));
            int rounds = Integer.parseInt(prop.getProperty("rounds"));
            p = new Process[processCount];
			Master master = new Master(processCount);
			for(int i = 0; i < processCount; i++){
				p[i] = new Process(i,master,values[i],rounds,dropCounter,processCount);
			}
			
			master.setProcesses(p);
			Thread t = new Thread(master);
			t.start();
			for(int i = 0; i < processCount; i++){
				Thread tempThread = new Thread(p[i]);
				tempThread.start();
			}
        }
        catch (Exception ex)    {
            ex.printStackTrace();
        }
    }
    
	public static int[] buildList(String list) {
			
		String[] strList = list.split(",");
		int[] arrayList = new int[strList.length];
		int i = 0;
		while(i<strList.length) {
			arrayList[i] = Integer.parseInt(strList[i++]);
		}
		return arrayList;
	}
}
