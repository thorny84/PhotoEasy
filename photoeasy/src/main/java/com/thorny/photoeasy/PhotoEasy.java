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

/**
 * PhotoEasy reduces the complexity of requesting an image via the camera,
 * with various settings that can help you customize the event.
 */
public final class PhotoEasy {

  private final int REQUEST_CAMERA_KEY = 1566;
  private final ContentResolver contentResolver;
  private final Boolean enableRequestPermission;
  private File fileDirectory;
  private ExternalStoragePermission externalStoragePermission;
  MimeType mimeType;
  private String fileName;
  private StorageType storageType;
  private Uri lastUri;
  private String customDirectory;

  private PhotoEasy(
      final Activity activity,
      final StorageType storageType,
      final ExternalStoragePermission externalStoragePermission,
      final boolean enablePermission) {

    setStorageType(activity, storageType);
    setExternalPermission(activity, externalStoragePermission);
    contentResolver = activity.getContentResolver();
    enableRequestPermission = enablePermission;
  }

  private void setStorageType(
      final Activity activity,
      final StorageType type) {
    storageType = type;
    switch (type) {
      case internal:
        fileDirectory = activity.getFilesDir();
        break;
      case external:
        fileDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }
  }

  private void setExternalPermission(
      final Activity activity,
      final ExternalStoragePermission externalStoragePermission) {
    if (externalStoragePermission == null) {
      this.externalStoragePermission = new DefaultStoragePermission(activity);
      return;
    }
    this.externalStoragePermission = externalStoragePermission;
  }

  private void setMimeType(final MimeType mimeType) {
    this.mimeType = mimeType;
  }

  private void setName(final String fileName) {
    this.fileName = fileName;
  }

  private void setCustomDirectory(final String directoryName) {
    this.customDirectory = directoryName;
  }

  public final void onActivityResult(
      final int requestCode,
      final int resultCode,
      @NonNull OnPictureReady onPictureReady) {

    if (requestCode != REQUEST_CAMERA_KEY || resultCode != RESULT_OK)
      return;

    final BitmapCreator bitmapCreator = new BitmapCreator();
    final Bitmap bitmap = bitmapCreator.getBitmapFromContentResolver(contentResolver, lastUri);

    onPictureReady.onFinish(bitmap);
  }

  public final void startActivityForResult(final Activity activity) {

    if (enableRequestPermission)
      if (storageType == StorageType.external || storageType == StorageType.media) {
        if (!isExternalStorageWritable())
          return;
        if (!externalStoragePermission.permissionCheck()) {
          externalStoragePermission.init();
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
    private boolean enablePermission = true;
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

    /**
     * Use to set type of storage.</br>
     *
     * @param type {@link StorageType}
     */
    public final Builder setStorageType(@NonNull StorageType type) {
      this.storageType = type;
      return this;
    }

    public final Builder setExternalStoragePermission(@NonNull ExternalStoragePermission externalStoragePermission) {
      this.externalStoragePermission = externalStoragePermission;
      return this;
    }

    public final Builder enableRequestPermission(boolean value) {
      this.enablePermission = value;
      return this;
    }

    public final Builder saveInCustomDirectory(@NonNull String directoryName) {
      this.customDir = directoryName;
      return this;
    }

    public final PhotoEasy build() {
      if (activity == null)
        throw new IllegalArgumentException("activity not set or null");
      if (externalStoragePermission != null && !(storageType == StorageType.external || storageType == StorageType.media))
        throw new IllegalArgumentException("permission for external storage is settable only with external storage type");
      if (externalStoragePermission != null && !enablePermission)
        throw new IllegalArgumentException("set enable request permission");
      if (customDir != null && storageType != StorageType.media)
        throw new IllegalArgumentException("use custom directory only with storage type media");

      final PhotoEasy instance = new PhotoEasy(activity, storageType, externalStoragePermission, enablePermission);
      instance.setMimeType(mimeType);
      instance.setName(fileName);
      instance.setCustomDirectory(customDir);

      return instance;
    }
  }

  /**
   * Storage type supported.</br>
   * <ul>
   *   <li>
   *     {@link StorageType#internal} App-specific internal storage, intended for the exclusive use of your application,
   *      not accessible from other applications. From the API 29 images are encrypted.
   *   </li>
   *   <li>
   *     {@link StorageType#external} App-specific external storage, these images are available to other applications only with permissions.
   *   </li>
   *   <li>
   *     {@link StorageType#media} Storage in multimedia directory, these images are visible from all applications without the need permissions.
   *   </li>
   * </ul>
   */
  public enum StorageType {
    /**
     * App-specific internal storage, intended for the exclusive use of your application,
     * not accessible from other applications. From the API 29 images are encrypted.
     */
    internal,
    /**
     * App-specific external storage, these images are available to other applications
     * only with permissions.
     */
    external,
    /**
     * Storage in multimedia directory, these images are visible from all applications without the need permissions.
     */
    media
  }

  /**
   * Mime type supported
   */
  public enum MimeType {
    imageJpeg,
    imagePng,
    imageWebp
  }
}
