package client.object_pool;

import client.player.*;
import client.player.impl.MusicPlayerImpl;
import client.web.WebInterface;
import client.web.impl.LocalhostInterface;

import java.util.logging.Logger;

public class ObjectPool {

    private static ObjectPool pool;

    public static ObjectPool getPool() {
        if (null == pool)
            pool = new ObjectPool();
        return pool;
    }

    private MusicPlayerImpl musicPlayer;
    private LocalhostInterface webInterface;
    private Logger logger;

    private ObjectPool() {}

    public MusicPlayer getMusicPlayer() {
        if (null == musicPlayer)
            musicPlayer = new MusicPlayerImpl();
        return musicPlayer;
    }

    public Logger getLogger() {
        if (null == logger)
            logger = Logger.getGlobal();
        return logger;
    }

    public WebInterface getWebInterface() {
        if (null == webInterface)
            webInterface = new LocalhostInterface();
        return webInterface;
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

}
