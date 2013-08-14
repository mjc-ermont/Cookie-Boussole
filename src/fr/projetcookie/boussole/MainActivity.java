package fr.projetcookie.boussole;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener, SensorEventListener, LocationListener {

	
	private SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private Sensor sensorMagneticField;
	   
	private float[] valuesAccelerometer;
	private float[] valuesMagneticField;
	   
	private float[] matrixR;
	private float[] matrixI;
	private float[] matrixValues;
	 
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private CompassView compass=null;
	private Fragment curFragment=null;
	private LocationManager locationManager;
	private String provider;
	private BaloonPositionProvider bpos;
	private Location lastLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    
	    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    
		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								"Boussole",
								"Paramètres", }), this);
		
	   valuesAccelerometer = new float[3];
	   valuesMagneticField = new float[3];
	  
	   matrixR = new float[9];
	   matrixI = new float[9];
	   matrixValues = new float[3];
	   
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the locatioin provider -> use
	    // default
	    boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to 
		// go to the settings
		if (!enabled) {
		  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		  startActivity(intent);
		  finish();
		} else {

			
		    Location location = locationManager.getLastKnownLocation("gps");
		    
		    // Initialize the location fields
		    if (location != null) {
		      System.out.println("Provider " + provider + " has been selected.");
		      onLocationChanged(location);
		    } else {
		    	Toast.makeText(this, "Location not available!", Toast.LENGTH_LONG).show();
			  
		    }
		}
	    
		
	    

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		if(position == 0) {
			BoussoleFragment fragment = new BoussoleFragment();
 
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment).commit();
			
			curFragment = fragment;
		} else {
		
			Fragment fragment = new SettingsFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment).commit();
		}
		return true;
	}

	 @Override
	 protected void onResume() {
	  
	  sensorManager.registerListener(this,
	    sensorAccelerometer,
	    SensorManager.SENSOR_DELAY_NORMAL);
	  sensorManager.registerListener(this,
	    sensorMagneticField,
	    SensorManager.SENSOR_DELAY_NORMAL);

      locationManager.requestLocationUpdates(
              LocationManager.GPS_PROVIDER,
              500,
              10, this);
      bpos = new BaloonPositionTest(500);
      bpos.start();
      
	  super.onResume();
	  

	 }
	  
	 @Override
	 protected void onPause() {
	  
	  sensorManager.unregisterListener(this,
	    sensorAccelerometer);
	  sensorManager.unregisterListener(this,
	    sensorMagneticField);
	  
	  locationManager.removeUpdates(this);
	  bpos.stahp();
	  super.onPause();
	 }
	  

	public static class BoussoleFragment extends Fragment {
		public BoussoleFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_boussole,
					container, false);
			return rootView;
		}
	}
	public static class SettingsFragment extends Fragment {
		public SettingsFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
			
			final SharedPreferences settings = this.getActivity().getSharedPreferences("boussoleSwag", 0);
			final EditText serverUrlEditText = (EditText) rootView.findViewById(R.id.serverUrl);
			final Button validateButton = (Button) rootView.findViewById(R.id.validateButton);
			
			serverUrlEditText.setText(settings.getString("serverUrl", ""));
			validateButton.setVisibility(Button.INVISIBLE);
			validateButton.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					Editor editor = settings.edit();
					editor.putString("serverUrl", serverUrlEditText.getText().toString());
					editor.commit();
					Toast.makeText(v.getContext(), "C'est bon merci", Toast.LENGTH_SHORT).show();
					validateButton.setVisibility(Button.INVISIBLE);
				}
			});
			
			serverUrlEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					validateButton.setVisibility(Button.VISIBLE);
				}
			});
			
			return rootView;
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		  switch(event.sensor.getType()){
		  case Sensor.TYPE_ACCELEROMETER:
		   for(int i =0; i < 3; i++){
			   valuesAccelerometer[i] = event.values[i];
		   }
		   break;
		  case Sensor.TYPE_MAGNETIC_FIELD:
		   for(int i =0; i < 3; i++){
			   valuesMagneticField[i] = event.values[i];
		   }
		   break;
		  }
		    
		  boolean success = SensorManager.getRotationMatrix(
		       matrixR,
		       matrixI,
		       valuesAccelerometer,
		       valuesMagneticField);
		    
		  if(success){
			SensorManager.getOrientation(matrixR, matrixValues);
		     
		   	double azimuth = Math.toDegrees(matrixValues[0]);
		     
		   	if((compass == null) && (curFragment != null))
			   if(curFragment.getView() != null)
				   	compass = (CompassView) curFragment.getView().findViewById(R.id.compassview);
			
		   if((compass != null) && (lastLocation != null))
			   compass.updateDirection(lastLocation.bearingTo(bpos.getLocation()) + (float) azimuth);
		  }
	}

	@Override
	public void onLocationChanged(Location location) {
	    Log.w("Boussole","Longitude="+location.getLongitude());
	    Log.w("Boussole","Latitude="+location.getLatitude());
	    Log.w("Boussole","Accuracy="+location.getAccuracy());
	    Log.w("Boussole","Bearing="+location.getBearing());
	    lastLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
	    Toast.makeText(this, "Disabled provider " + provider,
	            Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
	    Toast.makeText(this, "Enabled new provider " + provider,
	            Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
