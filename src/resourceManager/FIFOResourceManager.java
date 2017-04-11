package resourceManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FIFOResourceManager extends ResourceManager{
	
	private boolean isDeadlock;

	public FIFOResourceManager(List<List<Activity>> activityList,
			int taskNumber, List<Integer> resourceValue) {
		super(activityList, taskNumber, resourceValue);
		// TODO Auto-generated constructor stub
		isDeadlock=true;
	}


	@Override
	protected void processBlockQueue() {
		// TODO Auto-generated method stub
		Queue<List<Activity>> tempBlockQueue =new LinkedList<List<Activity>>();
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
					readyQueue.offer(list);
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
		Queue<List<Activity>> tempReadyQueue =new LinkedList<List<Activity>>();
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
			Task task=taskList.get(taskNumber-1);
			//initiate and move to temp queue for the next round
			if(activityType.equals("initiate")){
				task.setInitialClaim(resourceType,number);
				task.setResourceType(resourceType);
				task.setCurrentAmount(resourceType,0);
				detail.append("Task "+taskNumber+" initiate\n");
				tempReadyQueue.offer(list);
				task.run();
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
				
			}
			//terminate
			else if(activityType.equals("terminate")){
				detail.append("Task "+taskNumber+" terminate\n");
				recycleValue.set(resourceType-1, recycleValue.get(resourceType-1)+task.getCurrentAmount(resourceType));
				isDeadlock=false;
			}
		}
		//update the ready queue for the next round
		readyQueue=tempReadyQueue;
		//update the resource value list
		
	}


	@Override
	protected boolean loop() {
		// TODO Auto-generated method stub
		if(readyQueue.isEmpty()&&blockQueue.isEmpty()) return false;
		detail.append("During "+cycle+"-"+(++cycle)+"\n");
		isDeadlock=true;
		processBlockQueue();
		processReadyQueue();
		if(isDeadlock) {
			for(int i=0;i<resourceValue.size();i++){
				releaseResource(i);
			}
		}
		recycleResource();
		return true;
	}

	private void releaseResource(int resourceType){
		int releaseRes=0;
		while(releaseRes==0){
			//find the min number
			int min=Integer.MAX_VALUE;
			for(List<Activity> list:blockQueue){
				Activity activity=list.get(0);
				int number=activity.getTaskNumber();
				min=Math.min(number, min);
			}
			Task task=taskList.get(min-1);
			releaseRes=task.getCurrentAmount(resourceType);
			recycleValue.set(resourceType-1, recycleValue.get(resourceType-1)+releaseRes);
			task.setAbortState(true);
			//remove the activity list from the list
			Queue<List<Activity>> tempQueue=new LinkedList<List<Activity>>();
			for(List<Activity> list:blockQueue){
				Activity activity=list.get(0);
				int number=activity.getTaskNumber();
				if(number!=min){
					tempQueue.offer(list);
				}
			}
			blockQueue=tempQueue;
		}
	}


	@Override
	protected void recycleResource() {
		// TODO Auto-generated method stub
		for(int i=0;i<resourceValue.size();i++){
			resourceValue.set(i, resourceValue.get(i)+recycleValue.get(i));
			recycleValue.set(i, 0);
		}
	}

}
