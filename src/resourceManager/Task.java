package resourceManager;

public class Task {
	private int id;
	private int resourceType;
	private int[] currentAmount;
	private int[] initialClaim;
	private double time;
	private double wait;
	private double percentage;
	private boolean abortState;
	
	public Task(int id,int resourceType){
		this.id=id;
		this.time=0;
		this.wait=0;
		this.abortState=false;
		this.resourceType=resourceType;
		this.currentAmount=new int[resourceType];
		this.initialClaim=new int[resourceType];
	}
	
	public Task(int id,int resourceType,int[] currentAmount,int[] initialClaim){
		this.id=id;
		this.resourceType=resourceType;
		this.currentAmount=currentAmount;
		this.initialClaim=initialClaim;
		this.time=0;
		this.wait=0;
	}
	
	public void run(){
		this.time++;
	}
	
	public void block(){
		this.time++;
		this.wait++;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

	public int getCurrentAmount(int resourceType) {
		return this.currentAmount[resourceType-1];
	}

	public void setCurrentAmount(int resourceType,int currentAmount) {
		this.currentAmount[resourceType-1] = currentAmount;
	}

	public int getInitialClaim(int resourceType) {
		return initialClaim[resourceType-1];
	}

	public void setInitialClaim(int resourceType,int initialClaim) {
		this.initialClaim[resourceType-1] = initialClaim;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getWait() {
		return wait;
	}

	public void setWait(double wait) {
		this.wait = wait;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	public boolean getAbortState() {
		return abortState;
	}

	public void setAbortState(boolean abortState) {
		this.abortState = abortState;
	}
	
}
