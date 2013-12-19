package com.jinhs.guardian;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class MainActivity extends Activity implements SensorEventListener{
	private static final int SENSOR_ACTIVITY_REQUEST_CODE = 1;
	private static final int ALERT_ACTIVITY_REQUEST_CODE = 2;
	
	private GestureDetector mGestureDetector;
	
	private static boolean stopRecording;
	private Timer recordTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("onCreate()","start");
		super.onCreate(savedInstanceState);
		
		mGestureDetector = createGestureDetector(this);
		stopRecording = false;
		recordTimer = new Timer();
		recordTimer.schedule(new DataRecordTimerTask(), 5000, 30*1000);
	}
	
	@Override
	protected void onResume() {
		Log.d("onResume()","start");
		super.onResume();
		stopRecording = false;
		Card cardProtectMode = new Card(this);
		cardProtectMode.setText("Protect Mode On");
		View card1View = cardProtectMode.toView();
		setContentView(card1View);
	}

	@Override
	protected void onPause() {
		Log.d("onPause()","start");
		stopRecording = true;
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d("onStop()","start");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d("onDestroy()","start");
		super.onDestroy();
		recordTimer.cancel();
	}
	
	@Override
	public void onBackPressed() {
		Log.d("onBackPressed()","start");
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
            	stopRecording = true;
            	finish();
                return true;
            case R.id.menu_send_alert:
            	stopRecording = true;
            	Intent cameraIntent = new Intent(getBaseContext(), AlertActivity.class);
				startActivityForResult(cameraIntent, ALERT_ACTIVITY_REQUEST_CODE);
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
		if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
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
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		
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
				openOptionsMenu();
                return true;
            } else if (gesture == Gesture.TWO_TAP) {
            	stopRecording = false;
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
			  if(!stopRecording){
					Intent cameraIntent = new Intent(getBaseContext(), SensorActivity.class);
					startActivityForResult(cameraIntent, SENSOR_ACTIVITY_REQUEST_CODE);
				}
		  }
	}
}


