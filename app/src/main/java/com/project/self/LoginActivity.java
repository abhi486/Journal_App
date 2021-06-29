package com.project.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    private Button loginButton,createaccntbutton;
    private AutoCompleteTextView email;
    private EditText password;
    private ProgressBar loginprogressbar;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("User");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth = FirebaseAuth.getInstance();
        loginprogressbar  = findViewById(R.id.loginprogressbar);
        email = findViewById(R.id.emailtext);
        password = findViewById(R.id.passwordtext);
        loginButton = findViewById(R.id.loginbutton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithEmailAndPassword(email.getText().toString().trim(),
                        password.getText().toString().trim());
            }
        });
        createaccntbutton = findViewById(R.id.createaccountbutton);
        createaccntbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });
    }

    private void loginWithEmailAndPassword(String emailAddress, String pwd) {
        loginprogressbar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(emailAddress) && !TextUtils.isEmpty(pwd)){
            firebaseAuth.signInWithEmailAndPassword(emailAddress,pwd)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        String currentUserId = user.getUid();
                        collectionReference.whereEqualTo("UserId",currentUserId)
                           .addSnapshotListener(new EventListener<QuerySnapshot>() {
                               @Override
                               public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                   if (e!=null){
                                   }
                                   assert queryDocumentSnapshots != null;
                                   if (!queryDocumentSnapshots.isEmpty()){
                                       for (QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                                           JournalApi journalApi = JournalApi.getInstance();
                                           journalApi.setUsername(snapshot.getString("Username"));
                                           journalApi.setUserId(snapshot.getString("UserId"));
                                       }
                                       loginprogressbar.setVisibility(View.INVISIBLE);
                                       startActivity(new Intent(LoginActivity.this, Journal.class));
                                       finish();
                                   }
                               }
                           });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loginprogressbar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onFailure: " + e.toString());
                }
            });
        }else{
            loginprogressbar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this,"Invalid email or password",Toast.LENGTH_SHORT).show();
        }
    }
}
