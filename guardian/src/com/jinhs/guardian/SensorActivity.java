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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import com.jinhs.common.ActivityRequestCodeEnum;

public class SensorActivity extends Activity implements SensorEventListener{
	private Camera camera;
	private MediaRecorder recorder;
	private String fileName;
	
	private RecorderAsyncTask recorderTask;
	
	private static boolean stopRecording;
	
	private boolean isInitialized;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private final float NOISE = (float) 12.0;
	private float lastX, lastY, lastZ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
		
		recorderTask = new RecorderAsyncTask();
		fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/guardian_audio.3gp";
        
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
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
		isInitialized = false;
		stopRecording = false;
		if(recorderTask.getStatus()!=AsyncTask.Status.RUNNING)
			recorderTask.execute();
		else{
			recorderTask.cancel(false);
			finish();
		}
	}

	@Override
	protected void onPause() {
		stopRecording = true;
		recorderTask.cancel(true);
		if (camera != null) {
			camera.release();
			camera = null;
		}
		if (recorder != null) {
			recorder.release();
			recorder = null;
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
	

	@Override
	public void onSensorChanged(SensorEvent event) {
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
			if (deltaX < NOISE)
				deltaX = (float) 0.0;
			if (deltaY < NOISE)
				deltaY = (float) 0.0;
			if (deltaZ < NOISE)
				deltaZ = (float) 0.0;
			lastX = x;
			lastY = y;
			lastZ = z;
			if (deltaX + deltaY + deltaX > 0&&!stopRecording) {
				stopRecording = true;
            	Intent cameraIntent = new Intent(getBaseContext(), AlertActivity.class);
				startActivityForResult(cameraIntent, ActivityRequestCodeEnum.ALERT_ACTIVITY_REQUEST_CODE.getValue());
			} 
		}
	}
	
	private void takePicture() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
		
		camera = Camera.open(getCameraId());
		try {
			// init surface view
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
			SurfaceHolder sHolder = surfaceView.getHolder();
			sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			sHolder.setSizeFromLayout();
			Log.i("camera", "start");
			// set camera parameters
			Parameters parameters = camera.getParameters();
			camera.setParameters(parameters);
			camera.setPreviewDisplay(sHolder);
			camera.startPreview();
			camera.takePicture(shutterCallback, null, jpgCallback);
			Log.i("picture", "taken");
			
		} catch (IOException e) {
			camera.release();
			camera=null;
		}
	}
	
	private void startRecording() {
		Log.d("audio","start recording");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("audio", "prepare() failed");
        }

        recorder.start();
    }
	
	private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        Log.d("audio","stop recording");
    }
	
	
	PictureCallback jpgCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("CAMERA", "onPictureTaken - jpg");
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	};

	PictureCallback rawCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("CAMERA", "onPictureTaken - raw");
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	};

	ShutterCallback shutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			camera = null;
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

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
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