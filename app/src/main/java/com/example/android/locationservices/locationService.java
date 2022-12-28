package com.example.android.locationservices;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.android.childScreen.ChildHome;
import com.example.android.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class locationService extends Service {
    private SharedPreferences preferences;
    private static  final String shared_pref_name="data";
    private static  final String key_uid="parent_uid";
    private static LocationListener listener;
    private static LocationManager manager;
    private   DatabaseReference myRef;
    private  static FirebaseDatabase database;
    private    String myuid;
    public locationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        database = FirebaseDatabase.getInstance();
        database.goOnline();
        preferences=getSharedPreferences(shared_pref_name,MODE_PRIVATE);
        //get user uid
         myuid=preferences.getString(key_uid,null);
         myRef = database.getReference("users").child(myuid);

      //set location listener
        listener=new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //on location change add value to database

                  myRef.child("longitude").setValue(location.getLongitude());
                myRef.child("latitude").setValue(location.getLatitude());

            }
            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("TAG,", "onCreate: \n\n\n\n\n\n\n\n\n\n\n\n\n on status changed");

            }

        };

        manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
        }
        else{
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);
        }


        createNotificationchannel();
        Intent i=new Intent(this, ChildHome.class);
        PendingIntent pending=PendingIntent.getActivity(this,3000,i,PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new NotificationCompat.Builder(this,"channelid")
                .setContentTitle("child tracking application").setContentText("location service is running")
                .setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pending).build();
        startForeground(1001,notification);
        return START_STICKY;
    }
    public void  createNotificationchannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel notificationchannel=new NotificationChannel("channelid","foregroundservice", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationchannel);
        }
    }


    @Override
    public void onDestroy() {
       // stopForeground(true);

        super.onDestroy();
    }
    public static void stopLocationListner()
    {

         if(listener!=null&& manager!=null) {
             Toast.makeText(ChildHome.ChildHomeContext, " stoped ", Toast.LENGTH_SHORT).show();
            manager.removeUpdates(listener);
            database.goOffline();
            manager = null;

        }
    }

}