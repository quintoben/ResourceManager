package resourceManager;

import java.util.List;

public class BankerResourceManager extends ResourceManager{

	public BankerResourceManager(List<List<Activity>> activityList,
			int taskNumber, List<Integer> resourceValue) {
		super(activityList, taskNumber, resourceValue);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processBlockQueue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processReadyQueue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean loop() {
		// TODO Auto-generated method stub
		if(readyQueue.isEmpty()&&blockQueue.isEmpty()) return false;
		detail.append("During "+cycle+"-"+(++cycle)+"\n");
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

	@Override
	protected void recycleResource() {
		// TODO Auto-generated method stub
		
	}
	
	private boolean isStateSafe(){
		return  false;
	}

}
