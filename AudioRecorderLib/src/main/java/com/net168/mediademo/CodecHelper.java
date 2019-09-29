package com.net168.mediademo;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.provider.Settings;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by John on 7/3/2019.
 */

public class CodecHelper {
    private MediaCodec mEncodec,mDecodec;
    private MediaCodec.BufferInfo mEncodecInfo,mDecodecInfo;
    private int width,height;
    private Surface surface;

    private MuxerHelper muxerHelper;


    public CodecHelper(int width, int height, Surface surface) {
        this.width = width;
        this.height = height;
        this.surface = surface;
        muxerHelper = new MuxerHelper();
        initEncodec();
        initDecodec();
    }

    private void initEncodec(){
        try {
            mEncodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height);
            format.setInteger(MediaFormat.KEY_FRAME_RATE,30);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,10);
            format.setInteger(MediaFormat.KEY_BIT_RATE,2*1024*1024);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar); //此处是根据前面设置的camera format设置对应的
            mEncodecInfo = new MediaCodec.BufferInfo();
            mEncodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initDecodec(){
        try {
            mDecodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height);
            mDecodec.configure(format,surface,null,0);
            mDecodecInfo = new MediaCodec.BufferInfo();
            mDecodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void codec(byte[] data){
        encodec(data);
    }

    private void encodec(byte[] data){
        int inputIndex = mEncodec.dequeueInputBuffer(0);
        if (inputIndex >= 0){
            ByteBuffer inBuffer = mEncodec.getInputBuffer(inputIndex);
            inBuffer.clear();
            inBuffer.put(data);
            mEncodec.queueInputBuffer(inputIndex,0,data.length,System.nanoTime(),0);
        }
        int outputIndex = mEncodec.dequeueOutputBuffer(mEncodecInfo,0);

        if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            muxerHelper.startMuxer(mEncodec.getOutputFormat());
        }

        while (outputIndex>=0){
            ByteBuffer outBuffer = mEncodec.getOutputBuffer(outputIndex);
            byte[] out = new byte[mEncodecInfo.size];
            outBuffer.get(out);
            decodec(out);
            if (muxerHelper.isStart()){
                muxerHelper.writeData(outBuffer,mEncodecInfo);
            }
            mEncodec.releaseOutputBuffer(outputIndex,false);
            outputIndex = mEncodec.dequeueOutputBuffer(mEncodecInfo,0);
        }
    }


    private void decodec(byte[] data){
        int inputIndex = mDecodec.dequeueInputBuffer(0);
        if (inputIndex>=0){
            ByteBuffer inputBuffer = mDecodec.getInputBuffer(inputIndex);
            inputBuffer.clear();
            inputBuffer.put(data);
            mDecodec.queueInputBuffer(inputIndex,0,data.length, System.nanoTime(),0);
        }
        int outIndex = mDecodec.dequeueOutputBuffer(mDecodecInfo,0);
        while (outIndex>=0){
            mDecodec.releaseOutputBuffer(outIndex,true);
            outIndex = mDecodec.dequeueOutputBuffer(mDecodecInfo,0);
        }
    }

}
