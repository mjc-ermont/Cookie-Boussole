package fr.projetcookie.boussole.providers;

import android.location.Location;

public class BaloonPositionTest extends BaloonPositionProvider {
	
	public BaloonPositionTest(int t) {
		super(t);
	}
	
	public void run() {
		
	}

	@Override
	public float stahp() {
		return 0;
	}

	@Override
	public Location getLocation() {
		Location loc = new Location("bpos");
		loc.setLatitude(45.200);
		loc.setLongitude(0.733);
		loc.setBearing(0);
		loc.setAltitude(42000);
		return loc;
	}

}
