package com.example.mykhailo.cameratest2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by
 * Mykhailo on 7/16/2016.
 */

public class TestPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";
    private int CAMERA_ID = 0;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private Context mContext;
    private WindowManager windowManager;
    private boolean FULL_SCREEN = true;
    private GestureDetector gestureDetector;
    private Camera.Size optimal;
    private int mCameraId;

    public TestPreview(Context context) {
        super(context);
        init(context);
    }

    public TestPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    public void startPreview(final SurfaceHolder _holder) {
        Log.v(TAG, "Starting preview");
        try {
            mCamera.setPreviewDisplay(_holder);
            mCamera.startPreview();
        } catch (final IOException | RuntimeException _e) {
            final String error = "Failed to start preview, cause: " + _e.toString();
            Log.v(TAG, error);
        }
    }

    public final void releaseCamera() {
        Log.v(TAG, "Releasing camera");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int _width, int height) {
        if (mHolder.getSurface() == null)
            return;
        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();
        try {
            initialize(_width);
            startPreview(_holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
    }

    private final void initialize(final int _width) {
        final Camera.Parameters parameters = mCamera.getParameters();

        final Camera.Size preview = getMaxPreviewSize(parameters.getSupportedPreviewSizes());
        Log.v(TAG, String.format("Got preview size, width: %d, height: %d", preview.width, preview.height));
        parameters.setPreviewSize(preview.width, preview.height);

        final Camera.Size picture = getBestPictureSize(parameters);
        parameters.setPictureSize(picture.width, picture.height);

        final List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(parameters);
        int width;
        int height = 0;

        final double ratio = getRatio(preview.width, preview.height);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
        } else {
            display.getSize(point);
        }
        float previewWidth;
        float previewHeight;
        if (preview.height > preview.width) {
            previewHeight = preview.height;
            previewWidth = preview.width;
        } else {
            previewHeight = preview.width;
            previewWidth = preview.height;
        }
        if (((float)point.y / (float)point.x) <= previewHeight / previewWidth) {
            width = point.x;
            if (point.y != previewHeight) {
                height = (int) (width * ratio);
            } else {
                height = point.y;
            }
        } else {
            height = point.y;
            width = (int) (height / ratio);
        }

        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.width = width;
        params.height = height;
        setLayoutParams(params);
    }

    private final double getRatio(final int _width, final int _height) {
        Log.v(TAG, String.format("Get ratio from width: %d, height: %d", _width, _height));
        final double ratio = _width > _height ? _width / (double) _height : _height / (double) _width;
        Log.v(TAG, "Returning ratio: " + ratio);
        return ratio;
    }

    private final Camera.Size getBestPreviewSize(final int _width, final Camera.Parameters _parameters) {
        Log.v(TAG, String.format("Getting best preview size, view width: %d", _width));
        final List<Camera.Size> sizes = _parameters.getSupportedPreviewSizes();
        Log.v(TAG, "Supported preview sizes: " + getSizesString(sizes));
        Camera.Size preview = getBiggestSize(sizes);
        for (final Camera.Size size : sizes) {
            if (isBigger(size, preview)) continue;
            if (size.width > size.height) {
                if (size.height >= _width) preview = size;
            } else {
                if (size.width >= _width) preview = size;
            }
        }
        Log.v(TAG, "Returning best preview size: " + getSizeString(preview));
        return preview;
    }

    private final Camera.Size getBestPictureSize(final Camera.Parameters _parameters) {
        Log.v(TAG, String.format("Getting best picture size"));
        final List<Camera.Size> sizes = _parameters.getSupportedPictureSizes();
        Log.v(TAG, "Supported picture sizes: " + getSizesString(sizes));
        Camera.Size picture = getBiggestSize(sizes);
        for (final Camera.Size size : sizes) {
            if (isBigger(size, picture)) continue;
            if (size.width >= 1000 && size.height >= 1000) {
                picture = size;
            }
        }
        Log.v(TAG, "Returning best picture size: " + getSizeString(picture));
        return picture;
    }

    private final Camera.Size getBiggestSize(final List<Camera.Size> _sizes) {
        Log.v(TAG, "Getting biggest size from sizes: " + getSizesString(_sizes));
        Camera.Size biggest = _sizes.get(0);
        for (final Camera.Size size : _sizes) {
            if (size.width * size.height > biggest.width * biggest.height) {
                biggest = size;
            }
        }
        Log.v(TAG, "Returning biggest size: " + getSizeString(biggest));
        return biggest;
    }

    private final boolean isBigger(final Camera.Size _size1, final Camera.Size _size2) {
        Log.v(TAG, String.format("Entering is bigger size1: %s, size2: %s", getSizeString(_size1), getSizeString(_size2)));
        final boolean bigger = _size1.width * _size1.height > _size2.width * _size2.height;
        Log.v(TAG, "Returning is bigger: " + bigger);
        return bigger;
    }

    private final String getSizesString(final List<Camera.Size> _sizes) {
        String str = "";
        for (final Camera.Size size : _sizes) {
            str += getSizeString(size) + " ";
        }
        return str;
    }

    private final String getSizeString(final Camera.Size _size) {
        return String.format("[%dx%d]", _size.width, _size.height);
    }

    private final boolean isCameraPrepared() {
        Log.v(TAG, "Entering is camera prepared");
        final boolean prepared = mCamera != null;
        Log.v(TAG, "Returning camera prepared: " + prepared);
        return prepared;
    }

    private final boolean prepareCamera() {
        Log.v(TAG, "Preparing camera");
        try {
            mCamera = Camera.open(mCameraId);
            mCamera.setDisplayOrientation(setCameraDisplayOrientation(mCameraId));
            Log.v(TAG, "Camera prepared successfully");
            return true;
        } catch (final Exception _e) {
            Log.v(TAG, "Failed to prepare camera, cause: " + _e.toString());
            return false;
        }
    }

    private final int getCameraOrientation(final int _cameraId) {
        Log.v(TAG, "Entering getCameraOrientation()");
        final Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(_cameraId, info);
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        final int rotation = windowManager.getDefaultDisplay().getRotation();
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

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        Log.v(TAG, "Returning camera orientation: " + result);
        return result;
    }

    public boolean hasFlash() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void turnOnFlash() {
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
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
        final List<Camera.Size> listSize = mCamera.getParameters().getSupportedPreviewSizes();
        optimal = getMaxPreviewSize(listSize);

        Display display = windowManager.getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(point);
        } else {
            display.getSize(point);
        }


        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, point.x, point.y);

        if (widthIsMax) {
            rectPreview.set(0, 0, optimal.width, optimal.height);
        } else {
            rectPreview.set(0, 0, optimal.height, optimal.width);
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
        getLayoutParams().width = (int) (rectPreview.right);
        getLayoutParams().height = (int) (rectPreview.bottom);
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
        result = result % 360;
        return result;
//        mCamera.setDisplayOrientation(result);
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
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.takePicture(null, null, new PhotoHandler(mContext));
                }
            }
        });
    }

    public final void switchCamera() {
        Log.v(TAG, "Switching camera");
        if (Camera.getNumberOfCameras() <= 1) {
            Log.v(TAG, String.format("Device has %d camera(s), can't switch", Camera.getNumberOfCameras()));
            return;
        }

        switch (mCameraId) {
            case Camera.CameraInfo.CAMERA_FACING_BACK:
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                break;
            case Camera.CameraInfo.CAMERA_FACING_FRONT:
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                break;
        }

        releaseCamera();
        prepareCamera();
        if (isCameraPrepared()) initialize(getWidth());
        startPreview(mHolder);
    }

    public void openCamera(int cameraId) {
        mCamera = Camera.open(cameraId);
        mCamera.setDisplayOrientation(getCameraOrientation(cameraId));
    }
}
