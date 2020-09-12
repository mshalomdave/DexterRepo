package com.sitm.dexter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static androidx.core.content.FileProvider.getUriForFile;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView imgProfile;
    View imgCamera;

    public static final int REQUEST_IMAGE_CAPTURE = 10;
    public static final int REQUEST_GALLERY_IMAGE = 11;
    public static final int REQUEST_CROP_IMAGE = 12;

    static AlertDialog myDialog;

    private boolean lockAspectRatio = false;
    private int ASPECT_RATIO_X = 16, ASPECT_RATIO_Y = 9, bitmapMaxWidth = 1000, bitmapMaxHeight = 1000;

    public static String fileName;

    protected static int picFlag=2;

    Bitmap bitmap;

    //Picker Option Listener Interface
    protected interface PickerOptionListener {
        void onTakeCameraSelected();
        void onChooseGallerySelected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set Views
        imgCamera=findViewById(R.id.img_camera);
        imgProfile=findViewById(R.id.img_profile);

        //Set Up on click Listeners
        imgProfile.setOnClickListener(this);
        imgCamera.setOnClickListener(this);

        //SetUp Toolbar
        Toolbar toolbar;
        toolbar=(Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setTitle("Add Student");

        //Load Default Profile Photo
        loadProfileDefault();

    }
    private void loadProfile(String url) {
    GlideApp.with(this).load(url).into(imgProfile);
    imgProfile.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
}
    private void loadProfileDefault() {
        GlideApp.with(this).load(R.drawable.baseline_account_circle_black_48).into(imgProfile);
        imgProfile.setColorFilter(ContextCompat.getColor(this, R.color.profile_default_tint));
    }


    private void showImagePickerOptions() {
        MainActivity.showImagePickerOptions(this, new PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
            // If camera is selected,open Camera
                launchCamera();
            }

            @Override
            public void onChooseGallerySelected() {
                // If Gallery  is selected,go to Gallery
                launchGallery();
            }
        });
    }

    protected static void showImagePickerOptions(Context context, MainActivity.PickerOptionListener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.PhotoDialogTheme);
        builder.setTitle(context.getString(R.string.lbl_set_profile_photo));

        // add a list
        String[] photos = {context.getString(R.string.lbl_take_camera_picture), context.getString(R.string.lbl_choose_from_gallery)};
        builder.setItems(photos, (dialog, which) -> {
            switch (which) {
                case 0:
                    listener.onTakeCameraSelected();
                    break;
                case 1:
                    listener.onChooseGallerySelected();
                    break;
            }
        });

        // create and show the alert dialog
        myDialog = builder.create();
        myDialog.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        picFlag = 2;
                    }
                }
        );
        picFlag=0;
        myDialog.show();
    }

    private void launchCamera() {
        //Set Options for camera launch
        ASPECT_RATIO_X = 1;
        ASPECT_RATIO_Y =1;
        lockAspectRatio = true;
        bitmapMaxWidth = 1000;
        bitmapMaxHeight = 1000;
        takeCameraImage();
    }

    private void launchGallery() {
        //Set Options for Gallery Launch
        ASPECT_RATIO_X = 1;
        ASPECT_RATIO_Y =1;
        lockAspectRatio = true;
        bitmapMaxWidth = 1000;
        bitmapMaxHeight = 1000;
        chooseImageFromGallery();
    }
    private void takeCameraImage() {
        //Check For Permissions
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            //If Permissions are granted, open Camera
                            fileName = System.currentTimeMillis() + ".png";
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName));
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void chooseImageFromGallery() {
        //Check for Permissions
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            //If Permissions are granted, Got to Gallery
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                //After taking Picture, check resultCode
                if (resultCode == RESULT_OK) {
                    //if all is fine,Crop Image
                    cropImage(getCacheImagePath(fileName));
                    Log.e("Let's go","goo");
                } else {
                    //if all is not fine,Cancel
                    setResultCancelled();
                }
                break;
            case REQUEST_GALLERY_IMAGE:
                //After choosing Picture, check resultCode
                if (resultCode == RESULT_OK) {
                    //if all is fine,Crop Image
                    Uri imageUri = data.getData();
                    cropImage(imageUri);
                } else {
                    //if all is not fine,Cancel
                    setResultCancelled();
                }
                break;
            case REQUEST_CROP_IMAGE:
                //After cropping Picture, check resultCode
                if (resultCode == RESULT_OK) {
                    //if all is fine,Setup Bitmap
                    handleCropResult(data);
                } else {
                    //if all is not fine,Cancel
                    setResultCancelled();
                    //Temporary Clear Local Cache
                    //This will be created here for this repo to prevent keeping unnecessary data to user's phone as it has some issues
                    clearCache(this);
                }
                break;


            default:
                setResultCancelled();

        }

    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAppTheme_Dark_Dialog);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //On click of either views. Run setPhoto Method

            case R.id.img_camera:
                setPhoto();
                break;
            case  R.id.img_profile:
                setPhoto();
                break;

            default:
                break;
        }
    }


    private void setPhoto(){
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                        //If Permissions are granted, show Image Picker Dialog
                            showImagePickerOptions();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            //If Permissions are denied, show Go to Settings Dialog which explains a bit about the need for these permissions
                            showSettingsDialog();
                            }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }





    private void cropImage(Uri sourceUri) {
        Log.e("Let's crop","");
        Intent intent = new Intent(MainActivity.this, CropActivity.class);

        // setting aspect ratio
        intent.putExtra(CropActivity.INTENT_LOCK_ASPECT_RATIO, lockAspectRatio);
        intent.putExtra(CropActivity.INTENT_ASPECT_RATIO_X, ASPECT_RATIO_X); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(CropActivity.INTENT_ASPECT_RATIO_Y, ASPECT_RATIO_Y);


        // setting maximum bitmap width and height
        intent.putExtra(CropActivity.INTENT_BITMAP_MAX_WIDTH, bitmapMaxWidth);
        intent.putExtra(CropActivity.INTENT_BITMAP_MAX_HEIGHT, bitmapMaxHeight);
        intent.putExtra(CropActivity.INTENT_SOURCE_URI, sourceUri);


        startActivityForResult(intent, REQUEST_CROP_IMAGE);
    }

    private void handleCropResult(Intent data) {
        if (data == null) {
            setResultCancelled();
            return;
        }
        Uri uri = data.getParcelableExtra("path");
        try {
            // You can update this bitmap to your server
           bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            // loading profile image from local cache
            loadProfile(uri.toString());
            Toast.makeText(this, "Picture Set", Toast.LENGTH_SHORT).show();
            //Temporary Clear Local Cache
            //This will be created here for this repo to prevent keeping unnecessary data to user's phone as it has some issues
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do this after 5s = 5000ms
                    clearCache(getApplicationContext());
                }
            }, 5000);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setResultCancelled() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
    }
    /**
     * Calling this will get the path for the Cached Image
     */

    private Uri getCacheImagePath(String fileName) {
        File path=null;
        try {
            path = new File(getExternalCacheDir(), "camera");
            Log.e("Error 1","here");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error 1","here"+e.getMessage());
        }
        try {
            if (!path.exists()) path.mkdirs();
            Log.e("Error 2","here");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error 2","here"+e.getMessage());
        }
        File image=null;
        try {
            image = new File(path, fileName);
            Log.e("Error 3","here");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error 3","here"+e.getMessage());
        }
        Log.e("Error 4","here: "+getUriForFile(MainActivity.this, getPackageName() + ".provider", image));

        return getUriForFile(MainActivity.this, getPackageName() + ".provider", image);
    }


    /**
     * Calling this will delete the images from cache directory
     * useful to clear some memory
     */
    public static void clearCache(Context context) {
        File path = new File(context.getExternalCacheDir(), "camera");
        if (path.exists() && path.isDirectory()) {
            for (File child : path.listFiles()) {
                child.delete();
            }
        }
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //When the dialog is up and the screen in rotated,close it and open it to bring a correct UI Presentation
        if(picFlag==0){
            myDialog.dismiss();
            picFlag=0;
            myDialog.show();
        }
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
    //Save File Name
        outState.putString("fileName",fileName);
    //Save Options
        outState.putBoolean("lockAspectRatio",lockAspectRatio);
        outState.putInt("ASPECT_RATIO_X",ASPECT_RATIO_X);
        outState.putInt("ASPECT_RATIO_Y",ASPECT_RATIO_Y);
        outState.putInt("bitmapMaxWidth",bitmapMaxWidth);
        outState.putInt("bitmapMaxHeight",bitmapMaxHeight);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Restore File Name
        fileName=savedInstanceState.getString("fileName");

        //Restore Options
        lockAspectRatio=savedInstanceState.getBoolean("lockAspectRatio",true);
        ASPECT_RATIO_X=savedInstanceState.getInt("ASPECT_RATIO_X",1);
        ASPECT_RATIO_Y=savedInstanceState.getInt("ASPECT_RATIO_Y",1);
        bitmapMaxWidth=savedInstanceState.getInt("bitmapMaxWidth",1000);
        bitmapMaxHeight=savedInstanceState.getInt("bitmapMaxHeight",1000);
    }

}
