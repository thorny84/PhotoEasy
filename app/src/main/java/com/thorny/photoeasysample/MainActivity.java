package com.thorny.photoeasysample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.thorny.photoeasy.PhotoEasy;

public class MainActivity extends AppCompatActivity {

    final int REQUEST_CAMERA_KEY = 342;
    ImageView imageView;
    PhotoEasy p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);


    }

    public void cameraAction(View view) {
        p = new PhotoEasy(getApplicationContext());
        p.takePhoto();
        startActivityForResult(p.getIntentPhoto(), REQUEST_CAMERA_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK) {
            imageView.setImageBitmap(p.displayPhoto(imageView, p.getPhotoPath()));
        }
    }
}

