package com.thorny.photoeasy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

import androidx.annotation.NonNull;

import static android.app.Activity.RESULT_OK;

public final class PhotoEasy {

  private final int REQUEST_CAMERA_KEY = 1566;
  private final ContentResolver contentResolver;
  private final Boolean enableRequestPermission;
  private File fileDirectory;
  private ExternalStoragePermission defaultExternalStoragePermission;
  MimeType mimeType;
  private String fileName;
  private StorageType storageType;
  private Uri lastUri;
  private String customDirectory;

  private PhotoEasy(
      final Activity activity,
      final StorageType storageType,
      final ExternalStoragePermission externalStoragePermission,
      final Boolean enablePermission) {
    setStorageType(activity, storageType);
    setExternalPermission(activity, externalStoragePermission);
    contentResolver = activity.getContentResolver();
    enableRequestPermission = enablePermission;
  }

  private void setStorageType(Activity activity, StorageType type) {
    storageType = type;
    switch (type) {
      case internal:
        fileDirectory = activity.getFilesDir();
        break;
      case external:
        fileDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }
  }

  private void setExternalPermission(final Activity activity, final ExternalStoragePermission externalStoragePermission) {
    if (externalStoragePermission == null) {
      defaultExternalStoragePermission = new DefaultStoragePermission(activity);
      return;
    }
    defaultExternalStoragePermission = externalStoragePermission;
  }

  private void setMimeType(MimeType mimeType) {
    this.mimeType = mimeType;
  }

  private void setName(String fileName) {
    this.fileName = fileName;
  }

  private void setCustomDirectory(String directoryName){
    this.customDirectory = directoryName;
  }

  public void onActivityResult(
      int requestCode,
      int resultCode,
      @NonNull OnPictureReady onPictureReady) {

    if (requestCode != REQUEST_CAMERA_KEY || resultCode != RESULT_OK)
      return;

    final BitmapCreator bitmapCreator = new BitmapCreator();
    final Bitmap bitmap = bitmapCreator.getBitmapfromContentResolver(contentResolver, lastUri);

    onPictureReady.onFinish(bitmap);
  }

  public void startActivityForResult(Activity activity) {

    if (enableRequestPermission)
      if (storageType == StorageType.external || storageType == StorageType.media) {
        if (!isExternalStorageWritable())
          return;
        if (!defaultExternalStoragePermission.permissionCheck()) {
          defaultExternalStoragePermission.init();
          return;
        }
      }

    final StorageUriHandler storageUriHandler = new StorageUriHandler(activity, fileName, mimeType);
    Uri photoUri;
    switch (storageType) {
      case internal:
      case external:
        photoUri = storageUriHandler.getStorageUri(fileDirectory);
        break;
      default:
        photoUri = storageUriHandler.getStorageMediaUri(customDirectory);
    }

    if (photoUri == null)
      return;

    lastUri = photoUri;
    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
    activity.startActivityForResult(intent, REQUEST_CAMERA_KEY);
  }

  private boolean isExternalStorageWritable() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }

  // --Builder--
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Activity activity;
    private MimeType mimeType = MimeType.imageJpeg;
    private String fileName;
    private StorageType storageType = StorageType.external;
    private ExternalStoragePermission externalStoragePermission;
    private Boolean enablePermission = true;
    private String customDir = null;

    public final Builder setActivity(@NonNull Activity activity) {
      this.activity = activity;
      return this;
    }

    public final Builder setMimeType(@NonNull MimeType mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public final Builder setPhotoName(@NonNull String name) {
      this.fileName = name;
      return this;
    }

    public final Builder setStorageType(@NonNull StorageType type) {
      this.storageType = type;
      return this;
    }

    public final Builder setExternalStoragePermission(@NonNull ExternalStoragePermission externalStoragePermission) {
      this.externalStoragePermission = externalStoragePermission;
      return this;
    }

    public final Builder enableRequestPermission(@NonNull Boolean value) {
      this.enablePermission = value;
      return this;
    }

    public final Builder saveInCustomDirectory(@NonNull String directoryName){
      this.customDir = directoryName;
      return this;
    }

    public final PhotoEasy build() {
      if (activity == null)
        throw new IllegalArgumentException("activity not set or null");
      if (externalStoragePermission != null && storageType != StorageType.external)
        throw new IllegalArgumentException("permission for external storage is settable only with external storage type");
      if (externalStoragePermission != null && !enablePermission)
        throw new IllegalArgumentException("set enable request permission");

      final PhotoEasy instance = new PhotoEasy(activity, storageType, externalStoragePermission, enablePermission);
      instance.setMimeType(mimeType);
      instance.setName(fileName);
      instance.setCustomDirectory(customDir);

      return instance;
    }
  }

  // Storage Type
  public enum StorageType {
    internal,
    external,
    media
  }

  // Picture mime type
  public enum MimeType {
    imageJpeg,
    imagePng,
    imageWebp
  }
}
