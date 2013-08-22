package fr.projetcookie.boussole.providers;

import android.location.Location;

public class BaloonPositionFromSettings extends BaloonPositionProvider {

	Location mFakeLocation = new Location("baloon");
	
	public BaloonPositionFromSettings(int t) {
		super(t);
	}
	
	public BaloonPositionFromSettings(double lat, double lon) {
		super(0);
		setLocation(lat, lon);
	}
	
	public void setLocation(double lat, double lon) {
		mFakeLocation.setLongitude(lon);
		mFakeLocation.setLatitude(lat);
	}
	
	public void run() {} // Who needs Threads when you have swag?

	@Override
	public float stahp() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return mFakeLocation;
	}

}
