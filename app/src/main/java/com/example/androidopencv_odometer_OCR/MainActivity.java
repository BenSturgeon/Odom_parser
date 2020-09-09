package com.example.androidopencv_odometer_OCR;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1;

    boolean startYolo = false;
    boolean firstTimeYolo = false;
    Net findOdom;
    Net findNumbers;
    int odd;
    Map<String, Integer> poss_Strings = new HashMap<String, Integer>();


    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");

                // Load native library after(!) OpenCV initialization
                System.loadLibrary("native-lib");

                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST
        );



        setContentView(R.layout.activity_main);

        mOpenCvCameraView = findViewById(R.id.CameraView);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mOpenCvCameraView.setCameraPermissionGranted();
            } else {
                String message = "Camera permission was not granted";
                Log.e(TAG, message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Unexpected permission request");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
//        // get current camera frame as OpenCV Mat object
        Mat frame = inputFrame.rgba();





        if (startYolo == true) {

            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);


            Mat imageBlob = Dnn.blobFromImage(frame, 0.00392, new Size(416, 416), new Scalar(0, 0, 0),/*swapRB*/false, /*crop*/false);


            findOdom.setInput(imageBlob);


            java.util.List<Mat> result = new java.util.ArrayList<Mat>(2);

            List<String> outBlobNames = new java.util.ArrayList<>();
            outBlobNames.add(0, "yolo_16");
            outBlobNames.add(1, "yolo_23");

            findOdom.forward(result, outBlobNames);


            float confThreshold = 0.3f;


            List<Integer> clsIds = new ArrayList<>();
            List<Float> confs = new ArrayList<>();
            List<Rect2d> rects = new ArrayList<>();

            for (int i = 0; i < result.size(); ++i) {

                Mat level = result.get(i);

                for (int j = 0; j < level.rows(); ++j) {
                    Mat row = level.row(j);
                    Mat scores = row.colRange(5, level.cols());

                    Core.MinMaxLocResult mm = Core.minMaxLoc(scores);

                    float confidence = (float) mm.maxVal;


                    Point classIdPoint = mm.maxLoc;

                    if (confidence > confThreshold) {
                        int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                        int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                        int width = (int) (row.get(0, 2)[0] * frame.cols());
                        int height = (int) (row.get(0, 3)[0] * frame.rows());


                        int left = centerX - width / 2;
                        int top = centerY - height / 2;

                        clsIds.add((int) classIdPoint.x);
                        confs.add((float) confidence);

                        rects.add(new Rect2d(left, top, width, height));
                    }
                }
            }
            int ArrayLength = confs.size();


            if (ArrayLength >= 1) {
                // Apply non-maximum suppression procedure.
                float nmsThresh = 0.2f;

                MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));

                Rect2d[] boxesArray = rects.toArray(new Rect2d[0]);

                MatOfRect2d boxes = new MatOfRect2d(boxesArray);

                MatOfInt indices = new MatOfInt();

                Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);

                // Draw result boxes:
                int[] ind = indices.toArray();
                int idx = ind[0];
                Rect2d box = boxesArray[idx];

                int idGuy = clsIds.get(idx);

                float conf = confs.get(idx);
                Rect rec = new Rect((int) box.x, (int) box.y, (int) box.width, (int) box.height);
                Mat frameOdom = new Mat(frame, rec);


                Imgproc.cvtColor(frameOdom, frameOdom, Imgproc.COLOR_RGBA2RGB);


                Mat imageBlob2 = Dnn.blobFromImage(frameOdom, 0.00392, new Size(416, 416), new Scalar(0, 0, 0),/*swapRB*/false, /*crop*/false);


                findNumbers.setInput(imageBlob2);


                java.util.List<Mat> result2 = new java.util.ArrayList<Mat>(2);

                List<String> outBlobNames2 = new java.util.ArrayList<>();
                outBlobNames2.add(0, "yolo_16");
                outBlobNames2.add(1, "yolo_23");

                findNumbers.forward(result2, outBlobNames2);


                List<Integer> clsIds2 = new ArrayList<>();
                List<Float> confs2 = new ArrayList<>();
                List<Rect2d> rects2 = new ArrayList<>();

                for (int i = 0; i < result2.size(); ++i) {

                    Mat level = result2.get(i);

                    for (int j = 0; j < level.rows(); ++j) {
                        Mat row = level.row(j);
                        Mat scores = row.colRange(5, level.cols());

                        Core.MinMaxLocResult mm = Core.minMaxLoc(scores);

                        float confidence = (float) mm.maxVal;


                        Point classIdPoint = mm.maxLoc;

                        if (confidence > confThreshold) {
                            int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                            int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                            int width = (int) (row.get(0, 2)[0] * frame.cols());
                            int height = (int) (row.get(0, 3)[0] * frame.rows());


                            int left = centerX - width / 2;
                            int top = centerY - height / 2;

                            clsIds2.add((int) classIdPoint.x);
                            confs2.add((float) confidence);

                            rects2.add(new Rect2d(left, top, width, height));
                        }
                    }
                }
                int ArrayLength2 = confs2.size();



                if (ArrayLength2 >= 1) {
                    // Apply non-maximum suppression procedure.
                    float nmsThresh2 = 0.2f;

                    MatOfFloat confidences2 = new MatOfFloat(Converters.vector_float_to_Mat(confs2));

                    Rect2d[] boxesArray2 = rects2.toArray(new Rect2d[0]);

                    MatOfRect2d boxes2 = new MatOfRect2d(boxesArray2);

                    MatOfInt indices2 = new MatOfInt();

                    Dnn.NMSBoxes(boxes2, confidences2, confThreshold, nmsThresh2, indices2);

                    // Draw result2 boxes:
                    int[] ind2 = indices2.toArray();
                    String out = "";
                    for (int i = 0; i < ind2.length; ++i) {



                        //Rect2d box2 = boxesArray[idx2];

                        int idGuy2 = clsIds2.get(i);



                        List<String> cocoNames = Arrays.asList("Alpha", "8", "5", "4", ".", "9", "1", "7", "6", "3", "2", "0");




                        out = out + cocoNames.get(idGuy2) + " ";
                        //Imgproc.putText(frame, cocoNames.get(idGuy2) + " " + intConf2 + "%", box2.tl(), 1, 2, new Scalar(255, 255, 0), 2);



                    }
                    String likely = Likely(out);

                    TextView editOut = this.findViewById(R.id.NumberOutput);
                    editOut.setText(likely);
                    System.out.println( editOut.getText() );



                }

            }
        }

            return frame;

    }

    private native void adaptiveThresholdFromJNI(long mat);

    public void YOLO(View view) {

        if (startYolo == false){




            startYolo = true;

            if (firstTimeYolo == false){
                firstTimeYolo = true;
                String alt_model = Environment.getExternalStorageDirectory() + "/dnns/Odom_detect.onnx";
                String tinyYoloCfg = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny_obj(2).cfg" ;
                String tinyYoloWeights = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny_obj_best(1).weights";
                findOdom = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);
                //findOdom = Dnn.readNetFromDarknet(tinyYoloCfg, tinyYoloWeights);



                String tinyNumberscfg = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny_numbers.cfg" ;
                String tinyNumbersweights = Environment.getExternalStorageDirectory() + "/dnns/yolov3-tiny_numbers.weights";
                findNumbers = Dnn.readNetFromDarknet(tinyNumberscfg, tinyNumbersweights);



                //tinyYolo = Dnn.readNetFromTensorflow(effdet);
                //tinyYolo = Dnn.readNetFromTensorflow(effdet);









            }



        }

        else{

            startYolo = false;


        }


    }

    public String Likely(String curr){

        if (poss_Strings.containsKey(curr)==false ) {
            poss_Strings.put(curr, 1);
        }
        else {
            poss_Strings.compute(curr, (key, oldValue) -> oldValue +1);
        }
        return poss_Strings.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
    }

    public void clear(){
        poss_Strings.clear();
        TextView editOut = this.findViewById(R.id.NumberOutput);
        editOut.setText("Nothing detected");

    }


}
