package com.example.sasha.okhear.camera;

import android.graphics.Bitmap;
import android.hardware.Camera;

import com.example.sasha.okhear.utils.Utils;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

@EBean(scope = EBean.Scope.Singleton)
public class FrameManager {

    private static final int HAND_SIZE = 100;

    public interface FrameProcessingListener {
        void onHandBitmapCreated(Bitmap bitmap);

        void onHandBytesReady(byte[] bytes);
    }

    private FrameProcessingListener frameProcessingListener;

    public void setFrameProcessingListener(FrameProcessingListener listener) {
        frameProcessingListener = listener;
    }

    @Background
    public void detectHand(byte[] bytes, Camera camera, CascadeClassifier detector, boolean isFrontCamera) {
        Bitmap bitmap = Utils.frameBytesToBitmap(camera, bytes, isFrontCamera);
        Mat rgba = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, rgba);
        Mat gray = Utils.convertToGrayColors(rgba);
        MatOfRect hands = new MatOfRect();
        if (detector != null) {
            detector.detectMultiScale(gray, hands, 1.15, 25, 2, new Size(HAND_SIZE, HAND_SIZE), new Size());
        }
        Rect[] handsArray = hands.toArray();
        org.opencv.android.Utils.matToBitmap(rgba, bitmap);
        if (handsArray.length > 0) {
            bitmap = Utils.cropBitmap(bitmap, handsArray[0]);
            if (frameProcessingListener != null) {
                frameProcessingListener.onHandBitmapCreated(bitmap);
                frameProcessingListener.onHandBytesReady(Utils.getSmallBitmapBytes(bitmap));
            }
        }
    }
}
