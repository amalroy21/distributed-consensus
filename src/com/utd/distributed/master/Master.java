package com.utd.distributed.master;

import java.util.HashMap;
import com.utd.distributed.process.Process;

public class Master implements Runnable{

   private int ProcessCount;
   private Process[] p;
   private volatile HashMap<Integer,Boolean> roundDetails = new HashMap<>();
   private boolean masterDone = false;
   private boolean roundsCompleted = false;
   private volatile int messageCounter = 0;
   public Master(int ProcessCount){
		this.ProcessCount = ProcessCount;
		for(int i = 0; i < ProcessCount; i++){
			roundDetails.put(i, false);
		}
	}
   
   // Passing the Reference of the Process
   public void setProcesses(Process[] p){
		this.p = p;
		for(int i = 0; i < ProcessCount; i++){
			p[i].setProcessNeighbors(p);
		}
   }
   
   @Override
   public void run() {

	   //System.out.println("Master Process has started");
		
		while (!roundDone()) {
			// Waiting till all the Processes have started
		}
		resetRoundDetails();
		initiateProcesses();
		while (!roundDone()) {
			//Initiating the messages from all the Processes
		}
		resetRoundDetails();
		startRound();
		while(!masterDone){
			while (!roundDone()) {
				// Waiting till all the Processes complete one round
			}
			while(!roundsCompleted){
				resetRoundDetails();
				startRound();
				while(!roundDone()){
					// Waiting till all the Processes complete one round
				}
				checkDecisionDone();
			}
			//resetRoundDetails();
			
			displayResult();
			stopMasterProcess();
			masterDone = true;
		}
	}
	
   public void setRoundsCompleted() {
	   this.roundsCompleted = true;
   }
   
	// Sending Terminate message to all the Processes
	public void stopMasterProcess(){
		for(int i = 0; i < ProcessCount; i++){
			p[i].setMessageFromMaster("MasterDone");
		}
	}
	
	// Collecting round completion information from each Process
	public synchronized void roundCompletionForProcess(int id){
		roundDetails.put(id, true);
	}
	
	// Constructing and printing the Minimum Spanning Tree
	public void displayResult(){
		for(Process process : p) {
			System.out.println("Process "+ process.id + "and Decision " + process.getDecision() +" Values =" + process.val.toString());
		}
		
	}
	
	public synchronized int getMessageCounter() {
		this.messageCounter++;
		return messageCounter;
	}
	
	// To start next round
	private void startRound() {
		for (int i = 0; i < ProcessCount; i++) {
			p[i].setMessageFromMaster("StartRound");
		}
	}
	
	// To check the completion of the Round
	private boolean checkDecisionDone(){
		for(Process process : p){
			if(!process.getDecisionStatus()){
				return false;
			}
		}
		return true;
	}
	
	// To start next round
	private void initiateProcesses() {
		for (int i = 0; i < ProcessCount; i++) {
			p[i].setMessageFromMaster("Initiate");
		}
	}
	
	// Reset the Round confirmation after each round
	private void resetRoundDetails(){
		for(int i = 0; i < ProcessCount; i++){
			roundDetails.put(i, false);
		}
	}
	
	// To check the completion of the Round
	private boolean roundDone(){
		for(boolean b : roundDetails.values()){
			if(!b){
				return false;
			}
		}
		return true;
	}
}
