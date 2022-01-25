package com.example.mynutrition;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Additem extends AppCompatActivity {

    Button selectBtn;
    ImageView imageView;
    RadioGroup radioGroup;
    Button addBtn;
    EditText etCalorie;
    EditText etFoodName;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    ProgressBar progressBar;
    TextView dateTv;
    ImageView toWater;

    boolean isFruit = false;
    boolean isVegetable = false;
    boolean isGrain = false;
    boolean isDairy = false;
    boolean isProtein = false;

    String calories = "0";
    String foodName = "";
    String date = "";
    String category = "";
    int calorieCount = 0;

    ImageView back;

    boolean isAdding = true;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;

    boolean isClicked = false;

    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_additem);
        selectBtn = findViewById(R.id.selectBtn);
        addBtn = findViewById(R.id.addBtn);
        imageView = findViewById(R.id.imageView);
        etFoodName = findViewById(R.id.etFoodName);
        back = findViewById(R.id.back_icon);
        radioGroup = findViewById(R.id.radioGroup);
        etCalorie = findViewById(R.id.etCalorie);
        progressBar = findViewById(R.id.progressBar);
        dateTv = findViewById(R.id.dateTv);
        toWater = findViewById(R.id.add_water_icon);

        toWater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Additem.this,Addwater.class));
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(firebaseUser.getUid());
        mStorageReference = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(firebaseUser.getUid()).child("uploads");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Additem.super.onBackPressed();
            }
        });

        dateTv.setPaintFlags(dateTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        dateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(Additem.this,android.R.style.Theme_Holo_Light_Dialog_MinWidth,mDateSetListener,year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                date = month+"/"+dayOfMonth+"/"+year;
                dateTv.setText(date);
            }
        };

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isAdding) {

                    if(imageUri!=null) {

                        addBtn.setText("Add");

                        uploadFile();

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (isClicked) {
                                    reference.child("foodnames").setValue(etFoodName.getText().toString());
                                    reference.child("dates").setValue(date);
                                    reference.child("categories").setValue(category);
                                    isClicked = false;
                                }


                                if (isFruit) {
                                    if (dataSnapshot.child("fruit").getValue(String.class) != null)
                                        calorieCount = Integer.parseInt(dataSnapshot.child("fruit").getValue(String.class));
                                }
                                if (isVegetable) {
                                    if (dataSnapshot.child("vegetable").getValue(String.class) != null)
                                        calorieCount = Integer.parseInt(dataSnapshot.child("vegetable").getValue(String.class));
                                }
                                if (isGrain) {
                                    if (dataSnapshot.child("grains").getValue(String.class) != null)
                                        calorieCount = Integer.parseInt(dataSnapshot.child("grains").getValue(String.class));
                                }
                                if (isDairy) {
                                    if (dataSnapshot.child("dairy").getValue(String.class) != null)
                                        calorieCount = Integer.parseInt(dataSnapshot.child("dairy").getValue(String.class));
                                }
                                if (isProtein) {
                                    if (dataSnapshot.child("protein").getValue(String.class) != null)
                                        calorieCount = Integer.parseInt(dataSnapshot.child("protein").getValue(String.class));
                                }

                                calories = etCalorie.getText().toString();
                                calorieCount += Integer.parseInt(calories);
                                calories = Integer.toString(calorieCount);

                                foodName = etFoodName.getText().toString();

                                if (isFruit) {
                                    reference.child("fruit").setValue(calories);
                                    isFruit = false;
                                }
                                if (isDairy) {
                                    reference.child("dairy").setValue(calories);
                                    isDairy = false;
                                }
                                if (isVegetable) {
                                    reference.child("vegetable").setValue(calories);
                                    isVegetable = false;
                                }
                                if (isGrain) {
                                    reference.child("grains").setValue(calories);
                                    isGrain = false;
                                }
                                if (isProtein) {
                                    reference.child("protein").setValue(calories);
                                    isProtein = false;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        isAdding = false;
                    }
                    else{
                        Toast.makeText(Additem.this,"Please select an image",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    isAdding = true;
                    startActivity(new Intent(Additem.this, imagesActivity.class));
                }
            }
        });



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isClicked = true;
                switch(checkedId){
                    case R.id.fruitButton:
                        category = "fruit";
                        isFruit = true;
                        isVegetable = false;
                        isGrain = false;
                        isDairy = false;
                        isProtein = false;
                        break;
                    case R.id.vegetableButton:
                        category = "vegetable";
                        isFruit = false;
                        isVegetable = true;
                        isGrain = false;
                        isDairy = false;
                        isProtein = false;
                        break;
                    case R.id.grainsButton:
                        category = "grains";
                        isFruit = false;
                        isVegetable = false;
                        isGrain = true;
                        isDairy = false;
                        isProtein = false;
                        break;
                    case R.id.dairyButton:
                        category = "dairy";
                        isFruit = false;
                        isVegetable = false;
                        isGrain = false;
                        isDairy = true;
                        isProtein = false;
                        break;
                    case R.id.proteinButton:
                        category = "protein";
                        isFruit = false;
                        isVegetable = false;
                        isGrain = false;
                        isDairy = false;
                        isProtein = true;
                        break;
                }
            }
        });


    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data!=null && data.getData()!=null){
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile(){
        if(imageUri != null){
            final StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    addBtn.setText("View log");
                    Toast.makeText(Additem.this, etFoodName.getText().toString() + " added successfully!", Toast.LENGTH_LONG).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                        }
                    },500);

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Upload upload = new Upload(uri.toString(),etFoodName.getText().toString(),date);
                            String uploadId = mDatabaseReference.push().getKey();
                            mDatabaseReference.child(uploadId).setValue(upload);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Additem.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress);
                }
            });
        }
        else{
            Toast.makeText(Additem.this,"No file selected",Toast.LENGTH_SHORT).show();
        }
    }
}
