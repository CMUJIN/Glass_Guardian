package com.jinhs.guardian;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SensorActivity extends Activity {
	private Camera mCamera;
	private MediaRecorder mRecorder;
	private String mFileName;
	
	private RecorderAsyncTask recorderTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
		
		recorderTask = new RecorderAsyncTask();
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/guardian_audio.3gp";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sensor, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume(); 
		recorderTask.execute();
	}

	@Override
	protected void onPause() {
		recorderTask.cancel(true);
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void takePicture() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
		
		mCamera = Camera.open(getCameraId());
		try {
			// init surface view
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
			SurfaceHolder sHolder = surfaceView.getHolder();
			sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			sHolder.setSizeFromLayout();
			Log.i("camera", "start");
			// set camera parameters
			Parameters parameters = mCamera.getParameters();
			mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(sHolder);
			mCamera.startPreview();
			mCamera.takePicture(shutterCallback, null, jpgCallback);
			Log.i("picture", "taken");
			
		} catch (IOException e) {
			mCamera.release();
			mCamera=null;
		}
	}
	
	private void startRecording() {
		Log.d("audio","start recording");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("audio", "prepare() failed");
        }

        mRecorder.start();
    }
	
	private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Log.d("audio","stop recording");
    }
	
	
	PictureCallback jpgCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("CAMERA", "onPictureTaken - jpg");
			camera.stopPreview();
			camera.release();
			mCamera = null;
		}
	};

	PictureCallback rawCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("CAMERA", "onPictureTaken - raw");
			camera.stopPreview();
			camera.release();
			mCamera = null;
		}
	};

	ShutterCallback shutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			mCamera = null;
			Log.i("CAMERA", "onShutter'd");
		}

	};

	private int getCameraId() {
		CameraInfo ci = new CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, ci);
			if (ci.facing == CameraInfo.CAMERA_FACING_BACK)
				return i;
		}
		return -1; // No camera found
	}
	
	private Location getUserLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(true);

		String provider = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(provider, 30*1000, 100, new LocationUpdateListener());
		Log.d("provider", provider);
		Location location = locationManager.getLastKnownLocation(provider);
		if(location!=null)
			Log.d("coordinate", location.getLatitude()+","+location.getLongitude());
		return location;
	}
	
	private class RecorderAsyncTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			startRecording();
			getUserLocation();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(Void... arg0) {
			
			takePicture();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			stopRecording();
			
			Intent returnIntent = new Intent();
			returnIntent.putExtra("status","suc");
			setResult(RESULT_OK,returnIntent); 
			finish();
		}
	}
}

class LocationUpdateListener implements LocationListener {

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
}