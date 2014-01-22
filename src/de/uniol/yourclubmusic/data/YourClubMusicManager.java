package de.uniol.yourclubmusic.data;

import java.util.HashMap;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;




import de.uniol.yourclubmusic.servlet.YourClubMusicWebSocket;

public class YourClubMusicManager {
	public static YourClubMusicManager instance=null;
	HashMap<YourClubMusicWebSocket, Station> mapClientStation;
	HashMap<String, Station> stations;
	int totalVotes;
	private YourClubMusicManager(){
		System.out.println("init YourClubMusicManager");	
		mapClientStation= new HashMap<YourClubMusicWebSocket,Station>();
		stations= new HashMap<String,Station>();
		stations.put("Amadeus", new Station("Amadeus", 53.143450, 8.214552,true));
	}
	
	public static YourClubMusicManager getInstance(){
		if(instance==null) instance= new YourClubMusicManager();
		return instance;
	}

	public void receiveMessageFromClient(YourClubMusicWebSocket yourClubMusicWebSocket, String msg) {
		String station=JsonObject.readFrom(msg).get("station").asString();
		System.out.println("Received message for station "+station);
		if(stations.containsKey(station)){
			//send message to station
			if(!mapClientStation.containsKey(yourClubMusicWebSocket)){
				mapClientStation.put(yourClubMusicWebSocket, stations.get(station));
				mapClientStation.get(yourClubMusicWebSocket).registerClient(yourClubMusicWebSocket);
			}
			mapClientStation.get(yourClubMusicWebSocket).receiveMessageFromClient(yourClubMusicWebSocket, msg);	
		}
	}

	public void unregisterClient(YourClubMusicWebSocket yourClubMusicWebSocket) {
		if(mapClientStation.containsKey(yourClubMusicWebSocket)){
			mapClientStation.get(yourClubMusicWebSocket).unregisterClient(yourClubMusicWebSocket);
			mapClientStation.remove(yourClubMusicWebSocket);
		}
		
	}

	public void clientConnected(YourClubMusicWebSocket yourClubMusicWebSocket) {
		System.out.println("sending list of stations to "+yourClubMusicWebSocket.getHostname());
		JsonObject object= new JsonObject();
		JsonArray array=new JsonArray();
		for (Station station : stations.values()) {
			if(station.isPublic()){
				array.add(new JsonObject().add("name", station.getName()));
			}
			
		}
		object.add("stations", array);
		yourClubMusicWebSocket.sendText(object.toString());
		
		
	}
	
}
