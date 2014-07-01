package fr.projetcookie.boussole;


import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;

import fr.projetcookie.boussole.providers.BaloonPositionFromServer;
import fr.projetcookie.boussole.providers.BaloonPositionFromSettings;
import fr.projetcookie.boussole.providers.BaloonPositionProvider;
import fr.projetcookie.boussole.providers.BaloonPositionTest;

public class DataManager implements SensorEventListener, LocationListener{
	public BaloonPositionProvider mProvider;
	public Location mLastLocation= new Location("");
	public float direction=0;
	
	private DirectionUpdateListener mListener;
	
	private float[] mGravity = new float[3];
	private float[] mGeomagnetic = new float[3];
	private SensorManager sensorManager;
	private Sensor gsensor;
	private Sensor msensor;
	private boolean positionUpdated=false;
	
	private Context mContext;
	private boolean dataFromServer;
	private double lat;
	private String serverUri;
	private double lon;
	private GeomagneticField geoField;
	
	public DataManager(Context context, DirectionUpdateListener listener) {
		mContext = context;
		mListener = listener;
		
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		getSettings();
	}
	

	public void start() {
		if(dataFromServer) {
			mProvider = new BaloonPositionFromServer(5000, serverUri);
			Toast.makeText(mContext, "Getting data from server", Toast.LENGTH_SHORT).show();
		}
		else {
			mProvider = new BaloonPositionFromSettings(lat, lon);
			Toast.makeText(mContext, "Getting data from settings", Toast.LENGTH_SHORT).show();
		}
		mProvider.start();
		sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stop() {
		sensorManager.unregisterListener(this);
		mProvider.stahp();
	}
	
	public void getSettings() {
		SharedPreferences settings = mContext.getSharedPreferences("Cookie", 0);
		
		dataFromServer = settings.getBoolean("dataFromServer", true);
		serverUri = settings.getString("serverUri", "http://<server.tld>/Cookie-WebUI-Server/");
		lat = settings.getFloat("lat", 0);
		lon = settings.getFloat("lon", 0);
	}
	
	public void invalidateSettings() {
		getSettings();
		
		stop();
		start();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final float alpha = 0.97f;

		synchronized (this) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

				mGravity[0] = alpha * mGravity[0] + (1 - alpha)
						* event.values[0];
				mGravity[1] = alpha * mGravity[1] + (1 - alpha)
						* event.values[1];
				mGravity[2] = alpha * mGravity[2] + (1 - alpha)
						* event.values[2];
			}

			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
						* event.values[0];
				mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
						* event.values[1];
				mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
						* event.values[2];

			}

			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				direction = (float) Math.toDegrees(orientation[0]); // orientation
				direction = (direction + 360) % 360;
				if(positionUpdated)
					updateDirection();
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		positionUpdated=true;
		mLastLocation=location;

		geoField = new GeomagneticField(
				(float) mLastLocation.getLatitude(),
				(float) mLastLocation.getLongitude(),
				(float) mLastLocation.getAltitude(),
				System.currentTimeMillis());
		
		updateDirection();
	}
	
	private void updateDirection() {			
		direction += geoField.getDeclination();
		float bearing = mLastLocation.bearingTo(mProvider.getLocation());
		
		
		mListener.onDirectionUpdate(direction - bearing);
	}
}
