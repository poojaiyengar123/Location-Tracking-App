package com.example.gpsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    LocationListener locationListener;
    double latitude, longitude, initLatitude;
    TextView lat, lon, address, distance, currentTime, favLocation, favLocationTime, recentLoc1, recentLoc2, recentLoc3;
    List<Address> list, tempList;
    float meters;
    int j, maxTimeIndex1, locIndex, savedIndex;
    long currentTimeSpent, timeSum, maxTime1;
    boolean present;
    ArrayList<Location> locations;
    ArrayList<Long> times, locationTimes, locTimes;
    ArrayList<String> addressList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lat = findViewById(R.id.textView1);
        lon = findViewById(R.id.textView2);
        address = findViewById(R.id.textView3);
        distance = findViewById(R.id.textView4);
        currentTime = findViewById(R.id.textView5);
        favLocation = findViewById(R.id.textView6);
        favLocationTime = findViewById(R.id.textView7);
        recentLoc1 = findViewById(R.id.textView8);
        recentLoc2 = findViewById(R.id.textView9);
        recentLoc3 = findViewById(R.id.textView10);
        locations = new ArrayList<>();
        times = new ArrayList<>();
        locationTimes = new ArrayList<>();
        locTimes = new ArrayList<>();
        addressList = new ArrayList<>();
        maxTime1 = 0;
        maxTimeIndex1 = 0;
        j = 0;
        present = false;

        if (savedInstanceState != null) {
            initLatitude = savedInstanceState.getDouble("initLatitude");
            latitude = savedInstanceState.getDouble("latitude");
            longitude = savedInstanceState.getDouble("longitude");
            meters = savedInstanceState.getFloat("distance");
            j = savedInstanceState.getInt("j");
            currentTimeSpent = savedInstanceState.getLong("currentTime");
            timeSum = savedInstanceState.getLong("timeSum");
            maxTimeIndex1 = savedInstanceState.getInt("maxTimeIndex1");
            maxTime1 = savedInstanceState.getLong("maxTime1");
            locIndex = savedInstanceState.getInt("locIndex");
            savedIndex = savedInstanceState.getInt("savedIndex");
            lat.setText("Latitude: " + savedInstanceState.getDouble("latitude"));
            lon.setText("Longitude: " + savedInstanceState.getString("longitude"));
            locations = (ArrayList<Location>) savedInstanceState.getSerializable("locations");
            times = (ArrayList<Long>) savedInstanceState.getSerializable("times");
            addressList = (ArrayList<String>) savedInstanceState.getSerializable("addressList");
            locTimes = (ArrayList<Long>) savedInstanceState.getSerializable("locTimes");
            locationTimes = (ArrayList<Long>) savedInstanceState.getSerializable("locationTimes");
            distance.setText("Total Distance Traveled: " + savedInstanceState.getFloat("distance") + " meters");
        }

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        onRequestPermissionsResult(0, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, new int [] {PackageManager.PERMISSION_GRANTED});
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
        locations.add(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if (times.size() == 0) {
                    initLatitude = latitude;
                }
                lat.setText("Latitude: " + latitude);
                lon.setText("Longitude: " + longitude);
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
                try {
                    list = geocoder.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 5);
                    address.setText("Address: " + list.get(0).getAddressLine(0));
                    if (addressList.size() == 0)
                        addressList.add(list.get(0).getAddressLine(0));
                    else {
                        if (addressList.get(addressList.size() - 1).equals(list.get(0).getAddressLine(0))) {
                            present = true;
                        }
                        else
                            present = false;
                        if (!present) {
                            addressList.add(list.get(0).getAddressLine(0));
                            present = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                locations.add(location);
                meters += location.distanceTo(locations.get(j));
                j++;
                distance.setText("Total Distance Traveled: " + meters + " meters");
                if (initLatitude != location.getLatitude()) {
                    timeSum = 0;
                    currentTimeSpent = 0;
                    times.clear();
                    initLatitude = location.getLatitude();
                }
                times.add(SystemClock.elapsedRealtime());
                timeSum = times.get(0);
                currentTimeSpent = SystemClock.elapsedRealtime() - timeSum;
                currentTimeSpent *= 0.001;
                locationTimes.add(currentTimeSpent);
                if (locTimes.size() == 0) {
                    locIndex = 2;
                }
                else {
                    locIndex = savedIndex;
                }
                for (int loc = locIndex; loc < locationTimes.size(); loc++) {
                    if (locationTimes.get(loc) == 0 && locationTimes.size() > 1) {
                        if (locationTimes.get(loc - 1) != 0)
                            locTimes.add(locationTimes.get(loc - 1));
                        savedIndex = loc;
                    }
                }
                currentTime.setText("Time Spent at Current Location: " + currentTimeSpent + " seconds");
                maxTime1 = currentTimeSpent;
                for (int t = 0; t < locationTimes.size(); t++) {
                    if (locationTimes.get(t) > maxTime1) {
                        maxTimeIndex1 = t;
                        maxTime1 = locationTimes.get(t);
                    }
                }
                try {
                    tempList = geocoder.getFromLocation(locations.get(maxTimeIndex1).getLatitude(), locations.get(maxTimeIndex1).getLongitude(), 5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //favLocation.setText("Favorite Location: (" + locations.get(maxTimeIndex1).getLatitude() + ", " + locations.get(maxTimeIndex1).getLongitude() + ")");
                favLocation.setText("Favorite Location: " + tempList.get(0).getAddressLine(0));
                favLocationTime.setText("You have spent " + maxTime1 + " seconds at your favorite location!");

                if (addressList.size() == 0) {
                    recentLoc1.setText("");
                    recentLoc2.setText("");
                    recentLoc3.setText("");
                }
                if (addressList.size() >= 1 && locTimes.size() >= 1) {
                    recentLoc1.setText("Recent Location 1: " + locTimes.get(locTimes.size() - 1) + " seconds at " + addressList.get(addressList.size() - 2));
                    recentLoc2.setText("");
                    recentLoc3.setText("");
                }
                if (addressList.size() >= 2 && locTimes.size() >= 3) {
                    recentLoc1.setText("Recent Location 1: " + locTimes.get(locTimes.size() - 1) + " seconds at " + addressList.get(addressList.size() - 2));
                    recentLoc2.setText("Recent Location 2: " + locTimes.get(locTimes.size() - 3) + " seconds at " + addressList.get(addressList.size() - 3));
                    recentLoc3.setText("");
                }
                if (addressList.size() > 2 && locTimes.size() >= 5) {
                    recentLoc1.setText("Recent Location 1: " + locTimes.get(locTimes.size() - 1) + " seconds at " + addressList.get(addressList.size() - 2));
                    recentLoc2.setText("Recent Location 2: " + locTimes.get(locTimes.size() - 3) + " seconds at " + addressList.get(addressList.size() - 3));
                    recentLoc3.setText("Recent Location 3: " + locTimes.get(locTimes.size() - 5) + " seconds at " + addressList.get(addressList.size() - 4));
                }
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("initLatitude", initLatitude);
        outState.putDouble("latitude", latitude);
        outState.putDouble("longitude", longitude);
        outState.putFloat("distance", meters);
        outState.putInt("j", j);
        outState.putInt("maxTimeIndex1", maxTimeIndex1);
        outState.putLong("timeSum", timeSum);
        outState.putLong("currentTime", currentTimeSpent);
        outState.putLong("maxTime1", maxTime1);
        outState.putSerializable("locations", locations);
        outState.putSerializable("times", times);
        outState.putSerializable("addressList", addressList);
        outState.putSerializable("locTimes", locTimes);
        outState.putSerializable("locationTimes", locationTimes);
        outState.putInt("locIndex", locIndex);
        outState.putInt("savedIndex", savedIndex);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

}
