package com.jinhs.guardian;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

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
import com.jinhs.common.ApplicationConstant;
import com.jinhs.helper.AccountInfoHelper;
import com.jinhs.rest.DataBO;
import com.jinhs.rest.RestClient;

public class SensorActivity extends Activity implements SensorEventListener {
	private Camera camera;
	private MediaRecorder recorder;
	private String fileName;

	private static boolean stopRecording;

	private boolean isInitialized;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private float lastX, lastY, lastZ;

	private static DataBO trackingData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("SensorActivity", "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);

		fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		fileName += "/" + ApplicationConstant.AUDIO_FILE_NAME;

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
		Log.d("SensorActivity", "onResume()");
		super.onResume();
		isInitialized = false;
		stopRecording = false;
		trackingData = new DataBO();
		trackingData.setEmail(AccountInfoHelper.getEmail(getBaseContext()));
		new RecorderAsyncTask().execute();
	}

	@Override
	protected void onPause() {
		Log.d("SensorActivity", "onPause()");
		stopRecording = true;
		// recorderTask.cancel(false);
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
		Log.d("SensorActivity", "onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d("SensorActivity", "onDestroy()");
		super.onDestroy();
	}

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
			if (deltaX + deltaY + deltaX > 0 && !stopRecording) {
				stopRecording = true;
				Intent cameraIntent = new Intent(getBaseContext(),
						AlertActivity.class);
				startActivityForResult(cameraIntent,
						ActivityRequestCodeEnum.ALERT_ACTIVITY_REQUEST_CODE
								.getValue());
			}
		}
	}

	@SuppressWarnings("deprecation")
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
			camera = null;
		}
	}

	private void startRecording() {
		Log.d("audio", "start recording");
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setOutputFile(fileName);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setAudioChannels(2);
		recorder.setAudioEncodingBitRate(128);
		recorder.setAudioSamplingRate(44100);

		try {
			recorder.prepare();
		} catch (IOException e) {
			Log.e("audio", "prepare() failed");
		}

		recorder.start();
	}

	private void stopRecording() {
		if(recorder!=null){
			recorder.stop();
			recorder.release();
			recorder = null;
		}
		Log.d("audio", "stop recording");
		Log.d("audio file path", "" + fileName);
	}

	PictureCallback jpgCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			long b = data.length;
			Log.d("CAMERA", "onPictureTaken - jpg" + b);
			trackingData.setImage(data);
			camera.stopPreview();
			camera.release();
			camera = null;
			Log.d(""+trackingData.getImage().length,"tracking data image");
			Thread t = new Thread(new MyRunnable(trackingData));
			t.start();
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
		locationManager.requestLocationUpdates(provider, 30 * 1000, 100,
				new LocationUpdateListener());
		Log.d("provider", provider);
		Location location = locationManager.getLastKnownLocation(provider);
		if (location != null)
			Log.d("coordinate",
					location.getLatitude() + "," + location.getLongitude());
		else
			Log.d("location", "null");
		return location;
	}

	private class RecorderAsyncTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			//startRecording();
			Location location = getUserLocation();
			if (location != null) {
				trackingData.setLatitude(location.getLatitude());
				trackingData.setLongtitude(location.getLongitude());
			}
			try {
				Log.d("wait", "5s");
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(Void... arg0) {
			Log.d("backgroud", "record task");
			//stopRecording();
			takePicture();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Intent returnIntent = new Intent();
			returnIntent.putExtra("status", "suc");
			setResult(RESULT_OK, returnIntent);
			finish();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}

class MyRunnable implements Runnable {
    private final DataBO trackingData;
    public MyRunnable(DataBO trackingData) {
       this.trackingData = trackingData;
    }

    public void run() {
    	if (trackingData.getImage() == null)
			Log.d("null", "image in tracking data");
		try {
			new RestClient().sendTrackingInfo(trackingData);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
 }

class LocationUpdateListener implements LocationListener {

	@Override
	public void onLocationChanged(Location arg0) {}

	@Override
	public void onProviderDisabled(String arg0) {}

	@Override
	public void onProviderEnabled(String arg0) {}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}