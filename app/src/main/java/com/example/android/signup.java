package com.example.android;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.functions.RigistrationValidation;
import com.example.android.parentActivity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class signup extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private TextView parentLogin;
    private  Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_signup);
      parentLogin = (TextView) findViewById(R.id.alredyhaveAccount);
        signup = (Button) findViewById(R.id.signupButton);
        //set event listener for button
        parentLogin.setOnClickListener(this::onClick);
        signup.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alredyhaveAccount:
                // if alreay have account clicke dpass control to mainactivity
                Intent login = new Intent(this, MainActivity.class);
                startActivity(login);
                finish();
                break;
            case R.id.signupButton:
                // create authenticator instance
                mAuth = FirebaseAuth.getInstance();
                //create  view reference object
                EditText emailField = findViewById(R.id.signupEmail);
                EditText nameField = findViewById(R.id.signupName);
                EditText PasswordField = findViewById(R.id.signupPassword);
                EditText RePasswordField = findViewById(R.id.signupRePassword);
                //get data from detail
                String Email = emailField.getText().toString();
                String name = nameField.getText().toString();
                String password = PasswordField.getText().toString();
                String repassword = RePasswordField.getText().toString();
                RigistrationValidation validation=new RigistrationValidation();
            //validate
                if(!validation.checkEmail(Email,emailField)||!validation.checkName(name,nameField)||!validation.checkPassword(password,PasswordField)||!validation.checkRepass(repassword,password,RePasswordField))
                {

                }
                else {
                    ProgressBar bar = findViewById(R.id.signupprogress);
                    bar.setVisibility(view.VISIBLE);
                    signup.setEnabled(false);
                    parentLogin.setEnabled(false);
                    //create user with email
                    mAuth.createUserWithEmailAndPassword(Email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //create object of database
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users");
                                //store user information in database
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("email").setValue(Email);
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("account type").setValue("parent");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("child uid").setValue("null");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("linked").setValue("false");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(name)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //make toast saying registration successfully
                                                    Toast.makeText(signup.this, "registration successful ", Toast.LENGTH_LONG).show();
                                                    bar.setVisibility(view.GONE);
                                                    signup.setEnabled(true);
                                                    parentLogin.setEnabled(true);
                                                    //transfer control to log in page
                                                    Intent loginpage=new Intent(signup.this,MainActivity.class);
                                                    startActivity(loginpage);
                                                    finish();
                                                }
                                                else {
                                                    Toast.makeText(signup.this, "user registration failed ", Toast.LENGTH_SHORT).show();
                                                    bar.setVisibility(view.GONE);
                                                    signup.setEnabled(true);
                                                    parentLogin.setEnabled(true);
                                                }
                                            }
                                        });
                            }
                            else {
                                //check why registration failed
                                ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                if (cm.getActiveNetworkInfo() == null) {
                                    Toast.makeText(signup.this, "check your internet connection and try again ", Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(view.GONE);
                                    signup.setEnabled(true);
                                    parentLogin.setEnabled(true);
                                } else {
                                    Toast.makeText(signup.this, "existing user email ", Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(view.GONE);
                                    signup.setEnabled(true);
                                    parentLogin.setEnabled(true);
                                }

                            }
                        }
                    });

                }
                break;
        }
    }
}
