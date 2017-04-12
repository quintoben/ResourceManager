package resourceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceManage {
	public static void main(String[] args) {
		// list contains T and R
		List<Integer> params = new ArrayList<Integer>();
		// resource value
		List<Integer> RV = new ArrayList<Integer>();
		List<List<Activity>> activityList = read(args[0], params, RV);
		// get T and R
		int T = params.get(0);
		int R = params.get(1);
		// create a new fifo resource manager
		FIFOResourceManager fifoRM = new FIFOResourceManager(activityList, T,
				RV);
		// use this manager to run the input
		fifoRM.run();
		// get the result
		String fifoResult = fifoRM.getResult().toString();
		activityList = read(args[0], params, RV);
		// create a new banker resource manager
		BankerResourceManager bankerRM = new BankerResourceManager(
				activityList, T, RV);
		// use this manager to run the input
		bankerRM.run();
		// get the result
		String bankerResult = bankerRM.getResult().toString();
		StringBuilder result = new StringBuilder();
		// merge all results
		result.append("\t" + bankerRM.getError().toString());
		result.append("\t\tFIFO\t\t\t\tBANKER'S\n");
		String[] fifoLine = fifoResult.split("\n");
		String[] bankerLine = bankerResult.split("\n");
		for (int i = 0; i < fifoLine.length; i++) {
			result.append("\t" + fifoLine[i] + "\t" + bankerLine[i] + "\n");
		}
		System.out.println(result.toString());
	}

	// read the input file
	public static List<List<Activity>> read(String path, List<Integer> params,
			List<Integer> RV) {
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
		for (int i = 0; i < R; i++) {
			RV.add(Integer.parseInt(string[2 + i]));
		}

		// list that hold the cycle sequences
		List<List<Activity>> activityList = new ArrayList<List<Activity>>();
		for (int i = 1; i <= T; i++) {
			activityList.add(new ArrayList<Activity>(i));
		}
		for (int i = R + 2; i < string.length; i += 4) {
			int taskNumber = Integer.parseInt(string[i + 1]);
			Activity activity = new Activity(taskNumber);
			activity.setActivityType(string[i]);
			activity.setResourceType(Integer.parseInt(string[i + 2]));
			activity.setNumber(Integer.parseInt(string[i + 3]));
			List<Activity> cycleList = activityList.get(taskNumber - 1);
			cycleList.add(activity);
			activityList.set(taskNumber - 1, cycleList);
		}
		return activityList;
	}

	// print the input, used for debug
	public static void printActivityList(List<List<Activity>> activityList) {
		for (List<Activity> cycleList : activityList) {
			for (Activity activity : cycleList) {
				System.out.println(activity.getActivityType() + " "
						+ activity.getTaskNumber() + " "
						+ activity.getResourceType() + " "
						+ activity.getNumber());
			}
		}
	}

}
