package com.anton.suprun.simplepins.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.anton.suprun.simplepins.R;
import com.anton.suprun.simplepins.tools.Tools;
import com.anton.suprun.simplepins.data.PinEntity;
import com.anton.suprun.simplepins.data.PinsDBHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Pins_Map_Act";
    private static final double MIN_BOUNDS = 0.01;
    private GoogleMap mMap;
    private Marker mCurrentMarker;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LatLngBounds mBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fr_map);
        mapFragment.getMapAsync(this);

        // Build google API client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Tools.logout();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set up the map
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Add pins associated with current user
                LatLng pos = null;
                for (PinEntity pin : PinsDBHelper.getInstance().getPinsList()) {
                    pos = new LatLng(pin.getLatitude(), pin.getLongitude());
                    // Determine bounds of the region containing pins
                    appendToBounds(pos);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(pos).title(pin.getTitle()));
                    decoratePinned(marker);
                }

                // Set marker to current location and make it selected
                if (mCurrentLocation != null) {
                    pos = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    appendToBounds(pos);
                }
                selectMarker(mMap.addMarker(new MarkerOptions().position(pos)));

                // Shrink view to the bounds
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mBounds.getCenter()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 50));
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Display marker on current location
                LatLng pos = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                selectMarker(mMap.addMarker(new MarkerOptions().position(pos)));
                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Display marker on this location
                selectMarker(mMap.addMarker(new MarkerOptions().position(latLng)));
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Select the marker
                selectMarker(marker);
                return false;
            }
        });

    }

    private void appendToBounds(LatLng pos) {
        if (mBounds == null) {
            mBounds = new LatLngBounds(new LatLng(pos.latitude - MIN_BOUNDS, pos.longitude - MIN_BOUNDS),
                    new LatLng(pos.latitude + MIN_BOUNDS, pos.longitude + MIN_BOUNDS));
        } else {
            mBounds = mBounds.including(pos);
        }
    }

    private void selectMarker(Marker marker) {
        // Visually deselect previously selected marker
        unselectCurrentMarker();

        // Set current marker to new position
        mCurrentMarker = marker;
        LatLng pos = mCurrentMarker.getPosition();

        // Try to get address of the place if marker doesn't contain it
        if (TextUtils.isEmpty(mCurrentMarker.getTitle())) {
            (new ObtainAddressTask()).execute(pos);
        }

        // Visually select current marker and focus view on it
        decorateUnpinned(mCurrentMarker);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(mBounds.getCenter()));
    }

    private void unselectCurrentMarker() {
        if (mCurrentMarker != null) {
            if (PinsDBHelper.getInstance().getPinsList().contains(PinEntity.from(mCurrentMarker))) {
                // Visually deselect the marker
                decoratePinned(mCurrentMarker);
            } else {
                // Remove marker if it was not pinned
                mCurrentMarker.remove();
            }
        }
    }

    public Marker getCurrentMarker() {
        return mCurrentMarker;
    }

    public void decoratePinned(Marker marker) {
        try {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            marker.hideInfoWindow();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void decorateUnpinned(Marker marker) {
        try {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker());
            marker.showInfoWindow();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: need to handle google api connection issues
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: need to handle google api connection issues
    }

    class ObtainAddressTask extends AsyncTask<LatLng, Void, String> {
        protected String doInBackground(LatLng... params) {
            String resultAddress = "No address found";
            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            // Get the current location from the input parameter list
            LatLng loc = params[0];

            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.latitude,
                        loc.longitude, 1);
            } catch (Exception ioException) {
                ioException.printStackTrace();
                return resultAddress;
            }

            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);

                // Obtain available address details
                resultAddress = address.getAddressLine(0) != null ? address.getAddressLine(0) :
                        address.getLocality() != null ? address.getLocality() :
                                address.getCountryName();
            }
            return resultAddress;
        }

        @Override
        protected void onPostExecute(String address) {
            super.onPostExecute(address);
            // Update current marker with result
            mCurrentMarker.setTitle(address);
            mCurrentMarker.showInfoWindow();
        }
    }
}
