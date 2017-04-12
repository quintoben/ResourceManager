package resourceManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FIFOResourceManager extends ResourceManager {

	// indicate the current cycle state
	private boolean isDeadlock;
	// store the next round block list of activity
	private Queue<List<Activity>> tempBlockQueue;
	// store the next round ready list of activity
	private Queue<List<Activity>> tempReadyQueue;

	public FIFOResourceManager(List<List<Activity>> activityList,
			int taskNumber, List<Integer> resourceValue) {
		super(activityList, taskNumber, resourceValue);
		// TODO Auto-generated constructor stub
		isDeadlock = true;
	}

	@Override
	protected void processBlockQueue() {
		// TODO Auto-generated method stub
		detail.append("check block queue\n");
		// loop the current block queue
		while (!blockQueue.isEmpty()) {
			List<Activity> list = blockQueue.poll();
			// get the current activity
			Activity activity = list.get(0);
			list.remove(0);
			String activityType = activity.getActivityType();
			int taskNumber = activity.getTaskNumber();
			int number = activity.getNumber();
			int resourceType = activity.getResourceType();
			Task task = taskList.get(taskNumber - 1);
			if (activityType.equals("request")) {
				// assign success and move to the ready queue
				if (resourceValue.get(resourceType - 1) >= number) {
					task.setCurrentAmount(resourceType,
							task.getCurrentAmount(resourceType) + number);
					// move to next cycle's ready list
					tempReadyQueue.offer(list);
					resourceValue.set(resourceType - 1,
							resourceValue.get(resourceType - 1) - number);
					detail.append("Task " + taskNumber + " request " + number
							+ " success\n");
					task.run();
					isDeadlock = false;
				}
				// stay at block queue
				else {
					list.add(0, activity);
					tempBlockQueue.offer(list);
					detail.append("Task " + taskNumber + " request " + number
							+ " blocked\n");
					task.block();
				}
			}
		}
		blockQueue = tempBlockQueue;
	}

	@Override
	protected void processReadyQueue() {
		// TODO Auto-generated method stub
		detail.append("check ready queue\n");
		// loop the current ready queue
		while (!readyQueue.isEmpty()) {
			List<Activity> list = readyQueue.poll();
			// get the current activity
			Activity activity = list.get(0);
			// remove the activity from the activity list
			list.remove(0);
			String activityType = activity.getActivityType();
			int taskNumber = activity.getTaskNumber();
			int number = activity.getNumber();
			int resourceType = activity.getResourceType();
			Task task = taskList.get(taskNumber - 1);
			// initiate and move to temp queue for the next round
			if (activityType.equals("initiate")) {
				task.setInitialClaim(resourceType, number);
				task.setResourceType(resourceType);
				task.setCurrentAmount(resourceType, 0);
				detail.append("Task " + taskNumber + " initiate\n");
				tempReadyQueue.offer(list);
				task.run();
				isDeadlock = false;
			}
			// the activity type is to request
			else if (activityType.equals("request")) {
				// assign success and move to the temp ready queue for the next
				// round
				if (resourceValue.get(resourceType - 1) >= number) {
					task.setCurrentAmount(resourceType,
							task.getCurrentAmount(resourceType) + number);
					resourceValue.set(resourceType - 1,
							resourceValue.get(resourceType - 1) - number);
					tempReadyQueue.offer(list);
					detail.append("Task " + taskNumber + " request " + number
							+ " success\n");
					task.run();
					isDeadlock = false;
				}
				// recycle the resource and abort
				else {
					list.add(0, activity);
					blockQueue.offer(list);
					detail.append("Task " + taskNumber + " request " + number
							+ " blocked\n");
					task.block();
				}
			}
			// release
			else if (activityType.equals("release")) {
				task.setCurrentAmount(resourceType,
						task.getCurrentAmount(resourceType) - number);
				recycleValue.set(resourceType - 1,
						recycleValue.get(resourceType - 1) + number);
				tempReadyQueue.offer(list);
				detail.append("Task " + taskNumber + " release " + number
						+ " success\n");
				task.run();
				isDeadlock = false;
			}
			// compute
			else if (activityType.equals("compute")) {
				if (resourceType > 1) {
					activity.setResourceType(resourceType - 1);
					list.add(0, activity);
				}
				tempReadyQueue.offer(list);
				detail.append("Task " + taskNumber + " running. "
						+ (resourceType - 1) + " left\n");
				task.run();

			}

		}
		// update the ready queue for the next round
		readyQueue = tempReadyQueue;
		tempReadyQueue = new LinkedList<List<Activity>>();
		// loop the queue to find whether there is any activity type is
		// terminate, if so, terminate
		while (!readyQueue.isEmpty()) {
			List<Activity> list = readyQueue.poll();
			// get the current activity
			Activity activity = list.get(0);
			// remove the activity from the activity list
			list.remove(0);
			String activityType = activity.getActivityType();
			int taskNumber = activity.getTaskNumber();
			int number = activity.getNumber();
			int resourceType = activity.getResourceType();
			Task task = taskList.get(taskNumber - 1);
			// terminate
			if (activityType.equals("terminate")) {
				detail.append("Task " + taskNumber + " terminate\n");
				for (int i = 0; i < task.getResourceType(); i++) {
					recycleValue.set(i,
							recycleValue.get(i) + task.getCurrentAmount(i + 1));
				}
				isDeadlock = false;
			}
			// rejoin the queue
			else {
				list.add(0, activity);
				tempReadyQueue.offer(list);
			}
		}
		readyQueue = tempReadyQueue;
	}

	@Override
	protected boolean loop() {
		// TODO Auto-generated method stub
		if (readyQueue.isEmpty() && blockQueue.isEmpty())
			return false;
		detail.append("During " + cycle + "-" + (++cycle) + "\n");
		isDeadlock = true;
		initiateQueue();
		processBlockQueue();
		processReadyQueue();
		if (isDeadlock) {
			releaseResource(resourceValue.size());
		}
		recycleResource();
		return true;
	}

	// initiate the temp queues
	private void initiateQueue() {
		this.tempBlockQueue = new LinkedList<List<Activity>>();
		this.tempReadyQueue = new LinkedList<List<Activity>>();
	}

	// release resource when deadlock occurs
	private void releaseResource(int resourceType) {
		int[] releaseRes = new int[resourceType];
		while (!blockQueue.isEmpty()) {
			// find the min number
			int min = Integer.MAX_VALUE;
			for (List<Activity> list : blockQueue) {
				Activity activity = list.get(0);
				int number = activity.getTaskNumber();
				min = Math.min(number, min);
			}
			// get the task with the min task number
			Task task = taskList.get(min - 1);
			// recycle its resources
			for (int i = 0; i < resourceType; i++) {
				releaseRes[i] = task.getCurrentAmount(i + 1);
				recycleValue.set(i, recycleValue.get(i) + releaseRes[i]);
			}

			task.setAbortState(true);
			// remove the activity list from the list
			int[] minRequest = new int[resourceType];
			for (int i = 0; i < resourceType; i++) {
				minRequest[i] = Integer.MAX_VALUE;
			}
			Queue<List<Activity>> tempQueue = new LinkedList<List<Activity>>();
			// loop to get the min request needs to satisfy
			for (List<Activity> list : blockQueue) {
				Activity activity = list.get(0);
				int request = activity.getNumber();
				int number = activity.getTaskNumber();
				// get the resource type
				int rt = activity.getResourceType();
				if (number != min) {
					tempQueue.offer(list);
					minRequest[rt - 1] = Math.min(minRequest[rt - 1], request);
				}
			}
			blockQueue = tempQueue;

			boolean result = true;
			for (int i = 0; i < resourceType; i++) {
				if ((recycleValue.get(i) + resourceValue.get(i)) < minRequest[i]
						&& minRequest[i] != Integer.MAX_VALUE) {
					result = false;
				}
			}
			// meet the min request then break, otherwise continue to abort task
			if (result) {
				break;
			}
		}
	}

}
