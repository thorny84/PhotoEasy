package com.thorny.photoeasy;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class ExternalStoragePermission {

  private final int REQUEST_CODE = 3423;
  private final Activity activity;
  private final RequestMode mode;

  public ExternalStoragePermission(Activity activity, RequestMode mode) {
    this.activity = activity;
    this.mode = mode;
  }

  public int getRequestCode() {
    return REQUEST_CODE;
  }

  public void init() {
    if (sdkCheck()) {
      if (isPreApi23())
        requestPermissionPreApi23();
      return;
    }
    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      switch (mode) {
        case allControl:
        case requestRationalControl:
          requestPermissionRationale();
          break;
        default:
          activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
      }
    } else {
      if (mode == RequestMode.allControl) {
        requestPermission();
      } else
        activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }
  }

  public final boolean permissionCheck() {
    if (sdkCheck()) {
      if (isPreApi23())
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
      return true;
    }
    int permissionCheck = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    return permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  private boolean sdkCheck() {
    return isPreApi23() || Build.VERSION.SDK_INT > Build.VERSION_CODES.P;
  }

  private boolean isPreApi23() {
    return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M;
  }

  private void requestPermissionPreApi23() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
      switch (mode) {
        case allControl:
        case requestRationalControl:
          requestPermissionRationale();
          break;
        default:
          ActivityCompat.requestPermissions(activity,
              new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
              REQUEST_CODE);
      }
    else if (mode == RequestMode.allControl)
      requestPermission();
    else
      ActivityCompat.requestPermissions(activity,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
          REQUEST_CODE);
  }

  public abstract void requestPermissionRationale();

  public abstract void requestPermission();

  public enum RequestMode {
    alwaysRequest,
    requestRationalControl,
    allControl
  }
}
