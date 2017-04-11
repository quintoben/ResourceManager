package resourceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BankerResourceManager extends ResourceManager{
	private Queue<List<Activity>> tempBlockQueue;
	private Queue<List<Activity>> tempReadyQueue;

	public BankerResourceManager(List<List<Activity>> activityList,
			int taskNumber, List<Integer> resourceValue) {
		super(activityList, taskNumber, resourceValue);
		// TODO Auto-generated constructor stub
		
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
			//request
			if(activityType.equals("request")&&isStateSafe(activity)){
				//assign success and move to the ready queue
				if(resourceValue.get(resourceType-1)>=number){
					task.setCurrentAmount(resourceType,task.getCurrentAmount(resourceType)+number);
					tempReadyQueue.offer(list);
					resourceValue.set(resourceType-1,resourceValue.get(resourceType-1)-number);
					detail.append("Task "+taskNumber+" request "+number+" success\n");
					task.run();
				}
				//stay at block queue
				else{
					list.add(0,activity);
					tempBlockQueue.offer(list);
					detail.append("Task "+taskNumber+" request "+number+" blocked\n");
					task.block();
				}
			}
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
			}
			else if(activityType.equals("request")&&isStateSafe(activity)){
				//assign success and move to the temp ready queue for the next round
				if(resourceValue.get(resourceType-1)>=number){
					task.setCurrentAmount(resourceType,task.getCurrentAmount(resourceType)+number);
					resourceValue.set(resourceType-1,resourceValue.get(resourceType-1)-number);
					tempReadyQueue.offer(list);
					detail.append("Task "+taskNumber+" request "+number+" success\n");
					task.run();
				}
				//move to block queue
				else{
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
		processBlockQueue();
		processReadyQueue();
//		if(isDeadlock) {
//			for(int i=0;i<resourceValue.size();i++){
//				releaseResource(i);
//			}
//		}
		recycleResource();
		return true;
	}

	@Override
	protected void recycleResource() {
		// TODO Auto-generated method stub
		
	}
	
	private boolean isStateSafe(Activity activity){
		String activityType=activity.getActivityType();
		int taskNumber=activity.getTaskNumber();
		int number=activity.getNumber();
		int resourceType=activity.getResourceType();
		int totalType=resourceValue.size();
		List<List<List<Integer>>> resourceTable=new ArrayList<List<List<Integer>>>();
		/* list --resource type
		 * 		 list -- task
		 * 				list -- [1.claim,2.current resource,3.max require resource]
		*/
		for(int i=0;i<totalType;i++){
			resourceTable.add(new ArrayList<List<Integer>>());
			for(int j=0;i<taskList.size();i++){
				resourceTable.get(i).add(new ArrayList<Integer>());
				Task task=taskList.get(j);
				int claim=task.getInitialClaim(i+1);
				int current=task.getCurrentAmount(i+1);
				resourceTable.get(i).get(j).add(claim);
				resourceTable.get(i).get(j).add(current);
				resourceTable.get(i).get(j).add(claim-current);
			}
		}
		
		
		return  false;
	}
	
//	private boolean helper(){
//		
//	}

}
