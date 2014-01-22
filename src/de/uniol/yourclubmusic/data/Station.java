package de.uniol.yourclubmusic.data;

import java.util.ArrayList;
import java.util.HashMap;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.uniol.yourclubmusic.servlet.YourClubMusicWebSocket;

public class Station {
	HashMap<String, Genre> genres;
	HashMap<YourClubMusicWebSocket, ArrayList<String> > mapClientGenre;
	int totalVotes;
	
	String name;
	double latitude;
	double longitude;
	boolean isPublic;
	/**
	 * create a new station
	 * @param name name of the station
	 * @param latitude lat
	 * @param longitude lon
	 * @param isPublic is shown in the list of the client
	 */
	public Station(String name,double latitude,double longitude,boolean isPublic) {
		genres= new HashMap<>();
		mapClientGenre= new HashMap<>();
		totalVotes=0;
		this.isPublic=isPublic;
		this.latitude=latitude;
		this.longitude=longitude;
		this.name=name;
	}
	public String getName(){
		return name;
	}
	public double getLongitude(){
		return longitude;
	}
	public double getLatitude(){
		return latitude;
	}
	public boolean isPublic(){
		return isPublic;
	}
	
	public synchronized void registerClient(YourClubMusicWebSocket socket){
		if(!mapClientGenre.containsKey(socket)){
			mapClientGenre.put(socket, new ArrayList<String>());
			updateClients();
		}
	}
	public synchronized void unregisterClient(YourClubMusicWebSocket socket){
		for (String genre : mapClientGenre.get(socket)) {
			Genre g=genres.get(genre);
			g.decrementVotings();
			if(g.getVotings()==0) genres.remove(genre);
			
		}
		mapClientGenre.remove(socket);
		updateClients();
		
	}
	public synchronized void receiveMessageFromClient(YourClubMusicWebSocket socket,String msg) {
		System.out.println("Message from client:"+msg);
		//JsonArray jsonArray = JsonArray.readFrom( msg );
		JsonObject object= new JsonObject().readFrom(msg);
		//get genres
		JsonValue jsonGenres= object.get("genres");
		if(jsonGenres!=null && jsonGenres.isArray()){
			receiveGenres(socket,jsonGenres);
			}
		JsonValue jsonLocation= object.get("location");
		if(jsonLocation!=null && jsonLocation.isArray()){
			receiveLocation(socket,jsonLocation);
			}
		}

	private synchronized void receiveLocation(YourClubMusicWebSocket socket,
			JsonValue jsonLocation) {
		double latitude=jsonLocation.asArray().get(0).asObject().get("latitude").asDouble();
		double longitude=jsonLocation.asArray().get(1).asObject().get("longitude").asDouble();
		System.out.println(latitude+" "+longitude);
		
		
	updateClients();
		
	}
	private synchronized void receiveGenres(YourClubMusicWebSocket socket, JsonValue jsonGenres) {
		ArrayList<String> genresNew= new ArrayList<>();
		for (JsonValue jsonValue : (JsonArray)jsonGenres) {
			if(jsonValue.isObject()){
				String genre=jsonValue.asObject().get("genre").asString();
				genresNew.add(genre);
				if(!mapClientGenre.get(socket).contains(genre)){
					//add genre
					if(genres.containsKey(genre))
						genres.get(genre).incrementVotings();
					else genres.put(genre, new Genre(genre));
				}
			}
		}totalVotes+=genresNew.size();
		//remove unvoted
		ArrayList<String> unvoted= (ArrayList<String>) mapClientGenre.get(socket).clone();
		unvoted.removeAll(genresNew);
		//decrement
		totalVotes-=unvoted.size();
		for (String genre : unvoted) {
			Genre g=genres.get(genre);
			g.decrementVotings();
			if(g.getVotings()==0) genres.remove(genre);
			genresNew.remove(genre);
		}
		mapClientGenre.put(socket, genresNew);
	updateClients();
	}
	private void updateClients(){
		//create json file
		System.out.println("update clients");
		JsonObject object= new JsonObject();
		JsonArray array=new JsonArray();
		for (Genre genre : genres.values()) {
			JsonObject currentGenre= new JsonObject();
			currentGenre.add("name", genre.getName());
			currentGenre.add("votings", genre.getVotings());
			array.add(new JsonObject().add("genre", currentGenre));
		}
		object.add("genres", array);
		object.add("users", mapClientGenre.size());
		object.add("totalVotings", totalVotes);
		for (YourClubMusicWebSocket socket :mapClientGenre.keySet()) {
			socket.sendText(object.toString());
		}
	}
}