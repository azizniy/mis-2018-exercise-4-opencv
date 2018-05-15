package com.example.mis.opencv;


import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;
    String cascade;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    cascade  = initAssetFile("haarcascade_frontalface_default.xml");
                    cascadeClassifier =  new CascadeClassifier(cascade);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);




            //cascadeClassifier.load(cascade);



        //Log.d(TAG, "path = " + path);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (height * 0.2);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {


        Mat gray = inputFrame.gray();
        Mat col  = inputFrame.rgba();

        Mat tmp = gray.clone();
        Imgproc.Canny(gray, tmp, 80, 100);
        Imgproc.cvtColor(tmp, col, Imgproc.COLOR_GRAY2RGBA, 4);

        
        //https://www.mirkosertic.de/blog/2013/07/realtime-face-detection-on-android-using-opencv/
        MatOfRect faces = new MatOfRect();

        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(gray, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        // Draw rectangle on face
        Rect[] facesArray = faces.toArray();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        for (int i = 0; i <facesArray.length; i++){
            //Imgproc.rectangle(col, facesArray[i].tl(), facesArray[i].br(), new Scalar(255, 0, 0, 255), 3);
            //Log.d(TAG, "faces: " + facesArray[i].tl());

            //nose position almost in a center of a rectangle
            Point c = new Point(((facesArray[i].br().x + facesArray[i].tl().x))/2,
                                ((facesArray[i].br().y + facesArray[i].tl().y))/1.9);

            //the radius of circle changes according to the proximity of face
            int r = 20/(displayMetrics.heightPixels/facesArray[i].width);
            //Log.d(TAG, "faces: " + r);

            Imgproc.circle(col,c,r, new Scalar(255,0,0), 30);

            }
        return col;
    }


    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
            Log.d(TAG,"SUCCESS: ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"Error: "+e);
        }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }
}
