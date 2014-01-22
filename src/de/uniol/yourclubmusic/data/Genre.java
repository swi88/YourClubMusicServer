package de.uniol.yourclubmusic.data;


public class Genre {
	private String name;
	private int votedByUsers;
	
	public Genre(String name){
		this.name=name;
		this.votedByUsers=1;
	}
	public String getName(){
		return name;
	}
	public int getVotings(){
		return votedByUsers;
	}
	public synchronized void incrementVotings(){
		votedByUsers++;
	}
	public synchronized void decrementVotings(){
		votedByUsers--;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.name.equals(((Genre) obj).getName());
	}
}
