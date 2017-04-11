package resourceManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FIFOResourceManager extends ResourceManager{
	
	private boolean isDeadlock;
	//store the next round block list of activity
	private Queue<List<Activity>> tempBlockQueue;
	//store the next round ready list of activity
	private Queue<List<Activity>> tempReadyQueue;

	public FIFOResourceManager(List<List<Activity>> activityList,
			int taskNumber, List<Integer> resourceValue) {
		super(activityList, taskNumber, resourceValue);
		// TODO Auto-generated constructor stub
		isDeadlock=true;
	}


	@Override
	protected void processBlockQueue() {
		// TODO Auto-generated method stub
		detail.append("check block queue\n");
		while(!blockQueue.isEmpty()){
			List<Activity> list=blockQueue.poll();
			Activity activity=list.get(0);
			list.remove(0);
			String activityType=activity.getActivityType();
			int taskNumber=activity.getTaskNumber();
			int number=activity.getNumber();
			int resourceType=activity.getResourceType();
			Task task=taskList.get(taskNumber-1);
			if(activityType.equals("request")){
				//assign success and move to the ready queue
				if(resourceValue.get(resourceType-1)>=number){
					task.setCurrentAmount(resourceType,task.getCurrentAmount(resourceType)+number);
					tempReadyQueue.offer(list);
					resourceValue.set(resourceType-1,resourceValue.get(resourceType-1)-number);
					detail.append("Task "+taskNumber+" request "+number+" success\n");
					task.run();
					isDeadlock=false;
				}
				//stay at block queue
//				else if(resourceValue.get(resourceType-1)+recycleValue.get(resourceType-1)>=number){
				else{
					list.add(0,activity);
					tempBlockQueue.offer(list);
					detail.append("Task "+taskNumber+" request "+number+" blocked\n");
					task.block();
				}
				//recycle the resource
//				else{
//					resourceValue.set(resourceType-1, task.getCurrentAmount()+resourceValue.get(resourceType-1));
//					task.setCurrentAmount(0);
//					detail.append("Task "+taskNumber+" request "+number+" fail\n");
//				}
			}
//			else if(activityType.equals("compute")){
//				
//			}
		}
		blockQueue=tempBlockQueue;
	}

	@Override
	protected void processReadyQueue() {
		// TODO Auto-generated method stub
		detail.append("check ready queue\n");
		while(!readyQueue.isEmpty()){
			List<Activity> list=readyQueue.poll();
			//get the current activity
			Activity activity=list.get(0);
			//remove the activity from the activity list
			list.remove(0);
			String activityType=activity.getActivityType();
			int taskNumber=activity.getTaskNumber();
			int number=activity.getNumber();
			int resourceType=activity.getResourceType();
			// System.out.println(resourceType);
			Task task=taskList.get(taskNumber-1);
			//initiate and move to temp queue for the next round
			if(activityType.equals("initiate")){
				task.setInitialClaim(resourceType,number);
				task.setResourceType(resourceType);
				task.setCurrentAmount(resourceType,0);
				detail.append("Task "+taskNumber+" initiate\n");
				tempReadyQueue.offer(list);
				task.run();
				isDeadlock=false;
			}
			else if(activityType.equals("request")){
				//assign success and move to the temp ready queue for the next round
				if(resourceValue.get(resourceType-1)>=number){
					task.setCurrentAmount(resourceType,task.getCurrentAmount(resourceType)+number);
					resourceValue.set(resourceType-1,resourceValue.get(resourceType-1)-number);
					tempReadyQueue.offer(list);
					detail.append("Task "+taskNumber+" request "+number+" success\n");
					task.run();
					isDeadlock=false;
				}
				//move to block queue
//				else if(resourceValue.get(resourceType-1)+recycleValue.get(resourceType-1)>=number){
//					list.add(0,activity);
//					blockQueue.offer(list);
//					detail.append("Task "+taskNumber+" request "+number+" blocked\n");
//					task.block();
//				}
				//recycle the resource and abort
				else{
//					recycleValue.set(resourceType-1, task.getCurrentAmount()+recycleValue.get(resourceType-1));
//					task.setCurrentAmount(0);
//					detail.append("Task "+taskNumber+" request "+number+" fail\n");
//					task.setAbortState(true);
					list.add(0,activity);
					blockQueue.offer(list);
					detail.append("Task "+taskNumber+" request "+number+" blocked\n");
					task.block();
				}
			}
			//release
			else if(activityType.equals("release")){
				task.setCurrentAmount(resourceType,task.getCurrentAmount(resourceType)-number);
				recycleValue.set(resourceType-1, recycleValue.get(resourceType-1)+number);
				tempReadyQueue.offer(list);
				detail.append("Task "+taskNumber+" release "+number+" success\n");
				task.run();
				isDeadlock=false;
			}
			//compute
			else if(activityType.equals("compute")){
				if(resourceType>1){
					activity.setResourceType(resourceType-1);
					list.add(0,activity);
				}
				tempReadyQueue.offer(list);
				detail.append("Task "+taskNumber+" running. "+(resourceType-1)+" left\n");
				task.run();
				
			}
			
		}
		//update the ready queue for the next round
		readyQueue=tempReadyQueue;
		tempReadyQueue=new LinkedList<List<Activity>>();
		while(!readyQueue.isEmpty()){
			List<Activity> list=readyQueue.poll();
			//get the current activity
			Activity activity=list.get(0);
			//remove the activity from the activity list
			list.remove(0);
			String activityType=activity.getActivityType();
			int taskNumber=activity.getTaskNumber();
			int number=activity.getNumber();
			int resourceType=activity.getResourceType();
			// System.out.println(resourceType);
			Task task=taskList.get(taskNumber-1);
			//terminate
			if(activityType.equals("terminate")){
				detail.append("Task "+taskNumber+" terminate\n");
				for(int i=0;i<task.getResourceType();i++){
					recycleValue.set(i, recycleValue.get(i)+task.getCurrentAmount(i+1));
				}
				isDeadlock=false;
			}
			else{
				list.add(0,activity);
				tempReadyQueue.offer(list);
			}
		}
		readyQueue=tempReadyQueue;
	}


	@Override
	protected boolean loop() {
		// TODO Auto-generated method stub
		if(readyQueue.isEmpty()&&blockQueue.isEmpty()) return false;
		detail.append("During "+cycle+"-"+(++cycle)+"\n");
		isDeadlock=true;
		initiateQueue();
		processBlockQueue();
		processReadyQueue();
		if(isDeadlock) {
			releaseResource(resourceValue.size());
		}
		recycleResource();
		return true;
	}

	private void initiateQueue(){
		this.tempBlockQueue=new LinkedList<List<Activity>>();
		this.tempReadyQueue=new LinkedList<List<Activity>>();
	}

	private void releaseResource(int resourceType){
		int[] releaseRes=new int[resourceType];
		while(!blockQueue.isEmpty()){
			//find the min number
			int min=Integer.MAX_VALUE;
			for(List<Activity> list:blockQueue){
				Activity activity=list.get(0);
				int number=activity.getTaskNumber();
				// System.out.println(number);
				min=Math.min(number, min);
			}
			Task task=taskList.get(min-1);
			for(int i=0;i<resourceType;i++){
				releaseRes[i]=task.getCurrentAmount(i+1);
				recycleValue.set(i, recycleValue.get(i)+releaseRes[i]);
				// System.out.println("releaseRes: "+releaseRes[i]);
				// System.out.println("recycleValue : "+recycleValue.get(i));
			}
			
			task.setAbortState(true);
			//remove the activity list from the list
			int[] minRequest=new int[resourceType];
			for(int i=0;i<resourceType;i++){
				minRequest[i]=Integer.MAX_VALUE;
			}
			Queue<List<Activity>> tempQueue=new LinkedList<List<Activity>>();
			for(List<Activity> list:blockQueue){
				Activity activity=list.get(0);
				int request=activity.getNumber();
				int number=activity.getTaskNumber();
				//get the resource type
				int rt=activity.getResourceType();
				if(number!=min){
					tempQueue.offer(list);
					minRequest[rt-1]=Math.min(minRequest[rt-1],request);
				}
			}
			blockQueue=tempQueue;
			
			boolean result=true;
			for(int i=0;i<resourceType;i++){
				// System.out.println(resourceValue.get(i)+" "+minRequest[i]);
				if((recycleValue.get(i)+resourceValue.get(i))<minRequest[i]&&minRequest[i]!=Integer.MAX_VALUE){
					result=false;
				}
			}
			if(result){
				break;
			}
		}
	}


	//update the resource value list
	@Override
	protected void recycleResource() {
		// TODO Auto-generated method stub
		for(int i=0;i<resourceValue.size();i++){
			resourceValue.set(i, resourceValue.get(i)+recycleValue.get(i));
			recycleValue.set(i, 0);
		}
	}

}
