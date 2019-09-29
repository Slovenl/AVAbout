package com.net168.audiorecorddemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.net168.audiorecord.AudioCapture;
import com.net168.audiorecord.OnAudioCaptureCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnAudioCaptureCallback {

    private TextView mTipTv;
    private Button mStartInJava;
    private Button mStartInNative;
    private Button mStop;

    private AudioCapture mAudioCapture;
    private File mFile;
    private FileOutputStream mOutputStream;

    private int mStatus = 0;  //0-无采集  1-java 采集  2-native 采集

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartInJava = findViewById(R.id.record_in_java);
        mStartInJava.setOnClickListener(this);
        mStartInNative = findViewById(R.id.record_in_jni);
        mStartInNative.setOnClickListener(this);
        mStop = findViewById(R.id.record_stop);
        mStop.setOnClickListener(this);
        mTipTv = findViewById(R.id.record_tip);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_in_java:
                mStartInNative.setClickable(false);
                mStartInJava.setClickable(false);
                mStop.setClickable(true);
                mTipTv.setText("java层 采集中");
                mStatus = 1;
                startAudioRecordInJava();
                break;
            case R.id.record_in_jni:
                mStartInNative.setClickable(false);
                mStartInJava.setClickable(false);
                mStop.setClickable(true);
                mTipTv.setText("native层 采集中");
                mStatus = 2;
                startAudioRecordInNative();
                break;
            case R.id.record_stop:
                mStartInNative.setClickable(true);
                mStartInJava.setClickable(true);
                mStop.setClickable(false);
                mTipTv.setText("点击录制声音");
                if (mStatus == 1)
                    stopAudioRecordInJava();
                else
                    stopAudioRecordInNative();
                mStatus = 0;
                break;
        }
    }

    private void startAudioRecordInNative() {
        record();
    }

    private void stopAudioRecordInNative() {
        stop();
    }

    private void startAudioRecordInJava() {
//        mAudioCapture = new AudioCapture(AudioCapture.AUDIO_CAPTURE_TYPE_OPENSLES, AudioCapture.AUDIO_SAMPLE_RATE_44_1,
//                AudioCapture.AUDIO_CHANNEL_MONO, AudioCapture.AUDIO_FORMAT_PCM_16BIT);
        mAudioCapture = new AudioCapture(AudioCapture.AUDIO_CAPTURE_TYPE_OPENSLES, AudioCapture.AUDIO_SAMPLE_RATE_44_1,
                AudioCapture.AUDIO_CHANNEL_MONO, AudioCapture.AUDIO_FORMAT_PCM_16BIT);
        if (mAudioCapture.getState() == AudioCapture.STATE_IDLE) {
            mAudioCapture.setAudioCaptureCallback(this);
            mAudioCapture.startRecording();
        }
        else {
            mAudioCapture.releaseRecording();
        }
    }
    private void stopAudioRecordInJava() {
        mAudioCapture.stopRecording();
        mAudioCapture.releaseRecording();
    }

    static {
        System.loadLibrary("native-lib");
    }

    public native void record();
    public native void stop();

    @Override
    public void onPCMDataAvailable(byte[] data, int size) {
        if (mOutputStream == null) {
            try {
                mFile = new File("sdcard/output_java.pcm");
                if (mFile.exists()) {
                    mFile.delete();
                }
                mOutputStream = new FileOutputStream(mFile);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            mOutputStream.write(data, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
