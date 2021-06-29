package com.project.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.*;

public class CreateAccountActivity extends AppCompatActivity {
    private final String TAG = "CreateAccountActivity";
    private Button createnewAccount;
    private EditText newUsername,newEmail,newPassword;
    private ProgressBar progressBar;

    //Firestore authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore instance
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("User");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        firebaseAuth = FirebaseAuth.getInstance();
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        createnewAccount = findViewById(R.id.newcreateaccount);
        newUsername = findViewById(R.id.Addusername);
        newEmail = findViewById(R.id.newEmail);
        newPassword = findViewById(R.id.newPassword);
        progressBar = findViewById(R.id.progressbar);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser!=null){
                    //User is logged in...
                }else {
                    // There is no user yet...
                }
            }
        };

        createnewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = newEmail.getText().toString().trim();
                String password = newPassword.getText().toString().trim();
                String username = newUsername.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {
                    CreatenewAccount(email, password, username);
                }else{
                    Toast.makeText(getApplicationContext(),"Empty fields are not allowed!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void CreatenewAccount (String email, String password, final String username){

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                //we take to addJournalActivity
                                Log.d(TAG, "onComplete: " + task.toString());
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String currentUserId = currentUser.getUid();
                                Map<String,String> user = new HashMap<>();
                                user.put("UserId",currentUserId);
                                user.put("Username",username);
                                collectionReference.add(user)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(final DocumentReference documentReference) {
                                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (Objects.requireNonNull(task.getResult()).exists()){
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            String Username = task.getResult().getString("Username");
                                                            JournalApi journalApi = JournalApi.getInstance();
                                                            journalApi.setUserId(currentUserId);
                                                            journalApi.setUsername(Username);
                                                            Intent intent = new Intent(CreateAccountActivity.this,PostJournalActivity.class);
                                                            startActivity(intent);
                                                        }
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "document onFailure: " + e.toString());
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Collection onFailure: " + e.toString());
                }
            });
        }else{
            Log.d(TAG, "else onFailure: " + "Not working");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}
