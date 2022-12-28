package com.example.android.parentActivity;

import static java.lang.Double.parseDouble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.R;
import com.example.android.parentHome;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.spec.ECField;

public class googleMapsFragment extends Fragment {
    private  final String key_uid="parent_uid";
    private final String shared_pref_name="data";
    private static String parentuid;
    public static  DatabaseReference myRef ;
    private int zoom=15;
    private LatLng locationmark;
    private GoogleMap gMap;
    private SeekBar zoomControl;
    private SupportMapFragment mapFragment;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private FirebaseDatabase database;
    private TextView zoomvalue;
    private ValueEventListener childnameListener,logitudeListener, latitudeListener;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    };




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_google_maps, container, false);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
         mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //create firebase object
         database = FirebaseDatabase.getInstance();
         database.goOnline();
          parentHome.AppContext=root.getContext();
        //shared prefence object
        preferences= getActivity().getSharedPreferences(shared_pref_name,root.getContext().MODE_PRIVATE);
        parentuid=preferences.getString(key_uid,null);
        // zoon bar and value  obj and set max vaLUE
        zoomControl=root.findViewById(R.id.zoomControl);
        zoomControl.setMax(20);
        zoomvalue=root.findViewById(R.id.zoomvalue);
    //GET ZOOM VALUE FROM SHARED PREFENCE
        zoom=preferences.getInt("zoom",0);
        zoomvalue.setText(String.valueOf(zoom));
        zoomControl.setProgress(zoom);
        editor=preferences.edit();

        //SET ON CHANGE LISTENER FOR ZOOM BAR
      zoomControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //ON VALUE CHANGE STORE IT IN SHARED PREFENCE
              editor.putInt("zoom",i);
              editor.commit();
              zoom=preferences.getInt("zoom",0);
              zoomvalue.setText(String.valueOf(zoom));
              //IF LOCATION IS NOT NULL THEN CHANGE ZOOM OF MAP
              if(locationmark!=null) {
                  CameraPosition position = new CameraPosition.Builder().zoom(zoom).target(locationmark).bearing(80).tilt(30).build();
                  //mark on map
                  gMap.addMarker(new MarkerOptions().position(locationmark).title("child is here"));
                  gMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

              }
          }
          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
          }
          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
          }
      });

      //call  link notifier
       LinkNotify();

       //set setting button on click open and close window
      ImageButton setting=root.findViewById(R.id.MapsettingButton);
      RelativeLayout settingpage=root.findViewById(R.id.mapsettingContainer);
      setting.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
             if( settingpage.getTag().toString().equals("hidden"))
              {
                  settingpage.setTag("visible");
                  settingpage.setVisibility(View.VISIBLE);
              }
             else if(settingpage.getTag().toString().equals("visible"))
             {
                 settingpage.setTag("hidden");
                 settingpage.setVisibility(View.GONE);

               }
              }
         });

      //set database reference and start map tracker
         myRef = database.getReference("users");
         startMapTracker();
        }


     public void startMapTracker()
     {
         //get childuid from database


         //get child uid and logitude and latitude
         childnameListener=database.getReference("users").child(parentuid).child("child uid").addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (snapshot.getValue() != null) {

                     //if parent contains child
                     if (snapshot.getValue().toString() != "null") {
                         //get latitude
                         logitudeListener=database.getReference("users").child(snapshot.getValue().toString()).child("longitude").addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                 if (snapshot2.getValue() != null) {
                                     //when successfully get longitude get latitude
                                     latitudeListener=database.getReference("users").child(snapshot.getValue().toString()).child("latitude").addValueEventListener(new ValueEventListener() {
                                         @Override
                                         public void onDataChange(@NonNull DataSnapshot snapshot3) {
                                             //if logitude and latitude is not empty then generate map
                                             if (snapshot3.getValue() != null) {
                                                 if (!snapshot2.getValue().toString().equals("null") || !snapshot3.getValue().toString().equals("null")) {
                                                     if (mapFragment != null) {
                                                         //inside on map ready set mao
                                                         mapFragment.getMapAsync(new OnMapReadyCallback() {
                                                             @Override
                                                             public void onMapReady(@NonNull GoogleMap googleMap) {
                                                                 googleMap.clear();
                                                                 gMap = googleMap;

                                                                 //using logitude and latitude amek latlang and set it on map

                                                                 locationmark = new LatLng(Double.parseDouble(snapshot3.getValue().toString()), Double.parseDouble(snapshot2.getValue().toString()));
                                                                 CameraPosition position = new CameraPosition.Builder().zoom(preferences.getInt("zoom", 0)).target(locationmark).bearing(80).tilt(30).build();
                                                                 //mark on map
                                                                 googleMap.addMarker(new MarkerOptions().position(locationmark).title("child is here"));
                                                                 googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));


                                                             }
                                                         });
                                                     }

                                                 }
                                             }
                                         }

                                         @Override
                                         public void onCancelled(@NonNull DatabaseError error) {
                                         }
                                     });
                                 }
                             }
                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {

                             }
                         });

                     }
                 }
             }
             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }

         });
     }

     //notify if account is not linked
     public  void LinkNotify()
     {
         //link account notifier
         database.getReference("users").child(parentuid).child("child uid").addListenerForSingleValueEvent(new ValueEventListener() {

             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(snapshot.getValue()!=null)
                 {
                     if(snapshot.getValue().toString().equals("null"))
                     {
                         Toast.makeText(parentHome.AppContext, "link account ", Toast.LENGTH_SHORT).show();
                     }
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
     }




    }
