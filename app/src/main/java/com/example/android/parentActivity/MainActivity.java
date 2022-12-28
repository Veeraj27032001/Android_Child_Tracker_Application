package com.example.android.parentActivity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.R;
import com.example.android.childScreen.ChildHome;
import com.example.android.forgotpassword.forgotpassword;
import com.example.android.parentHome;
import com.example.android.signup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // firebase authenticator variable
    private FirebaseAuth mAuth;
// variable for shared preference
    private SharedPreferences preferences;
    private static  final String shared_pref_name="data";
    private static  final String key_pass="pass";
    private static  final String key_email="email";
    private static  final String key_name="name";
    private static  final String key_uid="parent_uid";
    private static  final String key_state="state";
    private static String uid;
    private String account_type="none";
    private String name="none";
    private Button loginButton;
    private TextView registerButton;
    private TextView forgotPasswordButton;
    private TextView ChildLoginButton;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // link  button object to ui wigits
        registerButton=(TextView) findViewById(R.id.registerButton);
        forgotPasswordButton=(TextView) findViewById(R.id.forgot);
        loginButton=(Button) findViewById(R.id.loginButton);
         ChildLoginButton=(TextView) findViewById(R.id.childLogin);

        // getting data from shared prefrence
        preferences=getSharedPreferences(shared_pref_name,MODE_PRIVATE);
        String state=preferences.getString(key_state,null);
        String uuid=preferences.getString(key_uid,null);

        //if  already loged is is notnull
        if(state!=null)
        {
            // if loged in as parent then transfer control to parent page

            if(state.equals("parent")){
                startActivity(new Intent(MainActivity.this, parentHome.class));
                finish();

            }
            //if loged in as child then pass control to child home
            else if(state.equals("child"))
            {
                startActivity(new Intent(MainActivity.this, ChildHome.class));
                finish();
            }
        }

       //set on click event listner for buttons

        registerButton.setOnClickListener(this::onClick);
        forgotPasswordButton.setOnClickListener(this::onClick);
        loginButton.setOnClickListener(this::onClick);
        ChildLoginButton.setOnClickListener(this::onClick);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            // login button
            case R.id.loginButton:
                // linking text field objects to ui views
                EditText emailField=(EditText)findViewById(R.id.loginemail);
                EditText PasswordField=(EditText)findViewById(R.id.loginpassword);

                //get field data
                String email=emailField.getText().toString();
                String password= PasswordField.getText().toString();
                //call login function
                userLogin(email,password,emailField,PasswordField);
                break;

                //registration code

            case R.id.registerButton:
            //pass control to registration page and destroy previous page using finsh
                Intent register=new Intent(this, signup.class);
                startActivity(register);
                finish();
                break;

            //forgot password
            case R.id.forgot:
                startActivity( new Intent(this, forgotpassword.class));
                finish();
                 break;
            // child login button click
            case R.id.childLogin:
                //passes contol to child login page
                Intent childLogin=new Intent(this, com.example.android.childScreen.childLogin.class);
                startActivity(childLogin);
                finish();
                break;
        }
    }
    public void userLogin(String email,String password,EditText emailField,EditText PasswordField)
    {
// email and password requirement  verify
        if (email.equals("")) {
            emailField.requestFocus();
            emailField.setError("ENTER EMAIL");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.requestFocus();
            emailField.setError("ENTER VALID EMAIL");

        }  else if (password.equals("") || password.length() < 6) {
            PasswordField.requestFocus();
            PasswordField.setError("ENTER VALID PASSWORD");
        }
        // if all condition met then proceed  to login
        else{

            // create progress bar object and link it to the ui view
            ProgressBar bar=(ProgressBar) findViewById(R.id.loginprogress);
            //make progress bar visible
            bar.setVisibility(ProgressBar.VISIBLE);

            //disable buttons
            forgotPasswordButton.setEnabled(false);
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
            ChildLoginButton.setEnabled(false);

            //creating a instace  of firebase autenticator
            mAuth = FirebaseAuth.getInstance();
            //sign in with email and password
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    // if successfully autgenticated then store data to real time dataabse
                    if (task.isSuccessful()) {

                        //get  firebase authenticator  user unique id
                         uid=mAuth.getCurrentUser().getUid();

                         //create firebase instance
                         database = FirebaseDatabase.getInstance();
                        database.goOnline();
                         myRef = database.getReference("users");

                         //listnes for single value event
                         myRef.child(uid).child("account type").addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {

                                //if account type is parent then store information in database
                                 if (snapshot.getValue().toString().equals("parent")) {
                                    //storing data in shared preference
                                    preferences = getSharedPreferences(shared_pref_name, MODE_PRIVATE);

                                    //creating shared prefernce ediotr object and putting data  into it
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(key_email, email);
                                    editor.putString(key_pass, password);
                                    editor.putString(key_uid, uid);
                                    editor.putString(key_state, "parent");
                                     editor.putInt("zoom", 15);
                                    editor.commit();

                                    //get user name from realtime database using singletime lsitener
                                     myRef.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                         @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot2) {

                                            //store name into preference
                                             editor.putString(key_name,snapshot2.getValue().toString() );
                                             editor.commit();
                                         }
                                         @Override
                                         public void onCancelled(@NonNull DatabaseError error) {
                                         }
                                     });// get name from database close

                                    //hide progress bar
                                     bar.setVisibility(ProgressBar.GONE);
                                     //enable buttons
                                     forgotPasswordButton.setEnabled(true);
                                     loginButton.setEnabled(true);
                                     ChildLoginButton.setEnabled(true);
                                     registerButton.setEnabled(true);
                                     // need to maintain multiple account so sign out and use this only for verification
                                    mAuth.signOut();
                                    //pass control to parent home and finish login page
                                     Intent i = new Intent(MainActivity.this, parentHome.class);
                                     startActivity(i);
                                     finish();
                                 }
                                //if account infromation povided is child then make toast message saying login in child login page
                                 else if (snapshot.getValue().toString().equals("child")) {
                                     bar.setVisibility(ProgressBar.GONE);
                                    //enable buttons
                                     forgotPasswordButton.setEnabled(true);
                                     ChildLoginButton.setEnabled(true);
                                     loginButton.setEnabled(true);
                                     registerButton.setEnabled(true);

                                    //clear all field data
                                     emailField.getText().clear();
                                     PasswordField.getText().clear();

                                    //make toast
                                     Toast.makeText(MainActivity.this, " Log in in child log in ", Toast.LENGTH_SHORT).show();
                                     bar.setVisibility(ProgressBar.GONE);
                                     //enable button
                                     forgotPasswordButton.setEnabled(true);
                                     ChildLoginButton.setEnabled(true);
                                     loginButton.setEnabled(true);
                                     registerButton.setEnabled(true);
                                }
                                 else {
                                     Toast.makeText(MainActivity.this, " Something went wrong! ", Toast.LENGTH_SHORT).show();

                                 }
                            }//get account type from database on datachange clode

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });//get account type from database close

                    }//login sucessfull  bodyclose

                    // check why login failed and make toast
                    else{
                        //create conectivity nmanager object and make toast
                        ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        //if  connection problem then make toast saying check your internet connection
                        if (cm.getActiveNetworkInfo() == null) {
                            Toast.makeText(MainActivity.this, "check your internet connection and try again ", Toast.LENGTH_SHORT).show();
                            bar.setVisibility(ProgressBar.GONE);
                            //enable buttons
                            forgotPasswordButton.setEnabled(true);
                            loginButton.setEnabled(true);
                            ChildLoginButton.setEnabled(true);
                            registerButton.setEnabled(true);
                            ChildLoginButton.setEnabled(true);
                        }
                        else {
                            Toast.makeText(MainActivity.this, " unsucessfull", Toast.LENGTH_SHORT).show();
                            bar.setVisibility(ProgressBar.GONE);
                            //enable buttons
                            forgotPasswordButton.setEnabled(true);
                            loginButton.setEnabled(true);
                            registerButton.setEnabled(true);
                            ChildLoginButton.setEnabled(true);

                        }

                    }// if login sucessfull else body close
                }//on complete sign in with email close
            });// sigin with email and password close
        }// data condition else close
    }// userlogin function close
}