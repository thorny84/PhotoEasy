package com.thorny.photoeasy;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

public final class StorageUriHandler {

  private final Activity activity;
  private final String filename;
  private final String mimeType;
  private final String typeSuffix;

  public StorageUriHandler(Activity activity, String fileName, PhotoEasy.MimeType mimeType) {
    this.activity = activity;
    this.filename = fileName != null ? fileName : String.valueOf(System.nanoTime());
    switch (mimeType) {
      case imagePng:
        this.mimeType = "image/png";
        typeSuffix = ".png";
        break;
      case imageWebp:
        this.mimeType = "image/webp";
        typeSuffix = ".webp";
        break;
      case imageJpeg:
      default:
        this.mimeType = "image/jpeg";
        typeSuffix = ".jpeg";
    }
  }

  public Uri getStorageUri(final File fileDirectory) {
    try {
      return FileProvider.getUriForFile(activity,
          activity.getPackageName() + ".fileProvider",
          createTempImageFile(fileDirectory));
    } catch (IOException | IllegalArgumentException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Uri getStorageMediaUri(@Nullable final String customDirectory) {
    final ContentValues contentValues = new ContentValues();
    final String name = filename;
    contentValues.put(MediaStore.Images.Media.TITLE, name);
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
    contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
    if (customDirectory != null)
      if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + customDirectory);
      } else {
        File dir = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + customDirectory);
        if (!dir.exists() && !dir.mkdirs()) return null;
        String filename = name + typeSuffix;
        File file = new File(dir, filename);
        contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
      }
    return activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
  }

  private File createTempImageFile(File storageDir) throws IOException {
    return File.createTempFile(
        filename,
        typeSuffix,
        storageDir
    );
  }
}
