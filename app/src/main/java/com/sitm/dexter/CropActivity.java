package com.sitm.dexter;


import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;

import android.provider.OpenableColumns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//Comments will not be supplied for this file as they are not limited to the scope of the issue to be solved doesn't involve this file.
public class CropActivity extends AppCompatActivity {
    public static final String INTENT_ASPECT_RATIO_X = "aspect_ratio_x";
    public static final String INTENT_ASPECT_RATIO_Y = "aspect_ratio_Y";
    public static final String INTENT_LOCK_ASPECT_RATIO = "lock_aspect_ratio";
    public static final String INTENT_BITMAP_MAX_WIDTH = "max_width";
    public static final String INTENT_BITMAP_MAX_HEIGHT = "max_height";
    public static final String INTENT_SOURCE_URI = "source_uri";

    private boolean lockAspectRatio = false;
    private int ASPECT_RATIO_X = 16, ASPECT_RATIO_Y = 9, bitmapMaxWidth = 1000, bitmapMaxHeight = 1000;
    private View cancel,rotate,done;
    private CropImageView mCropImageView;
    private Uri mCropImageUri;

    @Override
    protected void onCreate(Bundle  savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mCropImageView = (CropImageView)  findViewById(R.id.CropImageView);
        cancel=findViewById(R.id.cancel);
        rotate=findViewById(R.id.rotate);
        done=findViewById(R.id.done);
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_image_intent_null), Toast.LENGTH_LONG).show();
            return;
        }

        ASPECT_RATIO_X = intent.getIntExtra(INTENT_ASPECT_RATIO_X, ASPECT_RATIO_X);
        ASPECT_RATIO_Y = intent.getIntExtra(INTENT_ASPECT_RATIO_Y, ASPECT_RATIO_Y);
        lockAspectRatio = intent.getBooleanExtra(INTENT_LOCK_ASPECT_RATIO, false);
        bitmapMaxWidth = intent.getIntExtra(INTENT_BITMAP_MAX_WIDTH, bitmapMaxWidth);
        bitmapMaxHeight = intent.getIntExtra(INTENT_BITMAP_MAX_HEIGHT, bitmapMaxHeight);
        mCropImageUri = intent.getParcelableExtra(INTENT_SOURCE_URI);

        if(lockAspectRatio) {
            mCropImageView.setFixedAspectRatio(true);
            mCropImageView.setAspectRatio(ASPECT_RATIO_X, ASPECT_RATIO_Y);
        }
        mCropImageView.setGuidelines(CropImageView.Guidelines.ON);
        mCropImageView.setImageUriAsync(mCropImageUri);

    }

    /**
     * On load image button click, start pick  image chooser activity.
     */

    public void onRotate(View view) {
        mCropImageView.rotateImage(90);
    }
    public void onCancel(View view) {
        cancel.setEnabled(false);
        rotate.setEnabled(false);
        done.setEnabled(false);
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    /**
     * Crop the image and set it back to the  cropping view.
     */
    public void onCropImageClick(View view) throws IOException {
        done.setEnabled(false);
        cancel.setEnabled(false);
        rotate.setEnabled(false);
        Bitmap cropped =  mCropImageView.getCroppedImage(bitmapMaxWidth, bitmapMaxHeight);
        String filename=queryName(getContentResolver(), mCropImageUri);
        File path = new File(getExternalCacheDir(), "camera");
        File image = new File(path, filename);
        FileOutputStream stream= null;
        try {
            stream = new FileOutputStream(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            done.setEnabled(true);
            cancel.setEnabled(true);
            rotate.setEnabled(true);
        }
        cropped.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //Cleanup
        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            done.setEnabled(true);
            cancel.setEnabled(true);
            rotate.setEnabled(true);
        }
        cropped.recycle();
        if (cropped != null)
        {
            Intent intent = new Intent();
            intent.putExtra("path", mCropImageUri);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }


    }



    private static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor = resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }






}
