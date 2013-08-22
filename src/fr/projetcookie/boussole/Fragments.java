package fr.projetcookie.boussole;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class Fragments {
	public static class BoussoleFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_boussole, container, false);
			return rootView;
		}
	}
	
	@SuppressLint("ValidFragment")
	public static class MapsFragment extends SupportMapFragment implements OnMyLocationChangeListener {
		
		boolean f=true;
		Location mMyLocation;
		Location mBaloonLocation;
		
		public MapsFragment() {
			super();
		}
	

		@Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
	        this.getMap().setMyLocationEnabled(true);
	        this.getMap().setOnMyLocationChangeListener(this);
	        this.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.395943, -1.166917), 9));
	    }

		@Override
		public void onMyLocationChange(Location location) {
			if((location.getLatitude() == 0) && (location.getLongitude() == 0))
				return;
			
			mMyLocation=location;
			if((f) && (this.getMap() != null) && (mBaloonLocation != null)) {
				fitMap();
				f=false;
			}
		}
		
		public void fitMap() {			
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			
			builder.include(new LatLng(mBaloonLocation.getLatitude(), mBaloonLocation.getLongitude()));
			builder.include(new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude()));
			
			this.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
		}
		
		public void onBaloonLocationChange(Location location) {
			if((location.getLatitude() == 0) && (location.getLongitude() == 0))
				return;
			
			mBaloonLocation=location;
			if ((f) && (this.getMap() != null) && (mMyLocation != null)) {
				fitMap();
				f=false;
			}
		}
	}
	
	public static class SettingsFragment extends Fragment {
		
		private SharedPreferences prefs;
		private boolean dataFromServer;
		private String serverUri;
		private float lat;
		private float lon;

		private RadioButton serverRadioButton;
		private EditText serverUriEditText;
		private EditText serverLatEditText;
		private EditText serverLonEditText;
		
		private Context ctx;
		private MainActivity mActivity;
		
		SettingsFragment(MainActivity activity) {
			mActivity = activity;
		}
		
		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ctx = inflater.getContext();
			View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
			prefs = inflater.getContext().getSharedPreferences("Cookie", 0); 
			
			dataFromServer = prefs.getBoolean("dataFromServer", true);
			serverUri = prefs.getString("serverUri", "http://<server.tld>/Cookie-WebUI-Server/");
			lat = prefs.getFloat("lat", 0);
			lon = prefs.getFloat("lon", 0);

			((RadioGroup)rootView.findViewById(R.id.radio_group)).check(dataFromServer ? R.id.radioButton1 : R.id.radioButton2);
			serverUriEditText = (EditText)rootView.findViewById(R.id.serverUrl);
			serverUriEditText.setText(serverUri);
			
			serverLatEditText = (EditText)rootView.findViewById(R.id.latitude);
			serverLatEditText.setText(""+lat);

			serverLonEditText = (EditText)rootView.findViewById(R.id.longitude);
			serverLonEditText.setText(""+lon);
			
			serverRadioButton = (RadioButton) rootView.findViewById(R.id.radioButton1);
			
			((Button)rootView.findViewById(R.id.validateButton)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					dataFromServer = serverRadioButton.isChecked();
					serverUri = serverUriEditText.getEditableText().toString();
					lat = Float.parseFloat(serverLatEditText.getEditableText().toString());
					lon = Float.parseFloat(serverLonEditText.getEditableText().toString());
				
					Editor edit = prefs.edit();
					edit.putBoolean("dataFromServer", dataFromServer);
					edit.putString("serverUri", serverUri);
					edit.putFloat("lon", lon);
					edit.putFloat("lat", lat);
					edit.commit();
					
					Toast.makeText(ctx, "C'est bon merci.", Toast.LENGTH_SHORT).show();
					mActivity.mDrawerLayout.openDrawer(mActivity.mDrawerList);
					mActivity.invalidateSettings();
				}
			});
			
			return rootView;
		}
	}
}
