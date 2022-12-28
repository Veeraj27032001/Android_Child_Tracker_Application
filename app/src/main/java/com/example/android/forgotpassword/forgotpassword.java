package com.example.android.forgotpassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.parentActivity.MainActivity;
import com.example.android.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotpassword extends AppCompatActivity  implements View.OnClickListener {
    private EditText emailField;
    private Button ResetButton;
    private TextView GoBackButton;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        emailField=findViewById(R.id.forgotemail);
        ResetButton =findViewById(R.id.ResetButton);
        GoBackButton=findViewById(R.id.goBackButton);
        ResetButton.setOnClickListener(this::onClick);
        GoBackButton.setOnClickListener(this::onClick);

    }

    @Override
    public void onClick(View view) {
 switch (view.getId())
 {
     case R.id.goBackButton:
         startActivity(new Intent(forgotpassword.this, MainActivity.class));
         finish();

         break;
     case R.id.ResetButton:
         String email=emailField.getText().toString();
         if (email.equals("")) {
             emailField.requestFocus();
             emailField.setError("ENTER EMAIL");
         } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
             emailField.requestFocus();
             emailField.setError("ENTER VALID EMAIL");
         }
          else {
             auth=FirebaseAuth.getInstance();
             auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                 @Override
                 public void onComplete(@NonNull Task<Void> task) {
                     Toast.makeText(forgotpassword.this, "RESET link has been sent to your email", Toast.LENGTH_SHORT).show();
                     startActivity(new Intent(forgotpassword.this, MainActivity.class));
                     finish();
                 }
             });
         }
         break;

 }
    }
}