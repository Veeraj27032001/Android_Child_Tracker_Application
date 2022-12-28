package com.example.android.functions;

import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Pattern;

public class RigistrationValidation {
 //password validation
 public boolean checkPassword(String password, EditText passField)
 {
  //patterns
  Pattern uppercase = Pattern.compile("[A-Z]");
  Pattern lowercase = Pattern.compile("[a-z]");
  Pattern digit = Pattern.compile("[0-9]");
  Pattern SpecialChar=Pattern.compile("[~'!@#$%?\\\\\\\\/&*\\\\]|\\\\[=()}\\\"{+_:;,.><'-]");
  //password length check
  if (password.equals("") || password.length() < 6) {
   passField.requestFocus();
   passField.setError("ENTER VALID PASSWORD");
   return  false;
  }
  //if password legth is greater then 6 then check for other requirement
  else {
   // if lowercase character is not present
   if (!lowercase.matcher(password).find()) {
    passField.requestFocus();
    passField.setError("PASSWORD SHOULD CONTAIN LOWERCASE");
    return  false;
   }
   // if uppercase character is not present
   else if (!uppercase.matcher(password).find()) {
    passField.requestFocus();
    passField.setError("PASSWORD SHOULD CONTAIN UPPERCASE");
    return  false;
   }
   // if digit is not present
   else if (!digit.matcher(password).find()) {
    passField.requestFocus();
    passField.setError("PASSWORD SHOULD CONTAIN NUMBER");
    return  false;
   }
   else if (!SpecialChar.matcher(password).find())
   {
    passField.requestFocus();
    passField.setError("PASSWORD SHOULD CONTAIN SPECIAL CHARACTER");
    return  false;

   }
  }
  return true;
 }
 //email validation
 public boolean checkEmail(String email, EditText emailField)
 {
     if (email.equals("")) {
      emailField.requestFocus();
      emailField.setError("ENTER EMAIL");
      return false;
     } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      emailField.requestFocus();
      emailField.setError("ENTER VALID EMAIL");
      return false;
     }
  return true;
 }
 //name  validation
 public boolean checkName(String name,EditText nameField)
 {
  if (name.equals("")) {
   nameField.requestFocus();
   nameField.setError("ENTER NAME");
   return false;
  }
  return true;
 }

//repassword validation
public boolean checkRepass(String repass,String pass,EditText RePasswordField) {
 if (!pass.equals(repass)) {
  RePasswordField.requestFocus();
  RePasswordField.setError("PASSWORD DOES NOT MATCH");
  return false;
 }
 return true;
}

}
