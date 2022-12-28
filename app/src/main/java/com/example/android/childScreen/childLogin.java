package com.example.android.childScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.parentActivity.MainActivity;
import com.example.android.R;
import com.example.android.forgotpassword.forgotpassword;
import com.example.android.functions.RigistrationValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class childLogin extends AppCompatActivity  implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private static String account_type="none";
    private SharedPreferences preferences;
    static  final String shared_pref_name="data";
    static  final String key_pass="pass";
    static  final String key_email="email";
    static  final String key_uid="parent_uid";
    static  final String key_state="state";
    private   TextView forgotPasswordButton;
    private Button loginButton;
    private TextView ParentLoginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);
        preferences=getSharedPreferences(shared_pref_name,MODE_PRIVATE);
        //create object for buttons
         forgotPasswordButton=(TextView) findViewById(R.id.Childforgot);
         loginButton=(Button) findViewById(R.id.ChildLoginButton);
         ParentLoginButton=(TextView) findViewById(R.id.ParentLogin);

        //add event listener
        forgotPasswordButton.setOnClickListener(this::onClick);
        loginButton.setOnClickListener(this::onClick);
        ParentLoginButton.setOnClickListener(this::onClick);

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.Childforgot:
                startActivity(new Intent(this, forgotpassword.class));
                finish();

                break;

            case R.id.ParentLogin:
                //on
                Intent parentLogin=new Intent(this, MainActivity.class);
                startActivity(parentLogin);
                finish();
                break;
            case R.id.ChildLoginButton:
                // create edit text objects
                EditText emailField=(EditText)findViewById(R.id.Childloginemail);
                EditText PasswordField=(EditText)findViewById(R.id.Childloginpassword);
                //get dat from field
                String email=emailField.getText().toString();
                String password= PasswordField.getText().toString();
                //create validation object and check validaaion
                RigistrationValidation validation=new RigistrationValidation();
                if(!validation.checkEmail(email,emailField)||!validation.checkPassword(password,PasswordField))
                {

                }
                else {
                    //if all requirement met then create authenticator instance
                    ProgressBar bar=(ProgressBar) findViewById(R.id.Childloginprogress);
                    bar.setVisibility(ProgressBar.VISIBLE);
                    forgotPasswordButton.setEnabled(false);
                    ParentLoginButton.setEnabled(false);
                    loginButton.setEnabled(false);


                    mAuth = FirebaseAuth.getInstance();
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //if task is sucessfully signed in thn check ofr type of account
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();
                                //create database instance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                database.goOnline();
                                DatabaseReference myRef = database.getReference("users");
                                //get account type from database
                                myRef.child(uid).child("account type").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //if account ype parent then doesnot transfer control
                                        if(snapshot.getValue().toString().equals("parent")) {
                                            Toast.makeText(childLogin.this, " Log in in Parent log in ", Toast.LENGTH_SHORT).show();
                                            bar.setVisibility(ProgressBar.GONE);
                                            forgotPasswordButton.setEnabled(true);
                                            ParentLoginButton.setEnabled(true);
                                            loginButton.setEnabled(true);


                                        }
                                        else if(snapshot.getValue().toString().equals("child")) {
                                            //check from database weather account has alredy been loged in ina device
                                            myRef.child(uid).child("loged in").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot logsnapshot) {
                                                    //if already loged in a device donot transfer control
                                                    if(logsnapshot.getValue().toString().equals("true"))
                                                    {
                                                        Toast.makeText(childLogin.this, " alraedy loged in a device", Toast.LENGTH_SHORT).show();
                                                        bar.setVisibility(ProgressBar.GONE);
                                                        forgotPasswordButton.setEnabled(true);
                                                        ParentLoginButton.setEnabled(true);
                                                        loginButton.setEnabled(true);


                                                    }
                                                    else  if(logsnapshot.getValue().toString().equals("false")){
                                                        // store data in prefernces
                                                        preferences=getSharedPreferences(shared_pref_name,MODE_PRIVATE);
                                                        SharedPreferences.Editor editor=preferences.edit();
                                                        editor.putString(key_email,email);
                                                        editor.putString(key_pass,password);
                                                        editor.putString(key_uid,uid);
                                                        editor.putString(key_state,"child");
                                                        editor.commit();
                                                        //set loged in to true so that multiple device cannot login
                                                        myRef.child(uid).child("loged in").setValue("true");

                                                        //hide progress bafr and enable buttons
                                                         bar.setVisibility(ProgressBar.GONE);
                                                        forgotPasswordButton.setEnabled(true);
                                                        ParentLoginButton.setEnabled(true);
                                                        loginButton.setEnabled(true);

                                                        Toast.makeText(childLogin.this, " loged in successfully", Toast.LENGTH_SHORT).show();

                                                        Intent i=new Intent(childLogin.this, ChildHome.class);
                                                        startActivity(i);
                                                        finish();

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });


                                        }
                                        else{
                                            Toast.makeText(childLogin.this, "something went wrong try again", Toast.LENGTH_SHORT).show();
                                            bar.setVisibility(ProgressBar.GONE);
                                            forgotPasswordButton.setEnabled(true);
                                            ParentLoginButton.setEnabled(true);
                                            loginButton.setEnabled(true);

                                        }



                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                            else{
                                //if login failed check for reason
                                ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                                if (cm.getActiveNetworkInfo() == null) {
                                    Toast.makeText(childLogin.this," check your internet connection and try again"+account_type,Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(ProgressBar.GONE);
                                    forgotPasswordButton.setEnabled(true);
                                    ParentLoginButton.setEnabled(true);
                                    loginButton.setEnabled(true);

                                } else {
                                    Toast.makeText(childLogin.this," unsucessfull",Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(ProgressBar.GONE);
                                    forgotPasswordButton.setEnabled(true);
                                    ParentLoginButton.setEnabled(true);
                                    loginButton.setEnabled(true);

                                }

                            }
                        }
                    });

                }
                    break;
        }
    }
}