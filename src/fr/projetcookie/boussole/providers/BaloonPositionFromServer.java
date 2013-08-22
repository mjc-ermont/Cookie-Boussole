package fr.projetcookie.boussole.providers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.location.Location;

public class BaloonPositionFromServer extends BaloonPositionProvider {
	boolean stillAlive=true;

	private Location mLastLocation=new Location("baloon");
	private String mServer="";

	private static final int ID_GPS = 0;
	private static final int ID_VAL_LAT = 0;
	private static final int ID_VAL_LON = 1;
	private static final int ID_VAL_ALT = 2;
	
	public BaloonPositionFromServer(int t) {
		super(t);
	}
	
	public BaloonPositionFromServer(int t, String server) {
		super(t);
		mServer=server;
	}

	public void run() {
		while(stillAlive) {
			/*
			This was a triumph.
			I'm making a note here: HUGE SUCCESS.
			It's hard to overstate my satisfaction.
			Aperture Science
			We do what we must
			because we can.
			For the good of all of us.
			Except the ones who are dead.
			But there's no sense crying over every mistake.
			You just keep on trying till you run out of cake.
			And the Science gets done.
			And you make a neat gun.
			For the people who are still alive.
			I'm not even angry.
			I'm being so sincere right now.
			Even though you broke my heart.
			And killed me.
			And tore me to pieces.
			And threw every piece into a fire.
			As they burned it hurt because I was so happy for you!
			Now these points of data make a beautiful line.
			And we're out of beta.
			We're releasing on time.
			So I'm GLaD. I got burned.
			Think of all the things we learned
			for the people who are still alive.
			Go ahead and leave me.
			I think I prefer to stay inside.
			Maybe you'll find someone else to help you.
			Maybe Black Mesa
			THAT WAS A JOKE.
			HAHA. FAT CHANCE.
			Anyway, this cake is great.
			It's so delicious and moist.
			Look at me still talking
			when there's Science to do.
			When I look out there, it makes me GLaD I'm not you.
			I've experiments to run.
			There is research to be done.
			On the people who are still alive.
			And believe me I am still alive.
			I'm doing Science and I'm still alive.
			I feel FANTASTIC and I'm still alive.
			While you're dying I'll be still alive.
			And when you're dead I will be still alive.
			STILL ALIVE (x2)
			 */
			try {
				String request = mServer + (mServer.endsWith("/") ? "" : "/") + "bin/get.php?c=" + ID_GPS + "&t=0&n=1";
				
				HttpClient httpclient = new DefaultHttpClient();
			    HttpResponse response;
			
				response = httpclient.execute(new HttpGet(request));
				StatusLine statusLine = response.getStatusLine();
			    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
			        ByteArrayOutputStream out = new ByteArrayOutputStream();
			        response.getEntity().writeTo(out);
			        out.close();
			        String responseString = out.toString();
			        JSONArray ja = new JSONArray(responseString);
			        double lat = ja.getJSONArray(ID_VAL_LAT).getJSONArray(0).getDouble(1);
			        double lon = ja.getJSONArray(ID_VAL_LON).getJSONArray(0).getDouble(1);
			        double alt = ja.getJSONArray(ID_VAL_ALT).getJSONArray(0).getDouble(1);
			        
			        mLastLocation.setAltitude(alt);
			        mLastLocation.setLatitude(lat);
			        mLastLocation.setLongitude(lon);
			    } else{
			        response.getEntity().getContent().close();
			        throw new IOException(statusLine.getReasonPhrase());
			    }
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		    
			    
			
			try {
				Thread.sleep(refresh_time);
			} catch (InterruptedException e) {
				e.printStackTrace();
				stillAlive = false;
			}
		}
	}
	
	public void setServer(String newServer) {
		mServer = newServer;
	}

	@Override
	public float stahp() {
		stillAlive=false;
		return 0;
	}

	@Override
	public Location getLocation() {
		return mLastLocation;
	}

}


