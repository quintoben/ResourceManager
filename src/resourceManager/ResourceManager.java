package resourceManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class ResourceManager {
	// list of activity
	protected List<List<Activity>> activityList;
	// cycle number
	protected int cycle;
	// block queue
	protected Queue<List<Activity>> blockQueue;
	// ready queue
	protected Queue<List<Activity>> readyQueue;
	// store the result
	protected StringBuilder result;
	// number of tasks
	protected int taskNumber;
	// current usable resource value, index:resourceType-1
	protected List<Integer> resourceValue;
	// recycled resource in current cycle
	protected List<Integer> recycleValue;
	// list of task
	protected List<Task> taskList;
	// store detail
	protected StringBuilder detail;
	// calculate total run time
	protected double totalTime;
	// calculate total wait time
	protected double totalWait;

	public ResourceManager(List<List<Activity>> activityList, int taskNumber,
			List<Integer> resourceValue) {
		this.activityList = activityList;
		this.cycle = 0;
		this.blockQueue = new LinkedList<List<Activity>>();
		this.readyQueue = new LinkedList<List<Activity>>();
		for (List<Activity> list : activityList) {
			readyQueue.offer(list);
		}
		this.taskNumber = taskNumber;
		this.resourceValue = resourceValue;
		taskList = new ArrayList<Task>();
		recycleValue = new ArrayList<Integer>();
		// initiate the task list
		for (int i = 0; i < taskNumber; i++) {
			taskList.add(new Task(i + 1, resourceValue.size()));
		}
		// initiate the recycle value list
		for (int i = 0; i < resourceValue.size(); i++) {
			recycleValue.add(0);
		}
		this.detail = new StringBuilder();
		this.result = new StringBuilder();
		this.totalTime = 0;
		this.totalWait = 0;
	}

	// start the manager
	public void run() {
		while (loop());
		for (Task task : taskList) {
			if (!task.getAbortState()) {
				result.append("Task " + task.getId() + "\t"
						+ Math.round(task.getTime()) + "\t"
						+ Math.round(task.getWait()) + "\t"
						+ Math.round((task.getWait() / task.getTime()) * 100)
						+ "%\n");
				this.totalTime += task.getTime();
				this.totalWait += task.getWait();
			} else {
				result.append("Task " + task.getId() + "\t" + "aborted\t\t\n");
			}
		}
		result.append("total \t" + Math.round(this.totalTime) + "\t"
				+ Math.round(this.totalWait) + "\t"
				+ Math.round((this.totalWait / this.totalTime) * 100) + "%\n");
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

	// continue to run until the result of this function becomes false
	protected abstract boolean loop();

	// recycle the resources back to resource value list
	protected void recycleResource() {
		for (int i = 0; i < resourceValue.size(); i++) {
			resourceValue.set(i, resourceValue.get(i) + recycleValue.get(i));
			recycleValue.set(i, 0);
		}
	}

}
