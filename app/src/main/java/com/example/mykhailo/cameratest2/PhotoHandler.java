package com.example.mykhailo.cameratest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by
 * Mykhailo on 7/17/2016.
 */

public class PhotoHandler implements Camera.PictureCallback {

    private final Context context;
    private int mAngle;

    public PhotoHandler(Context context) {
        this.context = context;
    }


    public PhotoHandler(Context context, int angle) {
        this.context = context;
        mAngle = angle;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

//        File pictureFileDir = getDir();
//
//        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
//
//            Toast.makeText(context, "Can't create directory to save image.",
//                    Toast.LENGTH_LONG).show();
//            return;
//
//        }
//        String photoFile = "Picture.jpg";

//        String filename = pictureFileDir.getPath() + File.separator + photoFile;

//        File pictureFile = new File(filename);
        Bitmap bitmap = BitMapManager.getBitmapFromDataArray(data);
//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(data);
//            fos.close();
//            Toast.makeText(context, "New Image saved:" + photoFile,
//                    Toast.LENGTH_LONG).show();
//        } catch (Exception error) {
//            Toast.makeText(context, "Image could not be saved.",
//                    Toast.LENGTH_LONG).show();
//        }
        BitMapManager.saveBitmap(context, BitMapManager.rotateBitmap(bitmap, mAngle));
        camera.startPreview();
    }

    private File getDir() {
        return new File(context.getExternalCacheDir().toString(), "CameraTest2");
    }
}