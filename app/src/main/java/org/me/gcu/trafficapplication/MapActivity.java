package org.me.gcu.trafficapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 10f;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private double latitude, longitude;
    private String title, startingPoint, endingPoint;
    private double startingLat, startingLon, endingLat, endingLon;
    private LatLng latitudeLongitude;
    private boolean isJourney = false;
    private Map<String, String[]> plannedWorks, currentWorks, currentIncicents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // START ---- change colour of back arrow
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.action_bar_text_color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        // END ---- change colour of back arrow

        plannedWorks = new HashMap<>();
        currentWorks = new HashMap<>();
        currentIncicents = new HashMap<>();

        if(getIntent().getStringExtra("LATITUDE") != null &&
                getIntent().getStringExtra("LONGITUDE") != null) {
            latitude = Double.valueOf(getIntent().getStringExtra("LATITUDE"));
            longitude = Double.valueOf(getIntent().getStringExtra("LONGITUDE"));
            latitudeLongitude = new LatLng(latitude, longitude);
        }

        if(getIntent().getStringExtra("START_POINT") != null) {
            startingPoint = getIntent().getStringExtra("START_POINT");
            endingPoint = getIntent().getStringExtra("END_POINT");
            isJourney = true;
        }

        if(getIntent().getStringExtra("TITLE") != null) {
            title = getIntent().getStringExtra("TITLE");
        } else {
            title = startingPoint + " - " + endingPoint;
        }

        if(getIntent().getStringExtra("IS_PLAN_JOURNEY") != null) {
            for(int i = 0; i < Integer.parseInt(getIntent().getStringExtra("PLANNED_WORKS")); i++) {
                ArrayList<String> list = getIntent().getStringArrayListExtra("PLANNED_WORKS_" + i);
                plannedWorks.put("location" + i,
                        new String[] {list.get(0), list.get(1), list.get(2)});
            }

            for(int i = 0; i < Integer.parseInt(getIntent().getStringExtra("CURRENT_INCIDENTS")); i++) {
                ArrayList<String> list = getIntent().getStringArrayListExtra("CURRENT_INCIDENTS_" + i);
                currentIncicents.put("location" + i,
                        new String[] {list.get(0), list.get(1), list.get(2)});
            }

            for(int i = 0; i < Integer.parseInt(getIntent().getStringExtra("CURRENT_WORKS")); i++) {
                ArrayList<String> list = getIntent().getStringArrayListExtra("CURRENT_WORKS_" + i);
                currentWorks.put("location" + i,
                        new String[] {list.get(0), list.get(1), list.get(2)});
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);

        getLocationPermission();
    }

    private void init() {
        geoLocate();
    }

    private void geoLocate() {
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> start = new ArrayList<>();
        List<Address> end = new ArrayList<>();

        try {
            start = geocoder.getFromLocationName(this.startingPoint, 1);
            end = geocoder.getFromLocationName(this.endingPoint, 1);
        } catch (IOException e) {
            Log.e("geolocate", e.getMessage());
        }

        if(start.size() > 0 && end.size() > 0) {
            Address startAddress = start.get(0);
            Address endAddress = end.get(0);
            this.startingLat = startAddress.getLatitude();
            this.startingLon = startAddress.getLongitude();

            this.endingLat = endAddress.getLatitude();
            this.endingLon = endAddress.getLongitude();

        }
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(mLocationPermissionsGranted) {
                Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && task.getResult() != null) {

                            if(isJourney) {
                                moveCameraBetweenTwoPoints();
                            } else {
                                moveCamera(latitudeLongitude, DEFAULT_ZOOM);
                            }
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        mMap.addMarker(options);
    }

    private void moveCameraBetweenTwoPoints() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng starting = new LatLng(startingLat, startingLon);
        LatLng ending = new LatLng(endingLat, endingLon);
        MarkerOptions startMarker = new MarkerOptions().position(starting).title(startingPoint);
        MarkerOptions endMarker = new MarkerOptions().position(ending).title(endingPoint);

        builder.include(starting);
        builder.include(ending);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));

        for(Map.Entry<String, String[]> work : currentWorks.entrySet()) {
            LatLng latLng =
                    new LatLng(Double.valueOf(work.getValue()[0]),
                    Double.valueOf(work.getValue()[1]));
            MarkerOptions marker = new MarkerOptions().position(latLng).title(work.getValue()[2]);
            mMap.addMarker(marker).setIcon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        }

        for(Map.Entry<String, String[]> work : plannedWorks.entrySet()) {
            LatLng latLng =
                    new LatLng(Double.valueOf(work.getValue()[0]),
                            Double.valueOf(work.getValue()[1]));
            MarkerOptions marker = new MarkerOptions().position(latLng).title(work.getValue()[2]);
            mMap.addMarker(marker).setIcon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        }

        for(Map.Entry<String, String[]> incident : currentIncicents.entrySet()) {
            LatLng latLng =
                    new LatLng(Double.valueOf(incident.getValue()[0]),
                            Double.valueOf(incident.getValue()[1]));
            MarkerOptions marker = new MarkerOptions().position(latLng).title(incident.getValue()[2]);
            mMap.addMarker(marker).setIcon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        mMap.addMarker(startMarker).
                setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(endMarker).
                setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mLocationPermissionsGranted) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            if(this.startingPoint != null) {
                init();
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if(
                ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }

        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if(grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }

        }
    }
}
