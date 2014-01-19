package com.jinhs.guardian;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.jinhs.common.ActivityRequestCodeEnum;
import com.jinhs.common.ApplicationConstant;
import com.jinhs.helper.SuspiciousMotionDetectionHelper;

public class MainActivity extends Activity implements SensorEventListener{
	
	private GestureDetector gestureDetector;
	
	private static boolean suspandTimer;
	private static boolean alertAlreadyTrigged;
	private Timer recordTimer;
	
	private boolean isInitialized;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private float lastX, lastY, lastZ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("MainActivity","onCreate()");
		super.onCreate(savedInstanceState);
		
		gestureDetector = createGestureDetector(this);
		recordTimer = new Timer();
		recordTimer.schedule(new DataRecordTimerTask(), 5000, 30*1000);
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		
	}
	
	@Override
	protected void onResume() {
		Log.d("MainActivity","onResume()");
		super.onResume();
		isInitialized = false;
		suspandTimer = false;
		alertAlreadyTrigged = false;
		Card cardProtectMode = new Card(this);
		cardProtectMode.setText("Protect Mode On");
		View card1View = cardProtectMode.toView();
		setContentView(card1View);
	}

	@Override
	protected void onPause() {
		Log.d("MainActivity","onPause()");
		suspandTimer = true;
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d("MainActivity","onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d("MainActivity","onDestroy()");
		super.onDestroy();
		recordTimer.cancel();
	}
	
	@Override
	public void onBackPressed() {
		Log.d("MainActivity","onBackPressed()");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
        switch (item.getItemId()) {
            case R.id.menu_stop:
            	finish();
                return true;
            case R.id.menu_send_alert:
            	Intent cameraIntent = new Intent(getBaseContext(), AlertActivity.class);
				startActivityForResult(cameraIntent, ActivityRequestCodeEnum.ALERT_ACTIVITY_REQUEST_CODE.getValue());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	
	/*
	 * Send generic motion events to the gesture detector
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (gestureDetector != null) {
            return gestureDetector.onMotionEvent(event);
        }
        return false;
	}

	//accelerometer
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	//accelerometer
	@Override
	public void onSensorChanged(SensorEvent event) {
		float noisy = ApplicationConstant.ACCELEROMETER_NOISY;
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!isInitialized) {
			lastX = x;
			lastY = y;
			lastZ = z;
			isInitialized = true;
		} else {
			float deltaX = Math.abs(lastX - x);
			float deltaY = Math.abs(lastY - y);
			float deltaZ = Math.abs(lastZ - z);
			if (deltaX < noisy)
				deltaX = (float) 0.0;
			if (deltaY < noisy)
				deltaY = (float) 0.0;
			if (deltaZ < noisy)
				deltaZ = (float) 0.0;
			lastX = x;
			lastY = y;
			lastZ = z;
			if (deltaX + deltaY + deltaX > 0&&SuspiciousMotionDetectionHelper.isSuspiciousMotion()&&!alertAlreadyTrigged) {
				suspandTimer = true;
				alertAlreadyTrigged = true;
            	Intent cameraIntent = new Intent(getBaseContext(), AlertActivity.class);
				startActivityForResult(cameraIntent, ActivityRequestCodeEnum.ALERT_ACTIVITY_REQUEST_CODE.getValue());
			} 
		}

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if (requestCode == 1) {
		     if(resultCode == RESULT_OK){      
		         String result=data.getStringExtra("status");
		         Log.d("result", ""+result);
		     }
		     if (resultCode == RESULT_CANCELED) {    
		         //Write your code if there's no result
		     }
		  }
		}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		gestureDetector.setBaseListener(new gestureListener());
		return gestureDetector;
	}
	
	private class gestureListener implements GestureDetector.BaseListener{

		@Override
		public boolean onGesture(Gesture gesture) {
			if (gesture == Gesture.TAP) {
				suspandTimer = true;
				openOptionsMenu();
                return true;
            } else if (gesture == Gesture.TWO_TAP) {
                return true;
            } else if (gesture == Gesture.SWIPE_RIGHT) {
                // do something on right (forward) swipe
                return true;
            } else if (gesture == Gesture.SWIPE_LEFT) {
                // do something on left (backwards) swipe
                return true;
            }
			return false;
		}
	}
	
	private class DataRecordTimerTask extends TimerTask {
		  public void run() {
			  if(!suspandTimer){
					Intent cameraIntent = new Intent(getBaseContext(), SensorActivity.class);
					startActivityForResult(cameraIntent, ActivityRequestCodeEnum.SENSOR_ACTIVITY_REQUEST_CODE.getValue());
				}
		  }
	}
}


