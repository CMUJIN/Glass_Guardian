package com.jinhs.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

public class RestClient {
	
	public static final String trackUrl = "http://jinhsglassguard.appspot.com/api/tracking";
	public static final String alertUrl = "http://jinhsglassguard.appspot.com/api/alert";

	public void sendTrackingInfo(DataBO data) throws ClientProtocolException,
			IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(trackUrl);
		StringEntity params = new StringEntity(new Gson().toJson(data));
		request.setEntity(params);
		httpClient.execute(request);
	}

	public void sendAlert() throws ClientProtocolException, IOException {
		URL url = new URL(alertUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.getInputStream();
	}
}
