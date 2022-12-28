package com.example.android.ParentChildActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.R;
import com.example.android.functions.RigistrationValidation;
import com.example.android.parentHome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChildRegistration#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChildRegistration extends Fragment implements View.OnClickListener {
    private FirebaseAuth mAuth;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View curentroot;
    private EditText emailField ;
    private EditText nameField ;
    private EditText PasswordField;
    private EditText RePasswordField;
    private ProgressBar bar;
   private TextView alreadyhaveaccount;
    private String child_uid="";
    private  String parent_uid="";
    private String key_childuid="child_uid";
    private SharedPreferences preferences;
    private static  final String key_uid="parent_uid";
    private static  final String shared_pref_name="data";
    private  Button childSignUp;
    public ChildRegistration() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChildRegistration.
     */
    // TODO: Rename and change types and number of parameters
    public static ChildRegistration newInstance(String param1, String param2) {
        ChildRegistration fragment = new ChildRegistration();
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
         View root=inflater.inflate(R.layout.fragment_child_registration, container, false);
        parentHome.AppContext=root.getContext();
         //create object for all view
         childSignUp=(Button)root.findViewById(R.id.ChildsignupButton);
        childSignUp.setOnClickListener(this::onClick);
         emailField = root.findViewById(R.id.ChildsignupEmail);
         nameField = root.findViewById(R.id.ChildsignupName);
        PasswordField = root.findViewById(R.id.ChildsignupPassword);
         RePasswordField =root.findViewById(R.id.ChildsignupRePassword);
        bar = root.findViewById(R.id.Childsignupprogress);

        alreadyhaveaccount=root.findViewById(R.id.ChildalredyhaveAccount);
        //add on click listner
        alreadyhaveaccount.setOnClickListener(this::onClick);
        curentroot=root;

        return root;
    }

    @Override
    public void onClick(View view) {
        preferences=this.getActivity().getSharedPreferences(shared_pref_name,curentroot.getContext().MODE_PRIVATE);

        SharedPreferences.Editor editor=preferences.edit();
        switch (view.getId())
        {
            case R.id.ChildalredyhaveAccount:
                //on already have account transfer control to link account page
                FragmentManager manager=getParentFragmentManager();
                FragmentTransaction transactionManager= manager.beginTransaction();
                transactionManager.replace(R.id.parentFragmentContainer,new linkchild());
                transactionManager.commit();
                break;
            case R.id.ChildsignupButton:
                //on register click
                //get all data from fields
                String Email = emailField.getText().toString();
                String name = nameField.getText().toString();
                String password = PasswordField.getText().toString();
                String repassword = RePasswordField.getText().toString();
                //verify all requiremnt
                RigistrationValidation validation=new RigistrationValidation();
                if(!validation.checkEmail(Email,emailField)||!validation.checkName(name,nameField)||!validation.checkPassword(password,PasswordField)||!validation.checkRepass(repassword,password,RePasswordField))
                {

                } else {
                    //when all requirement met

                    bar.setVisibility(view.VISIBLE);
                    alreadyhaveaccount.setEnabled(false);
                    childSignUp.setEnabled(false);
                    //create authenticator object
                    mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(Email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //on sucessfull registration add data to database
                               FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("email").setValue(Email);
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("account type").setValue("child");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(name);
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("parent uid").setValue("null");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("loged in").setValue("false");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("latitude").setValue("null");
                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("longitude").setValue("null")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    Toast.makeText(curentroot.getContext(), "registration successful ", Toast.LENGTH_LONG).show();
                                                    bar.setVisibility(view.GONE);
                                                    alreadyhaveaccount.setEnabled(true);
                                                    childSignUp.setEnabled(true);
                                                    //go back to home
                                                    FragmentManager manager=getParentFragmentManager();
                                                    FragmentTransaction transactionManager= manager.beginTransaction();
                                                    transactionManager.replace(R.id.parentFragmentContainer,new linkchild());
                                                    transactionManager.commit();

                                                } else {
                                                    Toast.makeText(curentroot.getContext(), "user registration failed ", Toast.LENGTH_SHORT).show();
                                                    bar.setVisibility(view.GONE);
                                                    alreadyhaveaccount.setEnabled(true);
                                                    childSignUp.setEnabled(true);
                                                }
                                            }
                                        });

                            }
                            else {
                                //if registration failed toast reason by checking internet connection
                                ConnectivityManager cm=(ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                                if (cm.getActiveNetworkInfo() == null) {
                                    Toast.makeText(curentroot.getContext(), "check your internet connection and try again ", Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(view.GONE);
                                    alreadyhaveaccount.setEnabled(true);
                                    childSignUp.setEnabled(true);
                                } else {
                                    Toast.makeText(curentroot.getContext(), "existing user email ", Toast.LENGTH_SHORT).show();
                                    bar.setVisibility(view.GONE);
                                    alreadyhaveaccount.setEnabled(true);
                                    childSignUp.setEnabled(true);
                                }
                            }
                        }
                    });

                }

                break;
        }
    }
}