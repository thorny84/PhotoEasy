package com.thorny.photoeasy;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class BitmapCreator {

  public Bitmap getBitmapFromContentResolver(
      final ContentResolver contentResolver,
      final Uri lastUri){
    Bitmap bitmap = null;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      final ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, lastUri);
      try {
        bitmap = ImageDecoder.decodeBitmap(source);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      InputStream inputStream = null;
      try {
        inputStream = contentResolver.openInputStream(lastUri);
        bitmap = BitmapFactory.decodeStream(inputStream);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return bitmap;
  }
}
