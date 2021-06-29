package com.project.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import util.JournalApi;
import model.Journal;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int GALLERY_CODE = 1;
    private ImageView camera,background;
    private TextView name,date;
    private EditText title,thought;
    private ProgressBar postprogressbar;
    private Button savebutton;
    private Uri imageURI;

    private String currentUserID;
    private String currentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Firestore instances
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        postprogressbar = findViewById(R.id.post_progressBar);
        title = findViewById(R.id.post_title);
        thought = findViewById(R.id.post_journal_text);
        name = findViewById(R.id.post_name_view);
        date = findViewById(R.id.post_date_view);
        background = findViewById(R.id.background_view);
        camera = findViewById(R.id.camera_view);
        savebutton = findViewById(R.id.save_button);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        savebutton.setOnClickListener(this);
        camera.setOnClickListener(this);
        postprogressbar.setVisibility(View.INVISIBLE);
        if (JournalApi.getInstance()!=null){
            currentUserID = JournalApi.getInstance().getUserId();
            currentUsername = JournalApi.getInstance().getUsername();
            name.setText(currentUsername);
        }
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth!=null){
                  user = firebaseAuth.getCurrentUser();
                } else{

                }
            }
        };
    }
    public void setSavebutton(){
        final String titleString = title.getText().toString().trim();
        final String thoughtString = thought.getText().toString().trim();
        postprogressbar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(titleString) && !TextUtils.isEmpty(thoughtString) && imageURI!=null) {
            final StorageReference filepath = storageReference.child("journal_images")
                    .child("all_image" + Timestamp.now().getSeconds());
            filepath.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Creating Journal information Object
                            String imageUrl = uri.toString();  //image URL
                            Journal journal = new Journal();
                            journal.setTitle(titleString);
                            journal.setThought(thoughtString);
                            journal.setImageURL(imageUrl);
                            journal.setUserId(currentUserID);
                            journal.setUsername(currentUsername);
                            journal.setTimeAdded(new Timestamp(new Date()));

                            //Saving to collectionReference
                            collectionReference.add(journal)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            postprogressbar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(PostJournalActivity.this,JournalListActivity.class));
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    postprogressbar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    postprogressbar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }else{
            postprogressbar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLERY_CODE && resultCode==RESULT_OK){
             imageURI = data.getData();  //The path to the image saved in imageURI
             background.setImageURI(imageURI); //setting background
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save_button:
                setSavebutton();
                break;
            case R.id.camera_view:
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
                break;
        }
    }
}
