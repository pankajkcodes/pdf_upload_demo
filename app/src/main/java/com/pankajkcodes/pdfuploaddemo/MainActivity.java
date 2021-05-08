package com.pankajkcodes.pdfuploaddemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.OnProgressListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    // INITIALIZE ALL VIEWS
    ImageView selectPdf;
    Button uploadBtn, pdfListsBtn;
    EditText editText;
    StorageReference storageReference;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  GET ALL VIEW BY ID
        selectPdf = findViewById(R.id.uploadpdf);
        editText = findViewById(R.id.editText);
        uploadBtn = findViewById(R.id.uploadBtn);
        pdfListsBtn = findViewById(R.id.pdflist);


        uploadBtn.setEnabled(false);

        // AFTER CLICKING ON selectPdF BUTTON WE WILL REDIRECTED TO CHOOSE PDF
        selectPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("application/pdf");
                startActivityForResult(galleryIntent, 1);
                Toast.makeText(MainActivity.this, "Select Pdf ", Toast.LENGTH_SHORT).show();
            }
        });
        // AFTER CLICKING ON pdfListsBtn BUTTON WE WILL REDIRECTED SHOW PDF FILES ACTIVITY
        pdfListsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewPdfActivity.class);
                startActivity(intent);
            }
        });
    }
    // CREATE A PROGRESS DIALOG TO SHOW BEFORE UPLOADING
    ProgressDialog dialog;


    // OVERRIDE A METHOD onActivityResult METHOD WHICH REDIRECT PDF FILE SELECTED SUCCESSFULLY
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            dialog = new ProgressDialog(this);
            if (editText.getText().toString().isEmpty()) {
                editText.setError("Required");
            } else {
                uploadBtn.setEnabled(true);
            }
            // AFTER CLICKING ON upload BUTTON WE WILL REDIRECTED TO UPLOAD PDF FILES METHOD
            uploadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Here we are initialising the progress dialog box
                    dialog.setMessage("Uploading...");
                    dialog.show();
                    uploadPdfFiles(data.getData());
                }
            });
        }
    }

    // THIS IS METHOD FOR UPLOADS PDF FILES
    private void uploadPdfFiles(Uri data) {
        // GET REFERENCES OF DATABASE AND STORAGE
        storageReference = FirebaseStorage.getInstance().getReference("uploads/");
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");
        // CODE FOR UPLOAD PDF
        StorageReference reference = storageReference.child("pdf_" + System.currentTimeMillis() + ".pdf");
        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri uri = uriTask.getResult();
                UploadPdfModel model = new UploadPdfModel(editText.getText().toString(), uri.toString());
                databaseReference.child(databaseReference.push().getKey()).setValue(model);
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}
