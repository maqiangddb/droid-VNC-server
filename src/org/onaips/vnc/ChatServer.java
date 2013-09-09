package org.onaips.vnc;

import com.yongboy.socketio.MainServer;

/**
 * Created by mqddb on 13-8-14.
 */
public class ChatServer {
    private static final int PORT = 9000;

    public static void startWebServer()
    {

        MainServer chatServer = new MainServer(new DemoChatHandler(),
                PORT);
        chatServer.start();

    }

}
