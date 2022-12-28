package com.example.android.parentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.R;
import com.example.android.parentHome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profileFragment extends Fragment  implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    View currentroot;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SharedPreferences preferences;
    static  final String key_uid="parent_uid";
    static  final String shared_pref_name="data";
   private Context cont;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseAuth mAuth;
    private  SharedPreferences   preference;
    private TextView  profilename,profileemail,childname,childemail;
    private  FirebaseDatabase database;
    private  String parent_uid;
    private DatabaseReference myRef;
    public profileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment profileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static profileFragment newInstance(String param1, String param2) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =inflater.inflate(R.layout.fragment_profile, container, false);
        currentroot=root;
        parentHome.AppContext=root.getContext();

        preferences=getContext().getSharedPreferences(shared_pref_name,getContext().MODE_PRIVATE);
        //get data from sharedprefernce
        String pref_name=preferences.getString("name",null);
        String pref_email=preferences.getString("email",null);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        parent_uid=preferences.getString(key_uid,null);
        //create button objects
        Button  removechild=root.findViewById(R.id.profileremoveChild);
        Button  logoutButton=root.findViewById(R.id.profileparentlogout);
        Button  deleteaccountButton=root.findViewById(R.id.profiledeleteMyAccount);

        //add listner to button
        deleteaccountButton.setOnClickListener(this::onClick);
        logoutButton.setOnClickListener(this::onClick);
        removechild.setOnClickListener(this::onClick);

        //create objects for textviews
        profilename=root.findViewById(R.id.profileName);
        profileemail=root.findViewById(R.id.ProfileEmail);
        childname=root.findViewById(R.id.ProfileChild_name);
        childemail=root.findViewById(R.id.ProfileChildEmail);

        childname.setMovementMethod(new ScrollingMovementMethod());
        childemail.setMovementMethod(new ScrollingMovementMethod());
        profileemail.setMovementMethod(new ScrollingMovementMethod());
        profilename.setMovementMethod(new ScrollingMovementMethod());
        //get child name and email from database
        myRef = database.getReference("users");
        myRef.child(parent_uid).child("child uid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().toString() != "null") {
                        myRef.child(snapshot.getValue().toString()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                try {

                                    childemail.setText(snapshot2.getValue().toString());
                                } catch (Exception e) {
                                    childemail.setText(".........................................");

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        myRef.child(snapshot.getValue().toString()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                try {

                                    childname.setText(snapshot2.getValue().toString());
                                } catch (Exception e) {
                                    childname.setText(".........................................");

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else {
                        // if no account connected and child uid is null then set link account
                        childemail.setText("link account....");
                        childname.setText("link account....");

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        profileemail.setText(pref_email);
        profilename.setText(pref_name);
        cont=getContext();
        return root;
    }

    @Override
    public void onClick(View view) {
        preferences=this.getActivity().getSharedPreferences(shared_pref_name,currentroot.getContext().MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        SharedPreferences.Editor editor=preferences.edit();

        DatabaseReference myRef = database.getReference("users");
        switch(view.getId())
        {
            //only log out from device
            case R.id.profileparentlogout:
                // on click log out log out from device and clear shared preference
              editor.clear();
                editor.commit();

                parentHome.AppContext=null;
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();

                break;
            case R.id.profileremoveChild:

                 SharedPreferences.Editor editors=preferences.edit();
                //on remove child click set childs parent uid to null
                myRef.child(parent_uid).child("child uid").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            if (!snapshot.getValue().toString().equals("null")) {

                                myRef.child(snapshot.getValue().toString()).child("parent uid").setValue("null");
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                //set linked and child uid to null
                    myRef.child(parent_uid).child("linked").setValue("false");
                    myRef.child(parent_uid).child("child uid").setValue("null").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //on sucessfully setting child uid to null delete child name and email in prefence
                            editors.remove("childemail").commit();
                            editors.remove("childname").commit();
                            Toast.makeText(cont, "child account has been removed", Toast.LENGTH_SHORT).show();
                            // set name and email to link account.....
                            childemail.setText("link account.....");
                            childname.setText("link account......");
                        }
                        else {
                            Toast.makeText(cont, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                    });
                    break;


            case R.id.profiledeleteMyAccount:
                parent_uid=preferences.getString(key_uid,null);
                SharedPreferences.Editor editordelete=preferences.edit();

                //get password from  user using alert box
                AlertDialog.Builder alert=new AlertDialog.Builder(getContext(),AlertDialog.THEME_HOLO_DARK);

                //create edit text
                final EditText DeletePasswordField=new EditText(getContext());
                //set hint text color background color and hint color
                DeletePasswordField.setHint("enter your password");
                DeletePasswordField.setTextColor(getResources().getColor(R.color.white));
                DeletePasswordField.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.darkblue));
                DeletePasswordField.setHintTextColor(getResources().getColor(R.color.white));
                //create alert box and add edit text field to alert box
                AlertDialog alertDialog = alert.create();


                alert.setView(DeletePasswordField);

                //set positive button continue
                alert.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pass= DeletePasswordField.getText().toString();
                        String email=preferences.getString("email",null);
                        //check if password is greater then 5
                        if (pass.length()>=6)
                        {
                            //create instanece of  authenticator
                        mAuth = FirebaseAuth.getInstance();
                        //sign in
                        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                               if (task.isSuccessful()) {

                                     //delete
                                   mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()) {
                                               // if signed in sucessfully delete account
                                               //get child uid and set parent uid to null
                                               myRef.child(parent_uid).child("child uid").addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                       if (snapshot != null) {
                                                           if (!snapshot.getValue().toString().equals("null")) {

                                                               myRef.child(snapshot.getValue().toString()).child("parent uid").setValue("null");
                                                           }
                                                       }
                                                   }
                                                   @Override
                                                   public void onCancelled(@NonNull DatabaseError error) {
                                                   }
                                               });
                                               //delete all value from relatime database
                                               myRef.child(parent_uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {

                                                       editor.clear();
                                                       editor.commit();
                                                         }
                                               });
                                           }
                                       }
                                   });
                               }
                               else {
                                   // if sign in failed check reasoon and make toast
                                   ConnectivityManager cm=(ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                                   if (cm.getActiveNetworkInfo() == null) {
                                       Toast.makeText(getContext()," check your internet connection and try again",Toast.LENGTH_SHORT).show();
                                   } else {
                                       Toast.makeText(getContext()," unsucessfull",Toast.LENGTH_SHORT).show();
                                   }
                               }
                           }
                       });
                    }
                        else{
                            //if ass len is len then 6 make toast enter valid paswword
                            Toast.makeText(getContext(),"enter valid password",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //set negative button and on cancle make toast cancled
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(),"cancled",Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();

                    }
                });
                //set focus on editText and show alert message
                DeletePasswordField.setFocusable(true);
               alert.show();

                break;
        }

    }

}