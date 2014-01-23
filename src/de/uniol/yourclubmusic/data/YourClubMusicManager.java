package de.uniol.yourclubmusic.data;

import java.util.HashMap;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;




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
		stations.put("Amadeus", new Station("Amadeus", 53.143450, 8.214552,true,50));
		stations.put("Fun Factory", new Station("Fun Factory", 52.891579, 8.425859,true,1000));
	}
	
	public static YourClubMusicManager getInstance(){
		if(instance==null) instance= new YourClubMusicManager();
		return instance;
	}

	public void receiveMessageFromClient(YourClubMusicWebSocket yourClubMusicWebSocket, String msg) {
		JsonObject mainObject=JsonObject.readFrom(msg);
		if(mainObject.get("station")!=null){
			String station=mainObject.get("station").asString();
			System.out.println("Received message for station "+station);
			if(stations.containsKey(station)){
				//send message to station
				if(!mapClientStation.containsKey(yourClubMusicWebSocket)){
					mapClientStation.put(yourClubMusicWebSocket, stations.get(station));
					mapClientStation.get(yourClubMusicWebSocket).registerClient(yourClubMusicWebSocket);
				}
				mapClientStation.get(yourClubMusicWebSocket).receiveMessageFromClient(yourClubMusicWebSocket, msg);	
			}
		}else if(mainObject.get("location")!=null){
			//send list of clubs to client
			if(!mapClientStation.containsKey(yourClubMusicWebSocket)){
				JsonObject object= new JsonObject();
				JsonArray array=new JsonArray();
				JsonValue jsonLocation= mainObject.get("location");
				for (Station station : stations.values()) {
					if(station.isPublic() && station.isNeighbourhood(jsonLocation)){
						array.add(new JsonObject().add("name", station.getName()));
					}
					
				}
				object.add("stations", array);
				yourClubMusicWebSocket.sendText(object.toString());
				
			}else mapClientStation.get(yourClubMusicWebSocket).receiveMessageFromClient(yourClubMusicWebSocket, msg);
			
			}
		
	}

	public void unregisterClient(YourClubMusicWebSocket yourClubMusicWebSocket) {
		if(mapClientStation.containsKey(yourClubMusicWebSocket)){
			mapClientStation.get(yourClubMusicWebSocket).unregisterClient(yourClubMusicWebSocket);
			mapClientStation.remove(yourClubMusicWebSocket);
		}
		
	}
	
}
