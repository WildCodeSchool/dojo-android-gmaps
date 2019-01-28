package fr.wildcodeschool.gmaps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
  private static final String TAG = "MapsActivity";

  //Request access coarse permission id
  public static final int PERMISSIONS_LOCATION = 1;

  //The minimum distance and time to change updates
  private static final long MIN_DISTANCE = 0; // 10 meters
  private static final long MIN_TIME = 0;     // 1000 * 60 * 1; // 1 minute

  private final static boolean forceNetwork = false;

  private GoogleMap mMap;
  private Marker    mMarker;
  private Location  mLocation;

  /**
   * Perform initialization of all fragments.
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment)
      getSupportFragmentManager().findFragmentById(R.id.map);
    if (null != mapFragment) {
      mapFragment.getMapAsync(this);
    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
      public void onLocationChanged(Location location) {
        Log.e(TAG, location.toString());
        if (null == mLocation) {
          mLocation = location;
          // Check if the map is ready
          if (null != mMap)
            // Update map content
            setMapPosition();
        }
      }

      public void onStatusChanged(String provider, int status, Bundle extras) {}
      public void onProviderEnabled(String provider) {}
      public void onProviderDisabled(String provider) {}
    };

    if (RESULT_OK == requestGpsPermission()) {
      if (Build.VERSION.SDK_INT >= 23 &&
        ContextCompat.checkSelfPermission( this, ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission( this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
      }
      // Acquire a reference to the system Location Manager
      LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
      // Register the listener with the Location Manager to receive location updates
      locationManager.requestLocationUpdates(
        forceNetwork ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER,
        MIN_TIME, MIN_DISTANCE,
        locationListener );
    }
  }

  /**
   * Determine whether you have been granted gps permission.
   * @return RESULT_OK if you have the permission, or RESULT_CANCELED if not.
   */
  public int requestGpsPermission() {
    int permission;
    permission = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION );
    if (permission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
        this, new String[]{ACCESS_COARSE_LOCATION}, PERMISSIONS_LOCATION );
      return RESULT_CANCELED;
    }
    permission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION );
    if (permission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
        this, new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION );
      return RESULT_CANCELED;
    }
    return RESULT_OK;
  }

  /**
   * Callback for the result from requesting permissions.
   * This method is invoked for every call on requestPermissions
   * @param requestCode int: The request code passed in requestPermissions
   * @param permissions String: The requested permissions. Never null.
   * @param grantResults int: The grant results for the corresponding permissions.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String permissions[],
                                         @NonNull int[] grantResults) {
    if (requestCode == PERMISSIONS_LOCATION) {
      if (grantResults.length > 0
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permission has been granted
        Log.i(TAG, "PERMISSION GRANTED");
      } else {
        Log.e(TAG, "PERMISSION REFUSED");
        finish();
      }
    }
  }

  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    // Check if gps is ready
    if (null != mLocation)
      // Update map content
      setMapPosition();
  }

  /**
   * Update map content with marker and center position
   */
  public void setMapPosition() {
    double lLat  = mLocation.getLatitude();
    double lLng = mLocation.getLongitude();

    // Add a marker and move the camera
    LatLng position = new LatLng(lLat, lLng);
    mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
    mMap.setMaxZoomPreference(15);
    mMap.setMinZoomPreference(12);

    if ( Build.VERSION.SDK_INT >= 23 &&
      ContextCompat.checkSelfPermission( this, ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission( this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    mMap.setMyLocationEnabled(true);

    // SW NE
    LatLngBounds bound = new LatLngBounds(
      new LatLng(lLat-.2, lLng-.2),   // SW
      new LatLng(lLat+.2, lLng+.2));  // NE
    mMap.setLatLngBoundsForCameraTarget(bound);

    mMap.setOnMapClickListener(latLng -> {
      mMap.clear();
      MarkerOptions options = new MarkerOptions()
        .position(latLng)
        .title(latLng.toString());
      mMarker = mMap.addMarker(options);
    });

    mMap.setOnMarkerClickListener(marker -> {
      if (marker.equals(mMarker))
      {
        double lat = mMarker.getPosition().latitude;
        double lon = mMarker.getPosition().longitude;
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat+","+lon+"&mode=b");

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        startActivity(mapIntent);
        return true;
      }
      return false;
    });
  }
}
