package com.example.android.locationservices;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class locationBackgroundService extends Service {

    SharedPreferences preferences;
    static  final String shared_pref_name="data";
    static  final String key_uid="parent_uid";
    private static LocationListener listener;
    private static LocationManager manager;
    public locationBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        preferences=getSharedPreferences(shared_pref_name,MODE_PRIVATE);
        String myuid;
        preferences= getSharedPreferences(shared_pref_name,MODE_PRIVATE);
        myuid=preferences.getString(key_uid,null);
        DatabaseReference myRef = database.getReference("users").child(myuid);


         listener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                myRef.child("longitude").setValue(location.getLongitude());
                myRef.child("latitude").setValue(location.getLatitude());
                }
             @Override
             public void onStatusChanged(String provider, int status, Bundle extras) {

             }

        };
         manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
        }
        else{

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);
        }
        super.onCreate();
    }
    public static void stopLocationListner()
    {
            if(manager!=null&&listener!=null) {
                manager.removeUpdates(listener);
                manager = null;
            }

    }
}