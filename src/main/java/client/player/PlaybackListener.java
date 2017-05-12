package client.player;

public interface PlaybackListener {

    void update(EventType type, float playbackPos);

    enum EventType {
        PB_PLAY,
        PB_PLAY_START,
        PB_PLAY_OFF,
        PB_PLAY_STOP,
        PB_PLAY_PAUSE,
        PB_PLAY_RESUME
    }

}
