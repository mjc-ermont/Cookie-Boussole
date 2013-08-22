package fr.projetcookie.boussole.providers;

import android.location.Location;

public abstract class BaloonPositionProvider extends Thread {
	int refresh_time;
	
	public BaloonPositionProvider(int t) {
		refresh_time = t;
	}
	
	public abstract float stahp();

	abstract public Location getLocation(); 
}
