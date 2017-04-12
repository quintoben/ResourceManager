package resourceManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BankerResourceManager extends ResourceManager {
	private Queue<List<Activity>> tempBlockQueue;
	private Queue<List<Activity>> tempReadyQueue;
	private StringBuilder error;

	public BankerResourceManager(List<List<Activity>> activityList,
			int taskNumber, List<Integer> resourceValue) {
		super(activityList, taskNumber, resourceValue);
		// TODO Auto-generated constructor stub
		error = new StringBuilder();
	}

	@Override
	protected void processBlockQueue() {
		// TODO Auto-generated method stub
		detail.append("check block queue\n");
		while (!blockQueue.isEmpty()) {
			List<Activity> list = blockQueue.poll();
			Activity activity = list.get(0);
			list.remove(0);
			String activityType = activity.getActivityType();
			int taskNumber = activity.getTaskNumber();
			int number = activity.getNumber();
			int resourceType = activity.getResourceType();
			Task task = taskList.get(taskNumber - 1);
			// request
			if (activityType.equals("request")) {
				// state safe and move to the ready queue
				if (isStateSafe(activity)) {
					task.setCurrentAmount(resourceType,
							task.getCurrentAmount(resourceType) + number);
					tempReadyQueue.offer(list);
					resourceValue.set(resourceType - 1,
							resourceValue.get(resourceType - 1) - number);
					detail.append("Task " + taskNumber + " request " + number
							+ " success\n");
					task.run();
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
				if (number > resourceValue.get(resourceType - 1)) {
					task.setAbortState(true);
					error.append("Banker aborts task " + taskNumber
							+ " before run begins:\n\t\tclaim for resourse "
							+ resourceType + " (" + number
							+ ") exceeds number of units present ("
							+ resourceValue.get(resourceType - 1) + ")\n");
				} else {
					task.setInitialClaim(resourceType, number);
					task.setResourceType(resourceType);
					task.setCurrentAmount(resourceType, 0);
					detail.append("Task " + taskNumber + " initiate\n");
					tempReadyQueue.offer(list);
					task.run();
				}
			} else if (activityType.equals("request")) {
				if (number + task.getCurrentAmount(resourceType) > task
						.getInitialClaim(resourceType)) {
					task.setAbortState(true);
					error.append("During cycle " + (cycle - 1) + "-" + cycle
							+ " of Banker's algorithms\n\t\tTask " + taskNumber
							+ "'s request exceeds its claim; aborted; "
							+ task.getCurrentAmount(resourceType)
							+ " units available next cycle\n");
					// recycle the resource
					recycleValue.set(
							resourceType - 1,
							recycleValue.get(resourceType - 1)
									+ task.getCurrentAmount(resourceType));
				}
				// state safe and move to the temp ready queue for the next
				// round
				else if (isStateSafe(activity)) {
					task.setCurrentAmount(resourceType,
							task.getCurrentAmount(resourceType) + number);
					resourceValue.set(resourceType - 1,
							resourceValue.get(resourceType - 1) - number);
					tempReadyQueue.offer(list);
					detail.append("Task " + taskNumber + " request " + number
							+ " success\n");
					task.run();
				}
				// move to block queue
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
			} else {
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
		initiateQueue();
		processBlockQueue();
		processReadyQueue();
		recycleResource();
		return true;
	}

	// initiate the temp queues
	private void initiateQueue() {
		this.tempBlockQueue = new LinkedList<List<Activity>>();
		this.tempReadyQueue = new LinkedList<List<Activity>>();
	}

	// detect whether the state is safe
	private boolean isStateSafe(Activity activity) {

		int taskNumber = activity.getTaskNumber();
		int number = activity.getNumber();
		int resourceType = activity.getResourceType();
		int totalType = resourceValue.size();

		// define the resource table
		// row:task number
		// column:[initial claim,current resource,max request].*
		List<List<Integer>> resourceTable = new ArrayList<List<Integer>>();
		for (int i = 0; i < taskList.size(); i++) {
			resourceTable.add(new ArrayList<Integer>());
			Task task = taskList.get(i);
			for (int j = 0; j < totalType; j++) {
				int claim = task.getInitialClaim(j + 1);
				int current = task.getCurrentAmount(j + 1);
				resourceTable.get(i).add(claim);
				resourceTable.get(i).add(current);
				resourceTable.get(i).add(claim - current);
			}
		}

		// pretend to satisfy the request
		int current = resourceTable.get(taskNumber - 1).get(
				1 + (resourceType - 1) * 3);
		int max = resourceTable.get(taskNumber - 1).get(
				2 + (resourceType - 1) * 3);
		resourceTable.get(taskNumber - 1).set(1 + (resourceType - 1) * 3,
				current + number);
		resourceTable.get(taskNumber - 1).set(2 + (resourceType - 1) * 3,
				max - number);
		// define a list that contains the resource left
		List<Integer> resourceLeft = new ArrayList<Integer>(resourceValue);
		resourceLeft.set(resourceType - 1, resourceLeft.get(resourceType - 1)
				- number);
		// result of whether the state is safe
		boolean result = helper(resourceTable, resourceLeft);
		return result;
	}

	// use dfs to detect whether all the tasks can finish
	private boolean helper(List<List<Integer>> list, List<Integer> left) {
		if (list.size() == 0)
			return true;
		for (int i = 0; i < list.size(); i++) {
			boolean result = true;
			for (int j = 0; j < left.size(); j++) {
				if (list.get(i).get(j * 3 + 2) > left.get(j)) {
					result &= false;
				}
			}
			if (result) {
				List<Integer> temp = new ArrayList<Integer>(list.get(i));
				list.remove(i);
				for (int j = 0; j < left.size(); j++) {
					left.set(j, left.get(j) + temp.get(j * 3 + 1));
				}
				if (helper(list, left) == true)
					return true;
				for (int j = 0; j < left.size(); j++) {
					left.set(j, left.get(j) - temp.get(j * 3 + 1));
				}
				list.add(i, temp);
			}
		}
		return false;
	}

	public StringBuilder getError() {
		return error;
	}

	public void setError(StringBuilder error) {
		this.error = error;
	}
}
