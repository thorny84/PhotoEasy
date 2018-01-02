package com.thorny.photoeasy;/*
 <<<HEADER PLACEHOLDER>>>
  Creation date: 30/09/2020
 */

import android.app.Activity;

public class DefaultStoragePermission extends ExternalStoragePermission {
  public DefaultStoragePermission(Activity activity) {
    super(activity,RequestMode.alwaysRequest);
  }

  @Override
  public void requestPermissionRationale() {

  }

  @Override
  public void requestPermission() {

  }
}
