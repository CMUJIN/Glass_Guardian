package com.jinhs.guardian;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;

import com.google.android.glass.app.Card;
import com.jinhs.helper.AccountInfoHelper;
import com.jinhs.rest.RestClient;

public class AlertHandlerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alert_handler);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alert_handler, menu);
		return true;
	}

	@Override
	protected void onResume() {
		Log.d("onResume()", "start");
		super.onResume();
		
		Intent intent = getIntent();
		final boolean alertSend = intent.getBooleanExtra("AlertSend", true);
		Card alertIsSent = new Card(getBaseContext());
		if(alertSend){
			alertIsSent.setText(R.string.alert_send);
		}
		else{ 
			alertIsSent.setText(R.string.alert_cancel);
		}
		alertIsSent.setImageLayout(Card.ImageLayout.LEFT);
		setContentView(alertIsSent.toView());
		new CountDownTimer(2 * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				if(alertSend){
					new AlertSendingTask().execute();
				}
				finish();
			}
		}.start();
	}
	
	private class AlertSendingTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(Void... arg0) {
			Log.d("alert", "sent");
			try {
				new RestClient().sendAlert(AccountInfoHelper
						.getEmail(getBaseContext()));
			} catch (ClientProtocolException e) {
				Log.d("ClientProtocolException",
						"alert send e:" + e.getMessage());
				// return "Alert send failed, check your network connection";
			} catch (IOException e) {
				Log.d("IOException", "alert send e:" + e.getMessage());
				// return "Alert send failed, check your network connection";
			}
			return "alert is sent";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}
}
