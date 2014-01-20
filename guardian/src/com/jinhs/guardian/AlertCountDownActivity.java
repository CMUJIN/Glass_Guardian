package com.jinhs.guardian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class AlertCountDownActivity extends Activity {
	private GestureDetector mGestureDetector;
	private static boolean isAlertCanceled;
	private static boolean isAlertAlreadySent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onResume() {
		Log.d("onResume()", "start");
		super.onResume();
		isAlertCanceled = false;
		isAlertAlreadySent = false;

		mGestureDetector = createGestureDetector(this);
		new CountDownTimer(10 * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				int number = (int) (millisUntilFinished / 1000);
				Card cardProtectMode = new Card(getBaseContext());
				cardProtectMode.setText(R.string.alert_instruction);
				cardProtectMode.setImageLayout(Card.ImageLayout.LEFT);
				switch(number){
					case 0: cardProtectMode.addImage(R.drawable.timer_0);break;
					case 1: cardProtectMode.addImage(R.drawable.timer_1);break;
					case 2: cardProtectMode.addImage(R.drawable.timer_2);break;
					case 3: cardProtectMode.addImage(R.drawable.timer_3);break;
					case 4: cardProtectMode.addImage(R.drawable.timer_4);break;
					case 5: cardProtectMode.addImage(R.drawable.timer_5);break;
					case 6: cardProtectMode.addImage(R.drawable.timer_6);break;
					case 7: cardProtectMode.addImage(R.drawable.timer_7);break;
					case 8: cardProtectMode.addImage(R.drawable.timer_8);break;
					case 9: cardProtectMode.addImage(R.drawable.timer_9);break;
					default:break;
				}
				setContentView(cardProtectMode.toView());
			}

			public void onFinish() {
				if(!isAlertCanceled&&!isAlertAlreadySent){
					callAlertHandler(true);
				}
			}
		}.start();
	}

	@Override
	protected void onPause() {
		Log.d("onPause()", "start");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d("onStop()", "start");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d("onDestroy()", "start");
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Log.d("onBackPressed()", "start");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alert, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection. Menu items typically start another
		// activity, start a service, or broadcast another intent.
		switch (item.getItemId()) {
		case R.id.menu_cancel_alert:
			isAlertCanceled = true;
			callAlertHandler(false);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		gestureDetector.setBaseListener(new gestureListener());
		return gestureDetector;
	}
	
	private void callAlertHandler(boolean isSend) {
		Intent intent = new Intent(getBaseContext(), AlertHandlerActivity.class);
		intent.putExtra("AlertSend", isSend);
		startActivityForResult(intent, 1);
		finish();
	}

	private class gestureListener implements GestureDetector.BaseListener {

		@Override
		public boolean onGesture(Gesture gesture) {
			if (gesture == Gesture.TAP) {
				openOptionsMenu();
				return true;
			} else if (gesture == Gesture.TWO_TAP) {
				return true;
			} else if (gesture == Gesture.SWIPE_RIGHT) {
				isAlertAlreadySent = true;
				callAlertHandler(true);
				return true;
			} else if (gesture == Gesture.SWIPE_LEFT) {
				return true;
			}
			return false;
		}
	}
}
