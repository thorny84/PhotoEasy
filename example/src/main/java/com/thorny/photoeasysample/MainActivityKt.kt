package com.thorny.photoeasysample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.thorny.photoeasy.ExternalStoragePermission
import com.thorny.photoeasy.PhotoEasy

class MainActivityKt : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var photoEasy: PhotoEasy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
    }

    fun cameraAction(view: View) {
        photoEasy = PhotoEasy.builder()
            .setActivity(this)
            .setMimeType(PhotoEasy.MimeType.imagePng)
            .setStorageType(PhotoEasy.StorageType.media)
            .build()
        photoEasy.startActivityForResult(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        photoEasy.onActivityResult(
            requestCode, resultCode
        ) { thumbnail ->
            imageView.setImageBitmap(thumbnail)
        }
    }

    private class ExtStoPer(activity: Activity) :
        ExternalStoragePermission(activity, RequestMode.allControl) {
        override fun requestPermissionRationale() {
        }

        override fun requestPermission() {
        }

    }
}