package com.example.a025_071_uts_ga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Notifications extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted {
    // Constants
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TRACKING_LOCATION_KEY = "dapatkan_lokasi";

    // Views
    private Button mLocationButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;

    //Location classes
    private boolean mTrackingLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    EditText latitude, longitude, Nama_Lengkap, No_Whatsapp;
    TextView lokasi;

    LottieAnimationView api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        //Point Variable
        mLocationButton = (Button) findViewById(R.id.btn_location);
      lokasi = (TextView) findViewById(R.id.lokasi);
        latitude = (EditText) findViewById(R.id.et_latitude);
        longitude = (EditText) findViewById(R.id.et_longitude);
        Nama_Lengkap = (EditText) findViewById(R.id.nama_lengkap);
        No_Whatsapp = (EditText) findViewById(R.id.no_whatsapp);
        api = (LottieAnimationView) findViewById(R.id.fire);

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Notifications.this);

        // Restore the state if the activity is recreated.
        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }

        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Home Selected
        bottomNavigationView.setSelectedItemId(R.id.lokasi);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.dashboard:
                        startActivity(new Intent(getApplicationContext()
                                ,Dashboard.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext()
                                ,MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.notifications:
                        return true;
                }
                return false;
            }
        });

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Nama_Lengkap.getText().toString().isEmpty()) {
                    Toast.makeText(Notifications.this, "Masukkan Nama Anda", Toast.LENGTH_SHORT).show();
                } else if (No_Whatsapp.getText().toString().isEmpty()) {
                    Toast.makeText(Notifications.this, "Masukkan No. WhatsApp", Toast.LENGTH_SHORT).show();
                } else {
                    if (!mTrackingLocation) {
                        startTrackLocation();
                    } else {
                        stopTrackingLocation();
                    }
                }
            }
        });

        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation) {
                    new FetchAddressTask(Notifications.this, Notifications.this).execute(locationResult.getLastLocation());
                }
            }
        };
        return;
    }

    private void startTrackLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (!(location == null)) {
                        latitude.setText(String.valueOf(location.getLatitude()));
                        latitude.setEnabled(false);
                        longitude.setText(String.valueOf(location.getLongitude()));
                        longitude.setEnabled(false);
                        Nama_Lengkap.setEnabled(false);
                        No_Whatsapp.setEnabled(false);
                        api.playAnimation();
                    } else {
                        Toast.makeText(Notifications.this, "Aktifkan Lokasi", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Notifications.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            mTrackingLocation = true;
            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(),
                            mLocationCallback,
                            null /* Looper */);
            mLocationButton.setText("STOP");
        }
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mLocationButton.setText("DAPATKAN LOKASI");
            lokasi.setText(R.string.lokasi);
            Nama_Lengkap.setEnabled(true);
            Nama_Lengkap.setText("");
            No_Whatsapp.setText("");
            No_Whatsapp.setEnabled(true);
            latitude.setEnabled(true);
            latitude.setText("");
            longitude.setEnabled(true);
            longitude.setText("");
            api.pauseAnimation();
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:

                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    startTrackLocation();
                } else {
                    Toast.makeText(this,
                            R.string.izin_ditolak,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        if (mTrackingLocation) {
           lokasi.setText(result);
        }
    }

    @Override
    public void onPause() {
        if (mTrackingLocation) {
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mTrackingLocation) {
            startTrackLocation();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (mTrackingLocation) {
            stopTrackingLocation();
        }
        super.onDestroy();
    }
};