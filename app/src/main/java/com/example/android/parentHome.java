package com.example.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.ParentChildActivity.linkchild;
import com.example.android.parentActivity.MainActivity;
import com.example.android.parentActivity.googleMapsFragment;
import com.example.android.parentActivity.profileFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class parentHome extends AppCompatActivity implements View.OnClickListener {
    public static Context AppContext;
    SharedPreferences preferences;
    private String key_childuid="child_uid";
    private static  final String key_uid="parent_uid";
    private static  final String shared_pref_name="data";
    private  String parentuid;
    private ValueEventListener deletedListner=null;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ImageButton parentHomeButton;
    private ImageButton addChildButton;
    private  ImageButton parentProfiledButton;
    private  FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        // call map fragment as first page
        manager=getSupportFragmentManager();
        FragmentTransaction transactionManager= manager.beginTransaction();
        transactionManager.replace(R.id.parentFragmentContainer,new googleMapsFragment());
        transactionManager.commit();
        
        //get parent uid from sahared prefence
        preferences=getSharedPreferences(shared_pref_name,MODE_PRIVATE);
        parentuid=preferences.getString(key_uid,null);

        SharedPreferences.Editor editor=preferences.edit();
        
        //create databse instance
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

// delete listner always listes to on database account delted  if delete then automatically logouts from device and 
     deletedListner=myRef.child(parentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if data field does not exists then clear data and logout
                if(!snapshot.child("email").exists())
                {
                    //clear shared prefernce
                    editor.clear();
                    editor.commit();
                    Toast.makeText(parentHome.this,"account has been deleted  ",Toast.LENGTH_SHORT).show();

                    // deletes this listner
                    myRef.removeEventListener(deletedListner);
                   //pass control to parent home
                    startActivity(new Intent(parentHome.this, MainActivity.class));
                    finish();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        //create view refernce object
         parentHomeButton=(ImageButton) findViewById(R.id.ParentHomeButton);
         addChildButton=(ImageButton) findViewById(R.id.addChildButton);
         parentProfiledButton=(ImageButton) findViewById(R.id.parentProfile);

         //set event k=listner for buttons
         parentHomeButton.setOnClickListener(this::onClick);
         addChildButton.setOnClickListener(this::onClick);
         parentProfiledButton.setOnClickListener(this::onClick);


    }

    @Override
    public void onClick(View view) {


        FragmentTransaction transactionManager= manager.beginTransaction();
        switch (view.getId())
        {

            case R.id.addChildButton:
                //on add child clicked
                // check weather child is account already linked if not linked thn pass contol to link child  page
                myRef.child(parentuid).child("linked").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.getValue() != null) {
                            if (snapshot.getValue().toString().equals("false")) {
                                // if linked false then pass contol to link child home
                                transactionManager.replace(R.id.parentFragmentContainer, new linkchild());
                                transactionManager.commit();
                            }
                            else if (snapshot.getValue().toString().equals("true")) {
                                // if already have linked account then show toast message
                                Toast.makeText(parentHome.this, " already have child account remove child account in profile before proceeding ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                 break;
            case R.id.parentProfile:
                //on profile click add profile page
                transactionManager.replace(R.id.parentFragmentContainer,new profileFragment());
                transactionManager.commit();
                break;
            case R.id.ParentHomeButton:
                //on home click add map fragment
                transactionManager.replace(R.id.parentFragmentContainer,new googleMapsFragment());
                transactionManager.commit();
                break;
        }
    }

}