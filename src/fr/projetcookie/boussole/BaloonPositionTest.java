package fr.projetcookie.boussole;

import android.location.Location;

public class BaloonPositionTest extends BaloonPositionProvider {
	
	BaloonPositionTest(int t) {
		super(t);
	}
	
	public void run() {
		
	}

	@Override
	float stahp() {
		return 0;
	}

	@Override
	public Location getLocation() {
		Location loc = new Location("bpos");
		loc.setLatitude(0);
		loc.setLongitude(0);
		return loc;
	}

}
