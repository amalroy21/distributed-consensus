package com.utd.distributed.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.utd.distributed.master.Master;
import com.utd.distributed.util.Message;

public class Process implements Runnable{

	private Master master;
	public int id;
    private Process[] p;
    private volatile String messageFromMaster = "";
    private int round = 0;
	private boolean processDone = false;
	private int maxRound ;
	private int key;
	public ArrayList<Integer> val;
    public ArrayList<Integer> level;
	private int processCount;
	private int dropMessageCounter;
	private int messageNumber;
	private int decision;
	private boolean decisionDone;
	
	private volatile Queue<Message> messageQueue = new LinkedList<>();
	public Process(int id,Master master,int initialValue,int rounds,int dropCounter,int processCount) {
		this.id = id;
		this.master = master;
		this.maxRound = rounds;
		this.dropMessageCounter = dropCounter;
		this.processCount = processCount;
		this.val = new ArrayList<>(Collections.nCopies(processCount, -1));
		this.level = new ArrayList<>(Collections.nCopies(processCount, 0));
		this.val.set(this.id, initialValue);
		this.key = -1;
		if(id == 0) {
			Random rand = new Random();
            this.key = rand.nextInt(maxRound-1) + 1;
		}
	}
    
	public void setProcessNeighbors(Process p[]) {
		this.p = p;
	}

	@Override
	public void run() {

		master.roundCompletionForProcess(id);
		try {
			while (!processDone) {
				if(getMessageFromMaster().equals("Initiate")){
					//System.out.println("Process "+id+" Initiated");
					setMessageFromMaster("");
					master.roundCompletionForProcess(id);	
				}else 
	            if (getMessageFromMaster().equals("StartRound")) {
	            	setMessageFromMaster("");
	            	//System.out.println("Process "+id+" Entered Round"+round);
	                if(round < maxRound) {
	                    if (round == 0 && this.id == 0) {
	                        Random rand = new Random();
	                        //this.key = rand.nextInt(maxRound-1) + 1;
	                        //this.key = 6;
	                    } else 
	                    	if(round !=0) {
	                    		this.processQueue();
	                    	}
	                
	                    round++;
	                    Message message = new Message(this.key, this.val, this.level);
	                    for (int i = 0,count = 1; i < processCount; i++){
	                    	if(i!=this.id){
	                        	Thread.sleep(1000);
	                            sendMessage(message, i,count++);
	                        }
	                    }
	                    
	                }
	                else {
	                	processDone = true;
	                	this.processDecision();
	                }
	                master.roundCompletionForProcess(id);	
	            }
	        }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setRoundNumber(){
		this.round++;
	}
	
	public String getMessageFromMaster(){
		return messageFromMaster;
	}
	
	
	public void setMessageFromMaster(String messageFromMaster) {
		this.messageFromMaster = messageFromMaster;
	}
	
	 public synchronized void sendMessage(Message m,int receiverId,int count){
		messageNumber = ((round)*(processCount)*(processCount-1))+((id*(processCount-1))+count);
	    //System.out.println("Message from :"+id+" Receiver :"+receiverId+" Message number :"+messageNumber);
		if(messageNumber%dropMessageCounter != 0) {
	        send(m, p[receiverId]);
	    }
	    else
	        System.out.println("Message Number:" + messageNumber +" From process:"+id +" to process:" +receiverId+" was dropped!");
	 }
	 
	 private void send(Message m , Process p){
		 p.putMessage(m);
	 }
	 
	public void putMessage(Message m){
		 messageQueue.add(m);
    }
		
    public synchronized void processQueue() {
       if(!messageQueue.isEmpty()) {
           while (!messageQueue.isEmpty()) {
               Message m = messageQueue.poll();
               if(key==-1 && m.key!=-1) {
                   this.key = m.key;
               }
               for (int i = 0; i < processCount; i++) {
                   if (val.get(i) == -1 && (m.val.get(i)  != -1)) {
                       val.set(i,m.val.get(i));
                   }
                   if (level.get(i) < m.level.get(i)) {
                       level.set(i, m.level.get(i));
                   }
               }
           }
       level.set(id,Collections.min(level)+1);
       }
	}
    
	public void processDecision() {
		int bitOp = 1;
		for(Integer v:val) {
			bitOp = bitOp & v; 
		}
	    decision = (key != -1 && level.get(id) >= key && bitOp == 1) ? 1 : 0;
	    decisionDone = true;
	    System.out.println("Process "+id + " decision is "+decision+" and values are :"+val.toString() + " and Levels are :"+level.toString() + " and key is:"+key);
	}
	
	public boolean getDecisionStatus() {
		return decisionDone;
	}
	
	public int getDecision() {
		return decision;
	}
}
