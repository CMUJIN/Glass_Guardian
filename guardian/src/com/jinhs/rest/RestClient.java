package com.jinhs.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.google.gson.Gson;

public class RestClient {
	
	public static final String trackUrl = "http://jinhsglassguard.appspot.com/api/tracking";
	public static final String alertUrl = "http://jinhsglassguard.appspot.com/api/alert";

	public void sendTrackingInfo(DataBO data) throws ClientProtocolException,
			IOException {
		Log.d("rest","upload Tracking data");
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(trackUrl);
		StringEntity params = new StringEntity(new Gson().toJson(data));
		request.setEntity(params);
		httpClient.execute(request);
	}

	public void sendAlert(String userId) throws ClientProtocolException, IOException {
		Log.d("rest","send alert");
		URL url = new URL(alertUrl+"?userId="+userId);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.getInputStream();
	}
}
