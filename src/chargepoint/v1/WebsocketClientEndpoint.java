/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chargepoint.v1;

/**
 *
 * @author xcstupe1
 */
import static chargepoint.v1.DisplayControls.LogTextArea;
import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.tyrus.core.frame.PongFrame;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class WebsocketClientEndpoint {

    Session userSession = null;
    private MessageHandler messageHandler;
 
    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
        DisplayControls.LogTextArea.append("INFO: Opening websocket "+userSession.getId()+"\n");
        DisplayControls.ServerConnection.setBackground(Color.green);
       
         //System.out.println("poll"); // called once
         //ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
         //ScheduledFuture<?> files = executorService.scheduleAtFixedRate(() -> {
            //try {
            //    String ipAddress = "8.8.8.8";
            //    InetAddress inet = InetAddress.getByName(ipAddress);

            //    if (inet.isReachable(500)){
            //        System.out.println("Host is reachable");
            //    }else{
            //        System.out.println("Host is NOT reachable");
            //        CloseReason CR = null;
            //        System.err.println("Closing by HOST");
            //        onClose(userSession, CR); 
            //    };

            //} catch (Exception e) {
            //    CloseReason CR = null;
            //    try {
            //        System.err.println("Closing by Exception 1");
            //        onClose(userSession, CR);
            //    } catch (InterruptedException ex) {
            //        System.err.println("Closing by Exception 2");
            //    }
            //}
        //},
        //1, 10, TimeUnit.SECONDS);
    }


    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    
    
    @OnClose
    public void onClose(Session userSession, CloseReason reason) throws InterruptedException {
        System.out.println("closing websocket "+userSession.getId()+"\n");
        DisplayControls.LogTextArea.append("INFO: Closing websocket "+userSession.getId()+"\n");
        DisplayControls.ServerConnection.setBackground(Color.red);
        DisplayControls.StartConnection();
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        String Received = "";
        if (this.messageHandler != null) {
            Received = this.messageHandler.handleMessage(message);
        }
        //return Received;
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {

        public String handleMessage(String message);
    }
}
