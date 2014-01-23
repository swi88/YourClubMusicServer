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
	int neighbourhoodMeter;
	/**
	 * create a new station
	 * @param name name of the station
	 * @param latitude lat
	 * @param longitude lon
	 * @param isPublic is shown in the list of the client
	 * @param neighbourhoodMeter radius, there the station is available
	 */
	public Station(String name,double latitude,double longitude,boolean isPublic,int neighbourhoodMeter) {
		genres= new HashMap<>();
		mapClientGenre= new HashMap<>();
		totalVotes=0;
		this.isPublic=isPublic;
		this.latitude=latitude;
		this.longitude=longitude;
		this.name=name;
		this.neighbourhoodMeter=neighbourhoodMeter;
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
	public synchronized void unregisterClient(YourClubMusicWebSocket socket,boolean isConnected){
		//allready unregistered (from neighborhood kickout)
		if(!mapClientGenre.containsKey(socket)) return;
		if(!mapClientGenre.get(socket).isEmpty()){
			for (String genre : mapClientGenre.get(socket)) {
				Genre g=genres.get(genre);
				g.decrementVotings();
				if(g.getVotings()==0) genres.remove(genre);
				
			}
		}
		//decremt total votes
		totalVotes-=mapClientGenre.get(socket).size();
		mapClientGenre.remove(socket);
		if(!isConnected)
			socket.disconnect(1000, "Club verlassen");
		updateClients();
		
	}
	public synchronized void receiveMessageFromClient(YourClubMusicWebSocket socket,String msg) {
		System.out.println("Message from client:"+msg);
		//JsonArray jsonArray = JsonArray.readFrom( msg );
		JsonObject object= new JsonObject().readFrom(msg);
		JsonValue jsonLocation= object.get("location");
		if(jsonLocation!=null && jsonLocation.isArray()){
			receiveLocation(socket,jsonLocation);
			}
		//client kicked out by out of neighbourhood
		if(!mapClientGenre.containsKey(socket)) return;
		//get genres
		JsonValue jsonGenres= object.get("genres");
		if(jsonGenres!=null && jsonGenres.isArray()){
			receiveGenres(socket,jsonGenres);
			}
		}

	private synchronized void receiveLocation(YourClubMusicWebSocket socket,
			JsonValue jsonLocation) {
		if(!isNeighbourhood(jsonLocation)){
			unregisterClient(socket,false);
		}
		
	}
	private synchronized void receiveGenres(YourClubMusicWebSocket socket, JsonValue jsonGenres) {
		totalVotes-= mapClientGenre.get(socket).size();
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
	private static final int earthRadius = 6371;
	public boolean isNeighbourhood(JsonValue jsonLocation) {
			double latNew=jsonLocation.asArray().get(0).asObject().get("latitude").asDouble();
			double lonNew=jsonLocation.asArray().get(1).asObject().get("longitude").asDouble();
			//for clients who don't allow network/GPS locations
			if(latNew==0 && lonNew==0){
				return true;
			}
				
		    double dLat = (float) Math.toRadians(latNew - latitude);
		    double dLon = (float) Math.toRadians(lonNew - longitude);
		    double a =(double) (Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latitude))
		                        * Math.cos(Math.toRadians(latNew)) * Math.sin(dLon / 2) * Math.sin(dLon / 2));
		    double c = (double) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
		    double d = earthRadius * c;
		    d*=1000;
		    System.out.println(d);
		    return d<=neighbourhoodMeter?true:false;
	}
}