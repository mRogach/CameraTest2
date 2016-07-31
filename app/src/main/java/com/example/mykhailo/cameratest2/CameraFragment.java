package com.example.mykhailo.cameratest2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by
 * Mykhailo on 7/16/2016.
 */

public class CameraFragment extends Fragment implements View.OnClickListener {

    private ImageView flash, cameraFacing, ivTakePhoto;
    private MainActivity mainActivity;
    private boolean isFlashing = false;

    public static CameraFragment newInstance() {

        Bundle args = new Bundle();

        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        flash = (ImageView) view.findViewById(R.id.ivFlash_FC);
        cameraFacing = (ImageView) view.findViewById(R.id.ivChangeCamera_FC);
        ivTakePhoto = (ImageView) view.findViewById(R.id.ivTakePhoto_FC);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        flash.setOnClickListener(this);
        cameraFacing.setOnClickListener(this);
        ivTakePhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivFlash_FC:
                if (mainActivity.getCameraPreview().hasFlash()) {
                    if (!isFlashing) {
                        isFlashing = true;
                        mainActivity.getCameraPreview().turnAutoFlash();
                        Log.v("flash", "auto");
                    } else {
                        isFlashing = false;
                        mainActivity.getCameraPreview().turnOffFlash();
                    }
                }
                break;
            case R.id.ivChangeCamera_FC:
                changeCamera();
                break;
            case R.id.ivTakePhoto_FC:
                mainActivity.getCameraPreview().onClickStartRecord();
                break;
        }
    }

    private void changeCamera() {
        mainActivity.getCameraPreview().stopCamera();
        int cameraId = mainActivity.getCameraPreview().getCameraId();
        mainActivity.getCameraPreview().openCamera(cameraId);
        mainActivity.getCameraPreview().setCameraDisplayOrientation(cameraId);
        mainActivity.getCameraPreview().startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mainActivity.getCameraPreview().stopCamera();
        mainActivity.getCameraPreview().releaseMediaRecorder();
    }
}
