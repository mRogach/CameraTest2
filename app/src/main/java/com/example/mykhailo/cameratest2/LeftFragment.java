package com.example.mykhailo.cameratest2;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by
 * Mykhailo on 7/16/2016.
 */

public class LeftFragment extends Fragment {

    private ImageView ivPhoto;

    public static LeftFragment newInstance() {

        Bundle args = new Bundle();

        LeftFragment fragment = new LeftFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_left, container, false);
        ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto_FL);
        return view;
    }

    public void setPhoto() {
        String path = getActivity().getExternalCacheDir().toString() + "/CameraTest2/Picture.jpg";
        Glide.with(getActivity())
                .load(new File(path))
                .error(R.mipmap.ic_launcher)
                .into(ivPhoto);
    }
}
