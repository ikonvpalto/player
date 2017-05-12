package network;

import javax.sound.sampled.AudioFormat;

public class AudioInfo {

    private AudioFormat audioFormat;
    private long duration;
    private int chunksAmount;

    public int getChunksAmount() {
        return chunksAmount;
    }

    public void setChunksAmount(int chunksAmount) {
        this.chunksAmount = chunksAmount;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
