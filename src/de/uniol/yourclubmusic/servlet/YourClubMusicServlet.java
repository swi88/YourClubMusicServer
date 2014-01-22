package de.uniol.yourclubmusic.servlet;

import javax.servlet.annotation.WebServlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;


@WebServlet("/YourClubMusicServlet")
public class YourClubMusicServlet extends WebSocketServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2501049084607787230L;

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.register(YourClubMusicWebSocket.class);
		
	}

}
