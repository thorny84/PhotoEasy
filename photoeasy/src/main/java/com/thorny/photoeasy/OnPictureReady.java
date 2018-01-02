package com.thorny.photoeasy;

import android.graphics.Bitmap;

public interface OnPictureReady {
    void onFinish(Bitmap thumbnail);
    void onFailure();
}
