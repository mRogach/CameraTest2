package com.example.mykhailo.cameratest2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by
 * Mykhailo on 7/16/2016.
 */

public class PreviewCamera extends SurfaceView implements SurfaceHolder.Callback {

    private int CAMERA_ID = 0;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private Context mContext;
    private WindowManager windowManager;
    private boolean FULL_SCREEN = true;
    MediaRecorder mediaRecorder;
    File videoFile;

    public PreviewCamera(Context context) {
        super(context);
        init(context);
    }

    public PreviewCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        File pictures = mContext.getExternalCacheDir();
        videoFile = new File(pictures, "myvideo.mp4");
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null)
            return;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setDisplayOrientation(setCameraDisplayOrientation(CAMERA_ID));
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera == null)
            return;
        mHolder.removeCallback(this);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public boolean hasFlash() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void turnOnFlash() {
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    public void turnAutoFlash() {
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    public void turnOffFlash() {
        mCamera.stopPreview();
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    public void setPreviewSize(boolean fullScreen) {
        Display display = windowManager.getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
        } else {
            display.getSize(point);
        }

        final List<Camera.Size> listSize = mCamera.getParameters().getSupportedPreviewSizes();
        Camera.Size bestPreviewSize = getMaxPreviewSize(listSize);
        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, point.x, point.y);

        if (widthIsMax) {
            rectPreview.set(0, 0, bestPreviewSize.width, bestPreviewSize.height);
        } else {
            rectPreview.set(0, 0, bestPreviewSize.height, bestPreviewSize.width);
        }
        Matrix matrix = new Matrix();
        if (!fullScreen) {
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        matrix.mapRect(rectPreview);
        getLayoutParams().height = (int) (rectPreview.bottom);
        getLayoutParams().width = (int) (rectPreview.right);
        requestLayout();
    }

    private Camera.Size getMaxPreviewSize(List<Camera.Size> listSize) {
        List<Camera.Size> sizes = new ArrayList<>(listSize);
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.width > rhs.width && lhs.height > rhs.height ? -1 : 1;
            }
        });
        return sizes.get(0);
    }

    public int setCameraDisplayOrientation(int cameraId) {
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = ((360 - degrees) - info.orientation);
            result += 360;
        }
        return result % 360;
    }

    public int getCameraId() {
        if (CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        return CAMERA_ID;
    }

    public void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
    }

    public void startCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(getHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void takePhoto() {
//        mCamera.autoFocus(new Camera.AutoFocusCallback() {
//            public void onAutoFocus(boolean success, Camera camera) {
//                if (success) {
                    mCamera.takePicture(null, null, new PhotoHandler(mContext, setCameraDisplayOrientation(CAMERA_ID)));
//                }
//            }
//        });
    }

    public void openCamera(int cameraId) {
        mCamera = Camera.open(cameraId);
        mParameters = mCamera.getParameters();
        init();
        setPreviewSize(FULL_SCREEN);
    }

    private void init() {
        final List<String> focusModes = mParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.setParameters(mParameters);
    }

    private boolean prepareVideoRecorder() {

        mCamera.unlock();

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setOrientationHint(setCameraDisplayOrientation(CAMERA_ID));
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void onClickStartRecord() {
        if (prepareVideoRecorder()) {
            mediaRecorder.start();
        } else {
            releaseMediaRecorder();
        }
    }

    public void onClickStopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseMediaRecorder();
        }
    }


    public void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            mCamera.lock();
        }
    }

}
