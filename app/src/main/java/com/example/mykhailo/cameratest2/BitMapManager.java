package com.example.mykhailo.cameratest2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by
 * mRogach on 29.01.2016.
 */

public class BitMapManager {

    public static Bitmap getRotateImage(final String _imagePath, final int _reqWidth, final int _reqHeight) {
        Bitmap bitmap = decodeBitmap(_imagePath, _reqWidth, _reqHeight);
        Bitmap newBitmap = null;
        try {
            ExifInterface ei = new ExifInterface(_imagePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    newBitmap = rotateBitmap(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    newBitmap = rotateBitmap(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    newBitmap = rotateBitmap(bitmap, 270);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    newBitmap = rotateBitmap(bitmap, 0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (newBitmap == null)
            newBitmap = bitmap;

        return newBitmap;
    }

    public static Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private static Bitmap decodeBitmap(String _filePath, int reqWidth, int reqHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap first = BitmapFactory.decodeFile(_filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(_filePath, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getBitmapFromDataArray(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static String saveBitmap(Context context, Bitmap bitmap) {

        String photoFile = "Picture.jpg";

        String filename = getDir(context).getPath() + File.separator + photoFile;
        OutputStream fOut;
        File file;
        try {
            file = new File(filename);
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
        return file.getAbsolutePath();
    }

    private static File getDir(Context context) {
        return new File(context.getExternalCacheDir().toString());
    }
}
