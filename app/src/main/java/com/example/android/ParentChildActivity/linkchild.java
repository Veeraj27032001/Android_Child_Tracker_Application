package com.example.android.ParentChildActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.R;
import com.example.android.parentActivity.googleMapsFragment;
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
 * Use the {@link linkchild#newInstance} factory method to
 * create an instance of this fragment.
 */
public class linkchild extends Fragment  implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View parentroot;
    private FirebaseAuth mAuth;
    static String account_type = "none";
    static String name = "";
    static final String shared_pref_name = "data";
    static final String key_uid = "parent_uid";
    private  Button LinkchildButton;
    private TextView registerchild;
    public linkchild() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment linkchild.
     */
    // TODO: Rename and change types and number of parameters
    public static linkchild newInstance(String param1, String param2) {
        linkchild fragment = new linkchild();
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
        View root = inflater.inflate(R.layout.fragment_linkchild, container, false);
        parentHome.AppContext=root.getContext();
        // create obj for button
        registerchild = (TextView) root.findViewById(R.id.linkchildregisterButton);
        LinkchildButton = (Button) root.findViewById(R.id.linkchildconnectButton);
        //add listners
        registerchild.setOnClickListener(this::onClick);
        LinkchildButton.setOnClickListener(this::onClick);
        parentroot = root;
        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.linkchildregisterButton:
                //on register transfer control to registration page
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transactionManager = manager.beginTransaction();
                transactionManager.replace(R.id.parentFragmentContainer, new ChildRegistration());
                transactionManager.commit();
                break;
            case R.id.linkchildconnectButton:
                //on connect press
                //creating shared preferences
                SharedPreferences   preferences=getContext().getSharedPreferences(shared_pref_name,getContext().MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();

                // create  edit text obj
                EditText emailField = (EditText) parentroot.findViewById(R.id.linkchildEmail);
                EditText PasswordField = (EditText) parentroot.findViewById(R.id.linkchildPassword);
                //get data from field
                String email = emailField.getText().toString();
                String password = PasswordField.getText().toString();
                //verify password and email
                if (email.equals("")) {
                    emailField.requestFocus();
                    emailField.setError("ENTER EMAIL");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailField.requestFocus();
                    emailField.setError("ENTER VALID EMAIL");

                } else if (password.equals("") || password.length() < 6) {
                    PasswordField.requestFocus();
                    PasswordField.setError("ENTER VALID PASSWORD");
                } else {
                    // if password and email valid
                    ProgressBar bar = (ProgressBar) parentroot.findViewById(R.id.loginprogress);
                    bar.setVisibility(ProgressBar.VISIBLE);
                    registerchild.setEnabled(false);
                    LinkchildButton.setEnabled(false);
                    //create authenticator instance
                    mAuth = FirebaseAuth.getInstance();
                    //sign in with email
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();
                                //create fire database object
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users");
                                //get account type information from real time data base
                                myRef.child(uid).child("account type").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //if type is child tthen link account
                                        if( snapshot.getValue().toString().equals("child")) {
                                            myRef.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                   //get name of child and store it in shared prefernce
                                                    editor.putString("childname", snapshot2.getValue().toString());
                                                   editor.commit();

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                            //get my current parent login uid
                                            String my_uid=preferences.getString(key_uid,null);
                                            //set  adress of parent in child account and set parent linked true
                                            myRef.child(uid).child("parent uid").setValue(my_uid);
                                            myRef.child(my_uid).child("linked").setValue("true");
                                            //store email in shared prefernce
                                            editor.putString("childemail",email);
                                            //set child uid in parent database
                                            myRef.child(my_uid).child("child uid").setValue(uid);

                                            Toast.makeText(getActivity(), " linked successfully " , Toast.LENGTH_SHORT).show();
                                           //sign out hide progress bar  and transfer control to  home
                                            mAuth.signOut();
                                            bar.setVisibility(ProgressBar.GONE);
                                            registerchild.setEnabled(true);
                                            LinkchildButton.setEnabled(true);
                                            FragmentManager manager=getParentFragmentManager();
                                            FragmentTransaction transactionManager= manager.beginTransaction();
                                            transactionManager.replace(R.id.parentFragmentContainer,new googleMapsFragment());
                                            transactionManager.commit();
                                        }
                                        //if account type is parent then make toast saying cannot linkaccount
                                        else if(snapshot.getValue().toString().equals("parent"))
                                        {
                                            Toast.makeText(getActivity(), " cannot link parent account to a parent account " + account_type, Toast.LENGTH_SHORT).show();
                                            bar.setVisibility(ProgressBar.GONE);
                                            registerchild.setEnabled(true);
                                            LinkchildButton.setEnabled(true);
                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                                else {
                                    //if child sign in failed then check reason and make toast
                                ConnectivityManager cm=(ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                                if (cm.getActiveNetworkInfo() == null) {
                                    Toast.makeText(getContext(), "check your internet connection and try again ", Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(view.GONE);
                                    registerchild.setEnabled(true);
                                    LinkchildButton.setEnabled(true);
                                } else {

                                    Toast.makeText(getActivity(), " unsuccessful" + account_type, Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(ProgressBar.GONE);
                                    registerchild.setEnabled(true);
                                    LinkchildButton.setEnabled(true);
                                    PasswordField.getText().clear();
                                    emailField.getText().clear();
                                }

                            }

                        }
                    });
                    break;

                }
        }
    }
}
