package com.utd.distributed.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

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
	
	private volatile BlockingQueue<Message> messageQueue;
	public Process(int id,Master master,int initialValue,int rounds,int dropCounter,int processCount) {
		this.id = id;
		this.master = master;
		this.maxRound = rounds;
		this.dropMessageCounter = dropCounter;
		this.processCount = processCount;
		this.messageQueue = new LinkedBlockingDeque<>();
		this.val = new ArrayList<>(Collections.nCopies(processCount, -1));
		this.level = new ArrayList<>(Collections.nCopies(processCount, 0));
		this.val.set(this.id, initialValue);
		this.key = -1;
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
					setMessageFromMaster("");
					master.roundCompletionForProcess(id);	
				}else 
	            if (getMessageFromMaster().equals("StartRound")) {
	            	setMessageFromMaster("");
	                if(round < maxRound) {
	                    if (round == 0 && this.id == 0) {
	                    	Random rand = new Random();
	                        key = rand.nextInt(maxRound-1) + 1;
	                    } else if(round !=0) {
                    		processQueue();
                    	}
	                    Message message = new Message(key, val, level);
	                    Thread.sleep(1000);
	                    for (int i = 0,count = 1; i < processCount; i++){
	                    	if(i!=this.id){
	                            sendMessage(message, i,count++);
	                        }
	                    }
	                    setRoundNumber();
	                }
	                else {
	                	processDone = true;
	                	processDecision();
	                }
	                master.roundCompletionForProcess(id);	
	            }
	        }
		}catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("Process :"+id+" Round:"+round+" values:"+val.toString()+" Key:"+key+" Levels:"+level.toString());
		} 
		catch (InterruptedException e) {
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
	    //System.out.println("Message from :"+id+" Receiver :"+receiverId+" Message number :"+messageNumber+" Round no:"+round);
		if(messageNumber%dropMessageCounter != 0) {
			send(m, p[receiverId]);
	    }
	    else
	        System.out.println("Message Number:" + messageNumber +" From process:"+id +" to process:" +receiverId+" was dropped!");
	 }
	 
	 private void send(Message m , Process p){
		 p.putMessage(m);
	 }
	 
	public  void putMessage(Message m){
		if(m == null)
			System.out.println("Y message is null ?");
		messageQueue.add(m);
    }
		
    public synchronized void processQueue() {
       if(!messageQueue.isEmpty()) {
           while (!messageQueue.isEmpty()) {
               Message m;
               try {
					m = messageQueue.take();
	               if(m==null) {
	            	   //System.out.println("Message null for process:"+id);
	            	   return;
	               }else {
	               if(key==-1 && m.key!=-1) {
	                   key = m.key;
	               }
	               for (int i = 0; i < processCount; i++) {
	            	   if ( val.get(i) == -1 && (m.val.get(i)  != -1)) {
	                       val.set(i,m.val.get(i));
	                   }
	                   if (level.get(i) < m.level.get(i)) {
	                       level.set(i, m.level.get(i));
	                   }
	               }
	           }
           
		       level.set(id,Collections.min(level)+1);
		       } catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
           }
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
