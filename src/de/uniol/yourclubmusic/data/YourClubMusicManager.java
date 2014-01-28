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
		stations.put("Charlys Musikkneipe", new Station("Charlys Musikkneipe", 53.142670, 8.212720,true,100000));
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
				//is client connected to another station?
				if(mapClientStation.containsKey(yourClubMusicWebSocket) &&!mapClientStation.get(yourClubMusicWebSocket).getName().equals(station)){
					System.out.println("unregister station");
					mapClientStation.get(yourClubMusicWebSocket).unregisterClient(yourClubMusicWebSocket,true);
					mapClientStation.put(yourClubMusicWebSocket, stations.get(station));
					mapClientStation.get(yourClubMusicWebSocket).registerClient(yourClubMusicWebSocket);
				}
				//send message to station
				if(!mapClientStation.containsKey(yourClubMusicWebSocket)){
					System.out.println("put");
					mapClientStation.put(yourClubMusicWebSocket, stations.get(station));
					mapClientStation.get(yourClubMusicWebSocket).registerClient(yourClubMusicWebSocket);
				}
				System.out.println(station+" "+mapClientStation.get(yourClubMusicWebSocket).getName());
				mapClientStation.get(yourClubMusicWebSocket).receiveMessageFromClient(yourClubMusicWebSocket, msg);	
			}
		}else if(mainObject.get("location")!=null&& mainObject.get("stationRequest")!=null){
			//send list of clubs to client
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
				
			}else if(mainObject.get("keepAlive")!=null){
				System.out.println("get keep alive from "+yourClubMusicWebSocket.getHostname()+", which is connected to "+ mapClientStation.get(yourClubMusicWebSocket).getName());
			}else if(mapClientStation.containsKey(yourClubMusicWebSocket))
				mapClientStation.get(yourClubMusicWebSocket).receiveMessageFromClient(yourClubMusicWebSocket, msg);
	}

	public void unregisterClient(YourClubMusicWebSocket yourClubMusicWebSocket) {
		if(mapClientStation.containsKey(yourClubMusicWebSocket)){
			mapClientStation.get(yourClubMusicWebSocket).unregisterClient(yourClubMusicWebSocket,false);
			mapClientStation.remove(yourClubMusicWebSocket);
		}
		
	}
	
}
