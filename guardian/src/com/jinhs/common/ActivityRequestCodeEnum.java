package com.jinhs.common;

public enum ActivityRequestCodeEnum {
	SENSOR_ACTIVITY_REQUEST_CODE(1), ALERT_ACTIVITY_REQUEST_CODE(2);
	private int value;
	private ActivityRequestCodeEnum(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
}
