package fr.projetcookie.boussole;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, DirectionUpdateListener   {
    public DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mEntriesTitles;
	private String[] mDataTitles;
	private SeparatedListAdapter adapter;
	
	private LocationRequest mLocationRequest;
	private SharedPreferences mPrefs;
	private Editor mEditor;
	private LocationClient mLocationClient;
	private boolean mUpdatesRequested=false;
	
    private final static int
    CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    private Fragment currentFragment;

    private DataManager mDataManager;
	private String mAltitudeText;
	private String mDistanceText;
	private String mLatitudeText;
	private String mLongitudeText;
	private String mPrecisionText;
	
	private ArrayAdapter<String> dataAdapter;
	private ArrayList<String> mDataString = new ArrayList<String>();
	private Marker mBaloonMarker;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mEntriesTitles = getResources().getStringArray(R.array.entries_array);
        mDataTitles = getResources().getStringArray(R.array.data_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        adapter = new SeparatedListAdapter(this);
        adapter.addSection("Navigation", new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mEntriesTitles));
        
        
        mDataString.clear();
		mAltitudeText  = mDataTitles[0] + " N/A";
		mDistanceText  = mDataTitles[1] + " N/A";
		mLatitudeText  = mDataTitles[2] + " N/A";
		mLongitudeText = mDataTitles[3] + " N/A";
		mPrecisionText = mDataTitles[4] + " N/A";

		mDataString.add(mAltitudeText);
		mDataString.add(mDistanceText);
		mDataString.add(mLatitudeText);
		mDataString.add(mLongitudeText);
		mDataString.add(mPrecisionText);
        
        dataAdapter = new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDataString);
        adapter.addSection("Données", dataAdapter);
        
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
            	getSupportActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        }
        
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        
        // Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        
        
        mLocationClient = new LocationClient(this, this, this);
        // Start with updates turned off
        mUpdatesRequested = false;
        
        mDataManager = new DataManager(this, this);
        

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

       return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_settings:
        	selectItem(3);
        	return true;
        case R.id.action_enable_gps:
        	 startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
             return true;
        case R.id.action_boussole:
        	selectItem(1);
        	return true;
        case R.id.action_map:
        	selectItem(2);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
    	if(position >= 1 && position <= 3) {
    		setTitle(mEntriesTitles[position-1]);
        	mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
    	}
    	
    	switch(position) {
    	case 1: // Boussole
    		mBaloonMarker=null;
    		currentFragment = new Fragments.BoussoleFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    		break;
    	case 2: // Maps
    		currentFragment = new Fragments.MapsFragment() ;
			getSupportFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
			
			break;
    	case 3: // Settings
    		mBaloonMarker=null;
    		currentFragment = new Fragments.SettingsFragment(this);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    		break;
    	}
        //
    }
    
    private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Votre GPS n'est pas activé. Améliorer la précision en l'activant?")
	           .setCancelable(false)
	           .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               } //TODO yolo
	           })
	           .setNegativeButton("GET THE FUCK AWAY", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;

        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public class SeparatedListAdapter extends BaseAdapter {
    	
    	public final Map<String,Adapter> sections = new LinkedHashMap<String,Adapter>();
    	public final ArrayAdapter<String> headers;
    	public final static int TYPE_SECTION_HEADER = 0;
    	
    	public SeparatedListAdapter(Context context) {
    		headers = new ArrayAdapter<String>(context, R.layout.list_header);
    	}
    	
    	public void addSection(String section, Adapter adapter) {
    		this.headers.add(section);
    		this.sections.put(section, adapter);
    	}
    	
    	public Object getItem(int position) {
    		for(Object section : this.sections.keySet()) {
    			Adapter adapter = sections.get(section);
    			int size = adapter.getCount() + 1;
    			
    			// check if position inside this section 
    			if(position == 0) return section;
    			if(position < size) return adapter.getItem(position - 1);

    			// otherwise jump into next section
    			position -= size;
    		}
    		return null;
    	}

    	public int getCount() {
    		// total together all sections, plus one for each section header
    		int total = 0;
    		for(Adapter adapter : this.sections.values())
    			total += adapter.getCount() + 1;
    		return total;
    	}

    	public int getViewTypeCount() {
    		// assume that headers count as one, then total all sections
    		int total = 1;
    		for(Adapter adapter : this.sections.values())
    			total += adapter.getViewTypeCount();
    		return total;
    	}
    	
    	public int getItemViewType(int position) {
    		int type = 1;
    		for(Object section : this.sections.keySet()) {
    			Adapter adapter = sections.get(section);
    			int size = adapter.getCount() + 1;
    			
    			// check if position inside this section 
    			if(position == 0) return TYPE_SECTION_HEADER;
    			if(position < size) return type + adapter.getItemViewType(position - 1);

    			// otherwise jump into next section
    			position -= size;
    			type += adapter.getViewTypeCount();
    		}
    		return -1;
    	}
    	
    	public boolean areAllItemsSelectable() {
    		return false;
    	}

    	public boolean isEnabled(int position) {
    		return (getItemViewType(position) != TYPE_SECTION_HEADER);
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		int sectionnum = 0;
    		for(Object section : this.sections.keySet()) {
    			Adapter adapter = sections.get(section);
    			int size = adapter.getCount() + 1;
    			
    			// check if position inside this section 
    			if(position == 0) return headers.getView(sectionnum, convertView, parent);
    			if(position < size) return adapter.getView(position - 1, convertView, parent);

    			// otherwise jump into next section
    			position -= size;
    			sectionnum++;
    		}
    		return null;
    	}

    	@Override
    	public long getItemId(int position) {
    		return position;
    	}


    }

    @Override
    protected void onPause() {
    	super.onPause();
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        
        this.mDataManager.stop();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
        mLocationClient.connect();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

        // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
        
        this.mDataManager.start();
    }
	
   @Override
    protected void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
        	mLocationClient.removeLocationUpdates(mDataManager);
        	
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();
        super.onStop();
    }

  @Override
    public void onConnected(Bundle dataBundle) {
       // Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
       // if (mUpdatesRequested) {
        	mLocationClient.requestLocationUpdates(mLocationRequest, mDataManager);
      //  }
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } 
    }

	@Override
	public void onDirectionUpdate(float dir) {
		if(currentFragment instanceof Fragments.BoussoleFragment) {
			if(currentFragment != null) {
				if(currentFragment.getView() != null) {
					((CompassView)currentFragment.getView().findViewById(R.id.compassview)).updateDirection(dir);
				}
			}
		}
		
		Location baloonLoc = mDataManager.mProvider.getLocation();
		double h = baloonLoc.getAltitude(); // 6
		double distance = baloonLoc.distanceTo(mDataManager.mLastLocation); // 7
		double lat = baloonLoc.getLatitude(); // 8
		double lon = baloonLoc.getLongitude(); // 9
		double prec = baloonLoc.getAccuracy();
		
		if(h > 800)
			mAltitudeText  = mDataTitles[0] + " " + Math.round(h/10)/100f + " km";
		else
			mAltitudeText  = mDataTitles[0] + " " + Math.round(h) + " m";

		if(distance > 800)
			mDistanceText  = mDataTitles[1] + " " + Math.round(distance/10)/100f + " km";
		else
			mDistanceText  = mDataTitles[1] + " " + Math.round(distance) + " m";
			
		mLatitudeText  = mDataTitles[2] + " " + Math.round(lat*1000)/1000f + "°";
		mLongitudeText = mDataTitles[3] + " " + Math.round(lon*1000)/1000f + "°";
		
		if(prec > 800) 
			mPrecisionText = mDataTitles[4] + " " + Math.round(prec/10)/100f + " km";
		else
			mPrecisionText = mDataTitles[4] + " " + Math.round(prec) + " m";
			

		dataAdapter.clear();
		dataAdapter.add(mAltitudeText);
		dataAdapter.add(mDistanceText);
		dataAdapter.add(mLatitudeText);
		dataAdapter.add(mLongitudeText);
		
		if(prec != 0)
			dataAdapter.add(mPrecisionText);
		
		dataAdapter.notifyDataSetChanged();
		adapter.notifyDataSetChanged();
		
		if(currentFragment instanceof Fragments.MapsFragment) {
			((Fragments.MapsFragment) currentFragment).onBaloonLocationChange(baloonLoc);
		}
		
		if(mBaloonMarker == null){
			if(currentFragment instanceof Fragments.MapsFragment) {
				if(((Fragments.MapsFragment)currentFragment).getMap() != null) {
					((Fragments.MapsFragment)currentFragment).getMap().setInfoWindowAdapter(new PopupAdapter(this.getLayoutInflater()));
					mBaloonMarker = ((Fragments.MapsFragment) currentFragment).getMap().addMarker(new MarkerOptions()
						.position(new LatLng(lat, lon))
						.title("Ballon")
						.snippet(mAltitudeText + "\n" + mDistanceText));
				}
			}
		} else {
			mBaloonMarker.setPosition(new LatLng(lat, lon));
			mBaloonMarker.setSnippet(mAltitudeText + "\n" + mDistanceText);
		}
	}

	public void invalidateSettings() {
		mDataManager.invalidateSettings();
	}

}
