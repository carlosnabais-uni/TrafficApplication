package org.me.gcu.trafficapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(isServicesOK()) {
            statusCheck();
        }
    }

    public boolean isServicesOK() {
        Log.e(TAG, "Checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS) {
            Log.e(TAG, "GooglePlayServices are working");
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.e(TAG, "an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.e("statusCheck", "statusCheck called");

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("GPS_PROVIDER", "GPS_PROVIDER called");
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void currentIncidents(View view) {
        Intent intent = new Intent(MainActivity.this, CurrentIncidentsActivity.class);
        startActivity(intent);
    }

    public void plannedRoadWorks(View view) {
        Intent intent = new Intent(MainActivity.this, PlannedWorksActivity.class);
        startActivity(intent);
    }

    public void currentRoadWorks(View view) {
        Intent intent = new Intent(MainActivity.this, CurrentWorksActivity.class);
        startActivity(intent);
    }

    public void planJourney(View view) {
        Intent intent = new Intent(MainActivity.this, PlanJourneyActivity.class);
        startActivity(intent);
    }

    public void searchIncidents(View view) {
        Intent intent = new Intent(MainActivity.this, SearchIncidentsAndWorksActivity.class);
        startActivity(intent);
    }
}