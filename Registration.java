package com.example.mynutrition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {

    EditText emailet;
    EditText passwordet;
    EditText nameet;
    Button signup;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;


    String name = "";
    String email = "";
    String pass = "";
    String uid;
    UserHelperClass addNewUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        emailet = findViewById(R.id.etEmail);
        passwordet = findViewById(R.id.etPassword);
        signup = findViewById(R.id.btnSignup);
        nameet = findViewById(R.id.etName);

        firebaseAuth = FirebaseAuth.getInstance();


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = emailet.getText().toString();
                name = nameet.getText().toString();
                pass = passwordet.getText().toString();

                addNewUser = new UserHelperClass(name, email);

                firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            firebaseUser = firebaseAuth.getCurrentUser();
                            reference = FirebaseDatabase.getInstance().getReference(firebaseUser.getUid());

                            Toast.makeText(Registration.this, "Registration Successful", Toast.LENGTH_LONG).show();
                            reference.setValue(addNewUser);
                            Intent intent = new Intent(Registration.this, Dashboard.class);
                            startActivity(intent);

                        }
                        else
                            Toast.makeText(Registration.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

                }
        });

    }
}
