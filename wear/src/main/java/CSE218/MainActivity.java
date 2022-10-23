package CSE218;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import CSE218.wearapp2.R;

public class MainActivity extends WearableActivity {

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 34;
    private WifiManager wifiManager;
    private HeartRateListener heartrateListener;
    private StepCountListener stepCountListener;
    public TextView heartView;
    public TextView stepView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        this.heartView = (TextView) findViewById(R.id.text4);
        this.stepView = (TextView) findViewById(R.id.text61);
        ImageButton ib = (ImageButton) findViewById(R.id.myButton);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        heartrateListener = new HeartRateListener(this, this.heartView);
        heartrateListener.startSensor();

        stepCountListener = new StepCountListener(this, this.stepView);
        stepCountListener.startSensor();


        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView mainTextView = (TextView) findViewById(R.id.mainText);
                mainTextView.setText("Updating...");
                getHeartBeat();
                getCurrentLocation();

            }
        });
        setAmbientEnabled();
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        ;
        permissions.add(Manifest.permission.VIBRATE);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.BODY_SENSORS);

        boolean status = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Break if at least one permission is missing
                status = false;
                break;
            }
        }

        if (!status) {
            requestPermissions();
        }

        Log.v("permissions", "Permissions have been granted");

    }

    private void requestPermissions() {
        Log.v("permissions", "Requesting permissions");
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.VIBRATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BODY_SENSORS},
                ACTIVITY_RECOGNITION_REQUEST_CODE);
        Log.v("permissions", "Requested permissions successfully");
    }

    private void getHeartBeat() {
        // Get heart beat from listener on click
        TextView mTextView = (TextView) findViewById(R.id.text4);
        double heartRate = heartrateListener.getHeartRate();
        mTextView.setText("" + heartRate + " bpm");
        Log.v("heartSensor", "onClick: " + heartRate);
    }

    private void getCurrentLocation() {
        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        // Get WiFi strength details
        int rssi = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(rssi, 5);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        String MacAddr = wifiManager.getConnectionInfo().getMacAddress();
        TextView wifiTextView = (TextView) findViewById(R.id.mainText2);
        wifiTextView.setText(rssi + "dBm level " + level + "\n ssid " + ssid);
        Log.v("wifiSensor", "rssi " + rssi + " level " + level);

        // For location -> update when we get latitude and longitude
        Log.v("locationSensor", "updating");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1);
        locationRequest.setFastestInterval(3);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(getApplicationContext())
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestlocIndex = locationResult.getLocations().size() - 1;
                            double lati = locationResult.getLocations().get(latestlocIndex).getLatitude();
                            double longi = locationResult.getLocations().get(latestlocIndex).getLongitude();
                            double alti = locationResult.getLocations().get(latestlocIndex).getAltitude();
                            Log.v("locationSensor", String.format("Latitude : %s\n Longitude: %s", lati, longi));
                            Log.v("locationSensor", "Altitude : " + alti);

                            TextView mTextView = (TextView) findViewById(R.id.text);
                            TextView mTextView2 = (TextView) findViewById(R.id.text3);
                            TextView mTextView3 = (TextView) findViewById(R.id.text5);

                            mTextView.setText("" + lati + "");
                            mTextView2.setText("" + longi + "");
                            mTextView3.setText("" + (int) alti + " m");
                            TextView mainTextView = (TextView) findViewById(R.id.mainText);
                            mainTextView.setText("Click to update");
                            Log.v("locationSensor", "updated");
                        }
                    }
                }, Looper.getMainLooper());
    }

}
