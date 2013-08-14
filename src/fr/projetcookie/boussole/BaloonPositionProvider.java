package fr.projetcookie.boussole;

import android.location.Location;

public abstract class BaloonPositionProvider extends Thread {
	int refresh_time;
	
	BaloonPositionProvider(int t) {
		refresh_time = t;
	}
	
	abstract float stahp();

	abstract public Location getLocation(); 
}
