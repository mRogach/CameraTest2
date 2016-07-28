package com.example.mykhailo.cameratest2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.Camera;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private PreviewCamera cameraPreview;
    private FragmentsAdapter mAdapter;
    private ViewPager pager;
    private RelativeLayout rlNav, rlSquare, rlCam, rlParent;
    private ImageView ivNav, ivSquare, ivCam;
    private LinearLayout llButtons;
    private int width, height;
    private int mCurrentSelectedScreen;
    private float oldPositionOffset;
    private float fifty, thirty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview = (PreviewCamera) findViewById(R.id.cameraPreview);
        pager = (ViewPager) findViewById(R.id.viewpager_FT);
        rlNav = (RelativeLayout) findViewById(R.id.rlNav);
        rlSquare = (RelativeLayout) findViewById(R.id.rlSquare);
        rlCam = (RelativeLayout) findViewById(R.id.rlCam);
        rlParent = (RelativeLayout) findViewById(R.id.rlParent);
        ivNav = (ImageView) findViewById(R.id.ivNav);
        ivSquare = (ImageView) findViewById(R.id.ivSquare);
        ivCam = (ImageView) findViewById(R.id.ivCam);
        llButtons = (LinearLayout) findViewById(R.id.llButtons);
        setAdapter();

        width = ivCam.getLayoutParams().width;
        height = ivCam.getLayoutParams().height;
        mCurrentSelectedScreen = pager.getCurrentItem();
        fifty = dipToPixels(this, 25);
        thirty = dipToPixels(this, 18);
        if (mCurrentSelectedScreen == 1) {
            ivNav.getLayoutParams().width = (int) (width + (fifty * (1 - oldPositionOffset)));
            ivNav.getLayoutParams().height = (int) (height + (fifty * (1 - oldPositionOffset)));
            ivNav.requestLayout();
        }
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.v("pager", "currentPosition " + pager.getCurrentItem());
                Log.v("pager", "position " + String.valueOf(position));
                Log.v("pager", "oldPositionOffset " + String.valueOf(oldPositionOffset));
                Log.v("pager", "positionOffset " + String.valueOf(positionOffset));
                if (position > 0 && (position == mCurrentSelectedScreen || position + 1 == mCurrentSelectedScreen)) {
                    if (oldPositionOffset != 0 && oldPositionOffset < positionOffset) {
                        ivCam.getLayoutParams().width = (int) (width + (fifty * positionOffset));
                        ivCam.getLayoutParams().height = (int) (height + (fifty * positionOffset));
                        ivNav.getLayoutParams().width = (int) (width + (fifty * (1 - oldPositionOffset)));
                        ivNav.getLayoutParams().height = (int) (height + (fifty * (1 - oldPositionOffset)));
                        llButtons.setTranslationX((-rlCam.getMeasuredWidth() + thirty) * positionOffset);
                        Log.v("pager", "TransitionX " + String.valueOf((-rlCam.getMeasuredWidth() + 30) * positionOffset));
                        Log.v("pager", "ivCam Closer to next");
                    } else {
                        if (positionOffset != 0 && oldPositionOffset != 0 && oldPositionOffset > positionOffset) {
                            ivCam.getLayoutParams().width = (int) (width + (fifty * (oldPositionOffset)));
                            ivCam.getLayoutParams().height = (int) (height + (fifty * (oldPositionOffset)));
                            ivNav.getLayoutParams().width = (int) (width + (fifty * (1 - positionOffset)));
                            ivNav.getLayoutParams().height = (int) (height + (fifty * (1 - positionOffset)));
                            llButtons.setTranslationX((-rlCam.getMeasuredWidth() + thirty) * (oldPositionOffset));
                            Log.v("pager", "TransitionX " + String.valueOf((rlCam.getMeasuredWidth() + 30) * positionOffset));
                            Log.v("pager", "ivCam Closer to current");
                        }
                    }
                } else {
                    if (oldPositionOffset != 0 && oldPositionOffset < positionOffset) {
                        ivSquare.getLayoutParams().width = (int) (width + (fifty * (1 - positionOffset)));
                        ivSquare.getLayoutParams().height = (int) (height + (fifty * (1 - positionOffset)));
                        ivNav.getLayoutParams().width = (int) (width + (fifty * positionOffset));
                        ivNav.getLayoutParams().height = (int) (height + (fifty * positionOffset));
                        llButtons.setTranslationX((rlCam.getMeasuredWidth() + thirty) * (1 - positionOffset));
                        Log.v("pager", "ivNav");
                    } else {
                        if (oldPositionOffset != 0) {
                            ivSquare.getLayoutParams().width = (int) (width + (fifty * (1 - positionOffset)));
                            ivSquare.getLayoutParams().height = (int) (height + (fifty * (1 - positionOffset)));
                            ivNav.getLayoutParams().width = (int) (width + (fifty * (oldPositionOffset)));
                            ivNav.getLayoutParams().height = (int) (height + (fifty * (oldPositionOffset)));
                            llButtons.setTranslationX((rlCam.getMeasuredWidth() + thirty) * (1 - positionOffset));
                            Log.v("pager", "Measure " + String.valueOf(rlSquare.getMeasuredWidth()));
                            Log.v("pager", "ivSquare");
                        }
                    }
                }
                oldPositionOffset = positionOffset;
                ivNav.requestLayout();
                ivCam.requestLayout();
                ivSquare.requestLayout();
                Log.v("pager", "----------------------------------------------------");
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentSelectedScreen = position;
                if (mAdapter.getItem(position) instanceof LeftFragment) {
                    ((LeftFragment) mAdapter.getItem(position)).setPhoto();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    private void setAdapter() {
        mAdapter = new FragmentsAdapter(getSupportFragmentManager());
        mAdapter.addFragment(LeftFragment.newInstance());
        mAdapter.addFragment(RightFragment.newInstance());
        mAdapter.addFragment(CameraFragment.newInstance());

        pager.setAdapter(mAdapter);
        pager.setCurrentItem(1);
        pager.setOffscreenPageLimit(3);
//        pager.setPageTransformer(true, new ZoomOutPageTransformer());
//        pageIndicator.setViewPager(pager);
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraPreview.openCamera(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraPreview.stopCamera();
    }

    public PreviewCamera getCameraPreview() {
        return cameraPreview;
    }
}
