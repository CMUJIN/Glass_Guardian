package com.jinhs.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

public class SuspiciousMotionDetectionHelper {
	private static final int TIME_LIST_LENGTH = 3;
	private static final int ALERT_TIME_INTERVAL = 1000;
	private static List<Long> timeList = new ArrayList<Long>();
	public static boolean isSuspiciousMotion(){
		timeList.add(Calendar.getInstance().getTimeInMillis());
		return isAlert();
	}
	private static boolean isAlert() {
		if(timeList.size()<TIME_LIST_LENGTH)
			return false;
		else if(timeList.size()>TIME_LIST_LENGTH)
			timeList = timeList.subList(timeList.size()-TIME_LIST_LENGTH, timeList.size());
		for(int i=0;i<timeList.size()-1;i++){
			if(timeList.get(i+1)-timeList.get(i)>ALERT_TIME_INTERVAL){
				Log.d("detector", "continuous motion");
				return false;
			}
		}
		return true;
	}
}
