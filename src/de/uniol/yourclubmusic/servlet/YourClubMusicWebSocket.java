package de.uniol.yourclubmusic.servlet;


import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import de.uniol.yourclubmusic.data.YourClubMusicManager;

 @WebSocket
public class YourClubMusicWebSocket {
	  public Session session=null;
	 @OnWebSocketConnect
	    public void onConnect(Session session){
		 	this.session=session;
	        System.out.println("MyWebSocket.onConnect()"+ session.getRemoteAddress().getHostName());
	        YourClubMusicManager.getInstance().registerClient(this);
	    }

	    @OnWebSocketMessage
	    public void onText(String msg) {
	        YourClubMusicManager.getInstance().receiveMessageFromClient(this, msg);
	    }
	    
	    @OnWebSocketClose
	    public void onClose(int statusCode, String reason) {
	        System.out.println("MyWebSocket.onClose()"+reason);
	        YourClubMusicManager.getInstance().unregisterClient(this);
	    } 	    
	    public void sendText(String msg){
	    	try {
				session.getRemote().sendString(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
}