package resourceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ResourceManage {
	public static void main(String[] args){
		List<Integer> params=new ArrayList<Integer>();
		
		List<Integer> RV=new ArrayList<Integer>();
		List<List<Activity>> activityList =read(args[0],params,RV);
		int T=params.get(0);
		int R=params.get(1);
		printActivityList(activityList);
		FIFOResourceManager fifoRM=new FIFOResourceManager(activityList,T,RV);
		fifoRM.run();
		String detail=fifoRM.getDetail().toString();
		String result=fifoRM.getResult().toString();
		System.out.println(detail);
		System.out.println();
		System.out.println(result);
		
	}
	
	//read the input file
	public static List<List<Activity>> read(String path,List<Integer> params,List<Integer> RV){
		StringBuffer sb = new StringBuffer();
		try {
			File filename = new File(path);
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line + " ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] string = sb.toString().replace('\n', ' ').trim().split("\\s+");
		int T = Integer.parseInt(string[0]);
		int R = Integer.parseInt(string[1]);
		params.add(T);
		params.add(R);
		for(int i=0;i<R;i++){
			RV.add(Integer.parseInt(string[2+i]));
		}
		
		//list that hold the cycle sequences
		List<List<Activity>> activityList = new ArrayList<List<Activity>>();
		for(int i=1;i<=T;i++){
			activityList.add(new ArrayList<Activity>(i));
		}
		for (int i = R+2; i < string.length; i+=4) {
			int taskNumber = Integer.parseInt(string[i+1]);
			Activity activity=new Activity(taskNumber);
			activity.setActivityType(string[i]);
			activity.setResourceType(Integer.parseInt(string[i+2]));
			activity.setNumber(Integer.parseInt(string[i+3]));
			List<Activity> cycleList=activityList.get(taskNumber-1);
			cycleList.add(activity);
			activityList.set(taskNumber-1, cycleList);
		}
		return activityList;
	}
	
	public static void printActivityList(List<List<Activity>> activityList){
		for(List<Activity> cycleList:activityList){
			for(Activity activity:cycleList){
				System.out.println(activity.getActivityType()+" "
						+activity.getTaskNumber()+" "
						+activity.getResourceType()+" "
						+activity.getNumber());
			}
		}
	}
	
	public static void FIFO(List<List<Activity>> activityList){
		Queue<Integer> blockQueue=new LinkedList<Integer>();
		while(!blockQueue.isEmpty()){
			
		}
		
	}
}
