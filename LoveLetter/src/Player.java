
public class Player {
	private boolean handmaid = false;
	private boolean stillInRound = true;
	private String playerName;
	private int playerPoints;
	private Card[] playerHand = new Card[2];


	public  Player(String n) {
		playerName = n;
		playerPoints = 0;
		handmaid = false;
		stillInRound = true;

	}
	public void reset() {
		handmaid = false;
		stillInRound = true;
	}
	
	public String toString() {
		return playerName;
	}
	public void setPlayerName(String n) {
		playerName = n;
	}

	public void setPlayerPoints(String p) {
		playerName = p;
	}

	//index 0 will be player current hand, index 1 is new card
	public void setPlayerHand(Card n, int slot) {
		playerHand[slot] = n;
	}

	public void win() {
		playerPoints++;
	}


}
