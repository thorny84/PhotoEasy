package com.thorny.photoeasy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * PhotoEasy is a framework that simplifies the request for native use of the device's default camera. Instant PhotoEasy,
 * use the {@link PhotoEasy#takePhoto()} method to create the file that contains photo.
 * To start the camera call startActivityForResult({@link PhotoEasy#getIntentPhoto()},Request Code)
 * <p>
 * For other read file readme.txt
 */
public class PhotoEasy {

    public static String IMAGE_EXTENSION = "jpg";
    public static String NAME_PHOTO = "P_EASY";
    public final File PUBLIC_EXTERNAL_STORAGE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    public final File PRIVATE_EXTERNAL_STORAGE;
    public final File INTERNAL_STORAGE;
    private final String LIBRARY_NAME = "PhotoEasy";
    private final Intent INTENT_IMAGE_CAPTURE;
    private File imageFile = null;
    private File fileDirectory;
    private String photoPath = null;

    /**
     * Capture an image and return it
     */
    public PhotoEasy(Context context) {

        PRIVATE_EXTERNAL_STORAGE = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        INTERNAL_STORAGE = context.getFilesDir();
        fileDirectory = PRIVATE_EXTERNAL_STORAGE;

        INTENT_IMAGE_CAPTURE = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Control camera exist
        if (INTENT_IMAGE_CAPTURE.resolveActivity(context.getPackageManager()) == null) {
            Log.e(LIBRARY_NAME, "Camera is off or not exist");
            return;
        }

        Log.i(LIBRARY_NAME, "Capture started");

    }

    /**
     * Check External storage exist, make file container photo and complete intent request
     */
    public void takePhoto() {

        if (!haveExternalStorage())
            setFileDirectory(INTERNAL_STORAGE);

        makeFileImage();

        if (imageFile != null) {
            Log.i(LIBRARY_NAME, "Photo save in " + photoPath);
            Uri photoURI = Uri.fromFile(imageFile);
            INTENT_IMAGE_CAPTURE.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        }
    }


    /**
     * Create a unique file for your photo
     */
    private void makeFileImage() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        String imageFileName = NAME_PHOTO + "_" + timeStamp + "_";
        File image;
        try {
            image = File.createTempFile(
                    imageFileName,
                    "." + IMAGE_EXTENSION,
                    fileDirectory
            );
        } catch (IOException e) {
            Log.e(LIBRARY_NAME, "File of the photo not created");
            return;
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(LIBRARY_NAME, "File of the photo creation denied");
            return;
        }

        photoPath = image.getAbsolutePath();
        imageFile = image;

    }

    /**
     * Set directory you want use to save the photos, if set an external storage and this is not available the library
     * set internal storage in default
     *
     * @param storage there are three parameters to use,
     *                {@link PhotoEasy#PRIVATE_EXTERNAL_STORAGE} save in external storage, this photos is not
     *                visible at other app, is not always available and will be deleted when the app will be uninstalled,
     *                {@link PhotoEasy#PUBLIC_EXTERNAL_STORAGE} save in external storage, this photos is visible
     *                at other app, is not always available and not will be not deleted at app uninstall,
     *                {@link PhotoEasy#INTERNAL_STORAGE} save photos in internal storage, this is looks like {@link PhotoEasy#PRIVATE_EXTERNAL_STORAGE}
     *                but is always available
     */
    public void setFileDirectory(File storage) {
        if (storage == PRIVATE_EXTERNAL_STORAGE || storage == PUBLIC_EXTERNAL_STORAGE || storage == INTERNAL_STORAGE)
            fileDirectory = storage;
    }

    /**
     * Control if device have external storage
     */
    public boolean haveExternalStorage() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Get absolute path of photo, call this after {@link PhotoEasy#takePhoto()}
     *
     * @return absolute path or null
     */
    public String getPhotoPath() {
        return photoPath;
    }

    public Intent getIntentPhoto() {
        return INTENT_IMAGE_CAPTURE;
    }

    /**
     * @param view
     * @param photoPath
     * @return
     */
    public Bitmap displayPhoto(ImageView view, String photoPath) {

        int wPhotoView = view.getWidth();
        int hPhotoView = view.getHeight();

        return resizePhoto(wPhotoView, hPhotoView, photoPath);

    }

    /**
     * Scale bitmap and choice the best orientation
     *
     * @param wView width of view
     * @param hView height of view
     * @param path  url of image
     * @return image scaled and best oriented or null
     */
    private Bitmap resizePhoto(int wView, int hView, String path) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scalePhoto = Math.min(photoW / wView, photoH / hView);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scalePhoto;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = null;
        Matrix matrix = new Matrix();

        try {
            ExifInterface bitmapOrientation = new ExifInterface(path);
            Log.d("orientation", "" + bitmapOrientation.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL));
            switch (bitmapOrientation.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    bitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(path, bmOptions), 0, 0, wView, hView, matrix, false);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    bitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(path, bmOptions), 0, 0, wView, hView, matrix, false);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    bitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(path, bmOptions), 0, 0, wView, hView, matrix, false);
                    break;
                default:
                    matrix.postRotate(0);
                    bitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(path, bmOptions), 0, 0, wView, hView, matrix, false);
                    break;
            }

        } catch (IOException e) {
            Log.e(LIBRARY_NAME, "Error i/o");
            e.printStackTrace();
        }

        bmOptions.inPurgeable = false;

        return bitmap;

    }
}
