package com.hrawat.selfiecamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class HomeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    private Mat mRgba;
    private JavaCameraView javaCameraView;
    private boolean isFrontCamera;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
    }

    private void initViews() {
        ImageView imageView = findViewById(R.id.iv_rotate_camera);
        javaCameraView = findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        isFrontCamera = true;
        javaCameraView.setCvCameraViewListener(this);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFrontCamera) {
                    javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                    isFrontCamera = false;
                } else {
                    javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                    isFrontCamera = true;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV loaded Successfully");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "OpenCV not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, baseLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width, height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
//        Mat src = imread("0.jpg", 1);
        Mat dst = new Mat();//(src.rows,src.cols,CV_8UC4);
        Mat tmp = new Mat();
        Mat alpha = new Mat();
        Imgproc.cvtColor(mRgba, tmp, Imgproc.COLOR_RGB2BGRA);
        Imgproc.threshold(tmp, alpha, 100, 255, THRESH_BINARY);
        List<Mat> rgb = new ArrayList<>();
        Core.split(mRgba, rgb);
        rgb.add(alpha);
//        Mat[] rgba=new Mat()[rgb[0],rgb[1],rgb[2],alpha];
        Core.merge(rgb, dst);
//        imwrite("dst.png", dst);
        return dst;
    }
}
