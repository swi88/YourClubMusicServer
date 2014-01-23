package de.uniol.yourclubmusic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import de.uniol.yourclubmusic.servlet.YourClubMusicWebSocket;

public class WebSocketServer {
	    private Server server;
	    private static int port=1988;
	    private List<Handler> webSocketHandlerList = new ArrayList<>();

	    public static void main(String[] args) throws Exception {
	        WebSocketServer webSocketServer = new WebSocketServer();
	        webSocketServer.addWebSocket(YourClubMusicWebSocket.class, "/");
	        webSocketServer.initialize();
	        webSocketServer.start();
	    }

	    public void initialize() {
	        server = new Server(port);
	        // connector configuration
	        // handler configuration
	        HandlerCollection handlerCollection = new HandlerCollection();
	        handlerCollection.setHandlers(webSocketHandlerList.toArray(new Handler[0]));
	        server.setHandler(handlerCollection);
	    }

	    public void addWebSocket(final Class<?> webSocket, String pathSpec) {
	        WebSocketHandler wsHandler = new WebSocketHandler() {
	            @Override
	            public void configure(WebSocketServletFactory webSocketServletFactory) {
	                webSocketServletFactory.register(webSocket);
	            }
	        };
	        ContextHandler wsContextHandler = new ContextHandler();
	        wsContextHandler.setHandler(wsHandler);
	        wsContextHandler.setContextPath(pathSpec); 
	        webSocketHandlerList.add(wsHandler);
	    }

	    public void start() throws Exception {
	        server.start();
	        server.join();
	    }
	    public void stop() throws Exception {
	        server.stop();
	        server.join();
	    }

	}
