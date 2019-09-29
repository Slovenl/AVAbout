package com.net168.mediademo;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by John on 7/3/2019.
 */

public class MuxerHelper {
    private MediaMuxer muxer;
    private boolean isStart = false;
    private int videoTrack;
    private File file;

    public MuxerHelper(){
        initMuxer();
    }

    private void initMuxer(){
        file = new File("/sdcard/muxer.mp4");
        if (file.exists()){
            file.delete();
        }
        try {
            muxer = new MediaMuxer("/sdcard/muxer.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
        e.printStackTrace();
        }
        }

    public void startMuxer(MediaFormat format){
        videoTrack = muxer.addTrack(format);
        muxer.start();
        isStart = true;
    }

    public boolean isStart() {
        return isStart;
    }

    public void writeData(ByteBuffer buffer, MediaCodec.BufferInfo info){
        if (isStart){
            info.presentationTimeUs = System.nanoTime()/1000;
            muxer.writeSampleData(videoTrack,buffer,info);
            if (file.length()>20*1024*1024){    //如果大于20M，就停止合成。
                muxer.stop();
            }
        }

    }
}
