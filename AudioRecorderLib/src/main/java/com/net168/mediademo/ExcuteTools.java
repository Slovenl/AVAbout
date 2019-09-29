package com.net168.mediademo;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class ExcuteTools {

    private SurfaceView mCameraView,mCodecView;
    private Camera mCamera;
    private int mPreviewWidth,mPreviewHeight;

    private CodecHelper codecHelper;

    private void initView(){
        openCamera();
        mCameraView.getHolder().addCallback(cameraHolderCallback);
        mCodecView.getHolder().addCallback(codecCallback);
    }

    private void openCamera(){
        if (mCamera != null){
            mCamera.setPreviewCallback(null);   //在停止camera preview的时候，应该先把previewCallback设置为null，否则会出现不必要的错误
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mCamera = Camera.open();
        setCameraParams(mCamera);
    }

    /**
     * 设置摄像头参数，主要是摄像头支持的分辨率和颜色空间（YUV,NV12等）
     * @param camera
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private void setCameraParams(Camera camera){
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        //把第一组分辨率设置为previewWidth 和 previewHeight
        mPreviewWidth = sizeList.get(0).width;
        mPreviewHeight = sizeList.get(0).height;
        parameters.setPreviewSize(mPreviewWidth,mPreviewHeight);
        parameters.setPictureSize(mPreviewWidth,mPreviewHeight);

        //设置颜色空间，如果不知道自己摄像头的颜色空间，可以使用parameters.getPreviewFormat()获取
        parameters.setPictureFormat(ImageFormat.NV21);
        camera.setParameters(parameters);
    }

    SurfaceHolder.Callback cameraHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            try {
                //mCamera应该在surface创建后再startpreview
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.setPreviewCallback(previewCallback);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    };

    SurfaceHolder.Callback codecCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            codecHelper = new CodecHelper(mPreviewWidth,mPreviewHeight,surfaceHolder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    };

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            codecHelper.codec(bytes);
        }
    };

}
