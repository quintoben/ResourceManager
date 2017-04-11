package resourceManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class ResourceManager {
	protected List<List<Activity>> activityList;
	protected int cycle;
	protected Queue<List<Activity>> blockQueue;
	protected Queue<List<Activity>> readyQueue;
	protected StringBuilder result;
	protected int taskNumber;
	protected List<Integer> resourceValue;
	protected List<Integer> recycleValue;
	protected List<Task> taskList;
	protected StringBuilder detail;
	protected double totalTime;
	protected double totalWait;
	
	
	
	public ResourceManager(List<List<Activity>> activityList,int taskNumber,List<Integer> resourceValue){
		this.activityList=activityList;
		this.cycle=0;
		this.blockQueue=new LinkedList<List<Activity>>();
		this.readyQueue=new LinkedList<List<Activity>>();
		for(List<Activity> list:activityList){
			readyQueue.offer(list);
		}
		this.taskNumber=taskNumber;
		this.resourceValue=resourceValue;
		taskList=new ArrayList<Task>();
		recycleValue=new ArrayList<Integer>();
		//initiate the task list
		for(int i=0;i<taskNumber;i++){
			taskList.add(new Task(i+1,resourceValue.size()));
		}
		//initiate the recycle value list
		for(int i=0;i<resourceValue.size();i++){
			recycleValue.add(0);
		}
		this.detail=new StringBuilder();
		this.result=new StringBuilder();
		this.totalTime=0;
		this.totalWait=0;
	}
	
	public void run(){
		while(loop());
		for(Task task:taskList){
			if(!task.getAbortState()){
				result.append("Task "+task.getId()
						+"\t"+Math.round(task.getTime())
						+"\t"+Math.round(task.getWait())
						+"\t"+Math.round((task.getWait()/task.getTime())*100)+"%\n");
				this.totalTime+=task.getTime();
				this.totalWait+=task.getWait();
			}
			else{
				result.append("Task "+task.getId()+"\t"+"aborted\n");
			}
		}
		result.append("total \t"+Math.round(this.totalTime)
				+"\t"+Math.round(this.totalWait)
				+"\t"+Math.round((this.totalWait/this.totalTime)*100)+"%\n");
	}
	
	public StringBuilder getResult() {
		return result;
	}

	public void setResult(StringBuilder result) {
		this.result = result;
	}

	public StringBuilder getDetail() {
		return detail;
	}

	public void setDetail(StringBuilder detail) {
		this.detail = detail;
	}

	protected abstract void processBlockQueue();
	protected abstract void processReadyQueue();
	
//	protected boolean loop(){
//		if(readyQueue.isEmpty()&&blockQueue.isEmpty()) return false;
//		detail.append("During "+cycle+"-"+(++cycle)+"\n");
//		processBlockQueue();
//		processReadyQueue();
//		
//		return true;
//	}
	
	protected abstract boolean loop();
	protected abstract void recycleResource();
}
