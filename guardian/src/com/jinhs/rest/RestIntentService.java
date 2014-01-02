package com.jinhs.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class RestIntentService extends IntentService {
    private RestClient client;
    public RestIntentService() {
        super("RestIntentService");
        client = new RestClient();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
    	Log.d("start", "intent service");
    	DataBO data = (DataBO) intent.getSerializableExtra("data");
    	if(data.getImage()==null||data.getImage().length==0)
    		Log.d("null","image in intent");
    	try {
			client.sendTrackingInfo(data);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
