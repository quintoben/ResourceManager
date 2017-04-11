package resourceManager;

public class Activity {
	//Activity Type
	private String activityType;
	
	//Task Number
	private int taskNumber;
	
	//Resource Type or Circle Number
	private int resourceType;
	
	//Number
	private int number;
	
	public Activity(int taskNumber){
		this.taskNumber=taskNumber;
	}
	
	public Activity(String activityType,int taskNumber,int resourceType,int number){
		this.activityType=activityType;
		this.taskNumber=taskNumber;
		this.resourceType=resourceType;
		this.number=number;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public int getTaskNumber() {
		return taskNumber;
	}

	public void setTaskNumber(int taskNumber) {
		this.taskNumber = taskNumber;
	}

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	
}
