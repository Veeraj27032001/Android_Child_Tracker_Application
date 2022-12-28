package com.example.android.childScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.parentActivity.MainActivity;
import com.example.android.R;
import com.example.android.locationservices.locationBackgroundService;
import com.example.android.locationservices.locationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChildHome extends AppCompatActivity implements View.OnClickListener {
   private FirebaseDatabase database ;
   private DatabaseReference myRef;
    private SharedPreferences preferences;
    private String uid;
    static  final String shared_pref_name="data";
    private FirebaseAuth mAuth;
    private String pref_email;
    static  final String key_uid="parent_uid";
    private LinearLayout menuecontainer;
    private   ImageButton menuButton;
    private RelativeLayout tutorialcontainer;
    private Button NextButton;
    private ImageView ImageContainer;
    private TextView textTutorial;
    private LinearLayout  homecontainer;
    public static Context ChildHomeContext;
    private FrameLayout menuButtonContainer;
    private Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);
        //database obj
        ChildHomeContext=ChildHome.this;
        database = FirebaseDatabase.getInstance();
        database.goOnline();
       myRef = database.getReference("users");
        //logout obj
        logout=(Button) findViewById(R.id.childlogoutButton);
        //menue button container
        menuButtonContainer=findViewById(R.id.menuButtonContainer);
        //shared prefence email and pass;
        preferences=getSharedPreferences(shared_pref_name,MODE_PRIVATE);
         pref_email=preferences.getString("email",null);
        uid=preferences.getString("parent_uid",null);
        //tutorial objects
         tutorialcontainer=findViewById(R.id.TutorialContainer);
        NextButton=findViewById(R.id.tutorialControlButton);
        ImageContainer=findViewById(R.id.imageTutorial);
        textTutorial=findViewById(R.id.textTutorial);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                Button TutorialButton = findViewById(R.id.childTutorialButton);
                TutorialButton.setVisibility(View.VISIBLE);
                TutorialButton.setOnClickListener(this::onClick);
            }

        //layput objects
        RelativeLayout profilecontainer=findViewById(R.id.childprofilecontainer);
        homecontainer=findViewById(R.id.childhomeContainer);

        //on child log out click
         logout.setOnClickListener(this::onClick);

        //when user enters set service is running
        TextView status=(TextView)findViewById(R.id.servicestatus);
        status.setText("service is running");
        Toast.makeText(this, "log out to stop service", Toast.LENGTH_SHORT).show();

        //menue code
        menuecontainer=(LinearLayout)findViewById(R.id.menuContainer);
       menuButton=(ImageButton)findViewById(R.id.menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getTag().toString().equals("openmenu"))
                {
                    //on open hide all layputs and set tag to close
                    homecontainer.setVisibility(View.GONE);
                    profilecontainer.setVisibility(View.GONE);
                    menuecontainer.setVisibility(View.VISIBLE);
                    view.setBackgroundColor(getResources().getColor(R.color.lightblue));
                    view.setTag("closemenu");
                    menuButtonContainer.setBackgroundColor(getResources().getColor(R.color.lightblue));
                }
                else if(view.getTag().toString().equals("closemenu")){
                    //on close menu hide menue and make home pagevisible
                    menuecontainer.setVisibility(View.GONE);
                    view.setTag("openmenu");
                    view.setBackgroundColor(getResources().getColor(R.color.darkblue));
                    menuButtonContainer.setBackgroundColor(getResources().getColor(R.color.darkblue));
                    homecontainer.setVisibility(View.VISIBLE);
                }
            }
        });

        //on home click
        Button Home=(Button) findViewById(R.id.childhomeHomeButton);
        Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show home container and perform automatic menu click to close menu
                profilecontainer.setVisibility(View.GONE);
                menuButton.performClick();
                homecontainer.setVisibility(View.VISIBLE);

            }
        });

        //to open profile
        TextView profilename=findViewById(R.id.childprofileName);
        TextView profileemail=findViewById(R.id.childProfileEmail);
        profilename.setMovementMethod(new ScrollingMovementMethod());
        profileemail.setMovementMethod(new ScrollingMovementMethod());

        Button profile=(Button) findViewById(R.id.childhomeprofileButton);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.goOnline();
                //hide other containers
                profilecontainer.setVisibility(View.VISIBLE);
                menuButton.performClick();
                homecontainer.setVisibility(View.GONE);
                profileemail.setText(pref_email);

                //get name  and email from database
                myRef.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            profilename.setText(snapshot.getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        // delete account button
    Button deletemyAccount=(Button)findViewById(R.id.childprofiledeleteMyAccount);
    deletemyAccount.setOnClickListener(new View.OnClickListener() {
    @Override
    //pn delete button click check weather account is linked to parent
    public void onClick(View view) {
        //stop running service

        myRef.child(uid).child("parent uid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null)
                {
                    //if linked to parent call toast
                    if(!snapshot.getValue().toString().equals("null"))
                    {
                        Toast.makeText(ChildHome.this, "Account is linked to the parent account", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            locationService.stopLocationListner();
                            stopService(new Intent(ChildHome.this,locationService.class));
                        }
                        else{
                            locationBackgroundService.stopLocationListner();
                            stopService(new Intent(ChildHome.this,locationBackgroundService.class));
                        }

                        //get email pass from sharedprefence
                        String parent_uid=preferences.getString(key_uid,null);
                        SharedPreferences.Editor editor=preferences.edit();
                        String pass= preferences.getString("pass",null);
                        String email=preferences.getString("email",null);

                        //sign in to account and delete account
                        mAuth = FirebaseAuth.getInstance();
                        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                database.goOnline();
                                                myRef.child(parent_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        editor.clear();
                                                        editor.commit();
                                                        Toast.makeText(ChildHome.this, "  successfully deleted account", Toast.LENGTH_SHORT).show();

                                                        startActivity(new Intent(ChildHome.this, MainActivity.class));
                                                        finish();
                                                    }
                                                });
                                            }
                                            else{
                                                //if network went off restarts service
                                                Context context = getApplicationContext();
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    database.goOnline();
                                                    context.startForegroundService(new Intent(ChildHome.this,locationService.class));
                                                }
                                                else {
                                                    startService(new Intent(ChildHome.this,locationBackgroundService.class));
                                                }
                                                Toast.makeText(ChildHome.this, "  something went wrong", Toast.LENGTH_SHORT).show();

                                            }
                                        }});

                                }}});
                    }}}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }});


    }});
        Context context = getApplicationContext();
        //ask for permisions
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(ChildHome.this, new String[]{Manifest.permission.INTERNET,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},PackageManager.PERMISSION_GRANTED);
            ActivityCompat.requestPermissions(ChildHome.this, new String[]{Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND},102);
            ActivityCompat.requestPermissions(ChildHome.this, new String[]{Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND},103);ActivityCompat.requestPermissions(ChildHome.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},103);
            ActivityCompat.shouldShowRequestPermissionRationale(ChildHome.this,Manifest.permission.ACCESS_COARSE_LOCATION);
            ActivityCompat.shouldShowRequestPermissionRationale(ChildHome.this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tutorialcontainer.setVisibility(View.VISIBLE);
                textTutorial.setText("long press app icon and go to the child tracker app info and select  permissions ");
                NextButton.setOnClickListener(this::onClick);



            }
        }


        //start service base ond sdk verion

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.startForegroundService(new Intent(ChildHome.this,locationService.class));

        }
        else {
           locationBackgroundService.stopLocationListner();
           stopService(new Intent(ChildHome.this,locationBackgroundService.class));
               startService(new Intent(ChildHome.this,locationBackgroundService.class));
        }


    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.childlogoutButton:

                //set loged in false so we can login in other device
                myRef.child(uid).child("loged in").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear();
                            editor.commit();
                            //stop service based on sdk version
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                locationService.stopLocationListner();
                                stopService(new Intent(ChildHome.this,locationService.class));

                            }
                            else{
                                locationBackgroundService.stopLocationListner();
                                stopService(new Intent(ChildHome.this,locationBackgroundService.class));
                            }

                            //pass control to login page
                            startActivity(new Intent(ChildHome.this,MainActivity.class));
                            finish();
                        }

                    }
                });
                break;
            case R.id.tutorialControlButton:
                if(NextButton.getTag().toString().equals("tutorial1"))
                {
                    ImageContainer.setImageDrawable(getResources().getDrawable(R.drawable.tutorial2));
                    textTutorial.setText("select location ");

                    NextButton.setTag("tutorial2");
                }
                else if(NextButton.getTag().toString().equals("tutorial2"))
                {
                    ImageContainer.setImageDrawable(getResources().getDrawable(R.drawable.tutorial3));
                    NextButton.setTag("tutorial3");
                    textTutorial.setText("select allow all the time");
                    NextButton.setText("EXIT");

                }
                else if(NextButton.getTag().toString().equals("tutorial3"))
                {
                 tutorialcontainer.setVisibility(View.GONE);
                 NextButton.setTag("tutorial1");
                    textTutorial.setText("go to the child tracker app info and select  permissions ");
                     NextButton.setText("Next");
                    ImageContainer.setImageDrawable(getResources().getDrawable(R.drawable.tutorial1));
                    menuButton.setBackgroundColor(getResources().getColor(R.color.darkblue));
                    menuButtonContainer.setBackgroundColor(getResources().getColor(R.color.darkblue));
                    menuecontainer.setVisibility(View.GONE);
                    menuButton.setTag("openmenu");
                    homecontainer.setVisibility(View.VISIBLE);


                }
                break;
            case R.id.childTutorialButton:
                tutorialcontainer.setVisibility(View.VISIBLE);
                textTutorial.setText("long press app icon and go to the child tracker app info and select  permissions ");
                NextButton.setOnClickListener(this::onClick);
                break;
        }
        }


}
