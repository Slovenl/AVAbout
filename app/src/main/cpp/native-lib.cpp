#include <jni.h>
#include <stdio.h>
#include "audiocapture.h"

class AudioInstance {
public:
    AudioInstance();
    void start();
    void stop();
    void read(int8_t *data, uint32_t size);
private:
    AudioCapture *capture;
    FILE *file;
};

static AudioInstance *instance;

extern "C" JNIEXPORT void
JNICALL
Java_com_net168_audiorecorddemo_MainActivity_record(JNIEnv *env,jobject /* this */) {
    instance = new AudioInstance();
    instance->start();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_net168_audiorecorddemo_MainActivity_stop(JNIEnv *env,jobject /* this */) {
    if (instance)
    {
        instance->stop();
        delete instance;
        instance = nullptr;
    }
}

void static readData(int8_t *data, uint32_t size, void *ctx)
{
    AudioInstance *audio = static_cast<AudioInstance *>(ctx);
    audio->read(data, size);
}

AudioInstance::AudioInstance()
: capture(nullptr)
, file(nullptr)
{

}

void AudioInstance::read(int8_t *data, uint32_t size)
{
    if (file == nullptr)
    {
        file = fopen("sdcard/output_jni.pcm", "wb");
    }
    fwrite(data, 1, size, file);
}

void AudioInstance::start()
{
    capture = new AudioCapture(AUDIO_CAPTURE_TYPE_OPENSLES, AUDIO_SAMPLE_RATE_44_1, AUDIO_CHANNEL_MONO, AUDIO_FORMAT_PCM_16BIT);
    if (capture->getState() == STATE_IDEL)
    {
        capture->setAudioCaptureCallback(readData, this);
        capture->startRecording();
    }
    else
    {
        capture->releaseRecording();
        delete capture;
        capture = nullptr;
    }
}

void AudioInstance::stop()
{
    if (capture)
    {
        capture->stopRecording();
        capture->releaseRecording();
        delete capture;
        capture = nullptr;
    }
}