import java.io.*;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;


/**
 * 
 * @author Andrew Saeyang
 *
 */
public class Game {
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException
	{
		int nPlayers = getNumberofPLayers(); //Asks the player for how many people are playing and validates
		Player[] players = playerList(nPlayers); //creates the array storing player data for game

		/*
		 * Main loop for the game structure. 
		 * This level will take care of re-shuffling of the deck, resetting player state to active, and burning cards
		 * before looping through it will check if any players have hit the win condition
		 */

		boolean gameState = true;
		boolean roundState = true;
		int turnMarker = 0;


		while(gameState == true) {

			if(roundState == false) {
				roundState = true;
				turnMarker = 0;	
				for (int i=0; i<nPlayers; i++) {
					players[i].reset();
				}

			}



			Stack<Card> gameDeck = DeckBuilder.buildDeck();	//Builds the deck

			Card burnCard = burnCard(gameDeck, nPlayers); // number of cards burned depends on the number of players

			for (int i=0; i<nPlayers; i++) {
				players[i].setPlayerHand(gameDeck.pop(), 0); //puts the top card of the deck into each players hand
			}

			/*
			 * this loop will be the the bulk of the playing 
			 * this level includes: drawing from the deck, playing cards and eliminating players in order to give player points
			 * before looping through it will check if there is at least 2 players playing
			 */

			while(roundState == true) {

				Scanner scanner = new Scanner(System.in);

				players[turnMarker].resetHandmaid();

				System.out.println("It's " + players[turnMarker].getName() + "'s turn.");
				System.out.println("");

				players[turnMarker].setPlayerHand(gameDeck.pop(), 1);

				players[turnMarker].printPlayerHand();
				System.out.println("");

				int choice = getCardChoice(countessCheck(players[turnMarker].getPlayerHand()), players[turnMarker]);


				//System.out.println("Player 2 has " + players[1].getPlayerHand()[0].getVal()); // for testing purpose

				playCard(players, turnMarker, choice-1, nPlayers, handmaidCheck(players, nPlayers), gameDeck, burnCard);

				if(lastPlayer(players)) {

					roundWin(players);

					roundState = false;

				}else if (gameDeck.isEmpty()) {

					findWinner(players);

					roundState = false;
				}

				turnMarker = nextTurn(turnMarker, players);

				System.out.println("=========================================");
				System.out.println("");

			}

			gameState = checkForWinner(players, nPlayers);

		}
	}

/** Checks to see if there is a winner for the game. Will output the winner and their score.
 * 
 * @param players
 * @param nPlayers
 * @return A boolean value representing if the game is over
 */
	private static boolean checkForWinner(Player[] players, int nPlayers) {

		boolean retVal = true;
		int win = 0;

		switch(nPlayers) {

		case 2:
			win = 7;
			break;
		case 3:
			win = 5;
			break;
		case 4:
			win = 4;
			break;
		}

		for(int i = 0; i <nPlayers; i++) {//TODO: will search entire array. not optimal?

			if(players[i].getPlayerPoints() == win) {

				System.out.println(players[i].getName() +" Wins the game with " + win + " points!");						

				retVal = false;

			}
		}

		System.out.println();
		System.out.println("=========================================");

		for(int j = 0; j < nPlayers; j++) {
			System.out.println(players[j].getName() + " had " + players[j].getPlayerPoints() + " points.");
		}
		System.out.println("=========================================");
		System.out.println();


		return retVal;

	}

/**Moves turn marker to the next player, if they are out, it moves the marker again.
 * 
 * @param turn The current player's turn.
 * @param players The array containing all the player info.
 * @return Which player turn it is.
 */
	public static int nextTurn(int turn, Player[] players){

		do {
			if(turn != players.length -1 ) {

				turn++;

			}else {

				turn = 0;

			}

		} while(players[turn].getRoundInfo() == false) ;

		return turn;
	}


	/** Asks for each players name.
	 * 
	 * @param n The number of players in the game.
	 * @return An array containing all the players' name filled out.
	 */
	public static Player[] playerList(int n) {
		Scanner name = new Scanner(System.in);
		Player[] p = new Player[n];
		for (int i=0; i< n; i++) {

			System.out.println("What is the name of player " + (i+1));
			//Ask for Player name
			p[i] = new Player(name.next());
		}

		return p;
	}

	/** Will burn one card from the deck and reveal three more cards if there are only two players
	 * 
	 * @param n The game deck that has already been shuffled
	 * @param p The number of players in the game
	 * @return A card that is burned from the deck
	 */
	public static Card burnCard(Stack<Card> n, int p) {

		Card retVal = n.pop();

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

		if(p == 2) {
			System.out.println("The three cards burned are: ");
			for (int i=0; i <3;i++) {
				System.out.println(n.pop().getName());

			}
		}else{
			System.out.println("burning one card.");
		}

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

		return retVal;
	}

	/**
	 * 
	 * @return The number of players that will play the game.
	 */
	public static int getNumberofPLayers() {
		int numPlayer = 0;
		Scanner numObj = new Scanner(System.in);
		do {
			System.out.println("How many players yo? Enter a number between 2-4.");

			try {
				numPlayer = numObj.nextInt();
				if(numPlayer <2 || numPlayer >4) {
					System.out.println("Not a valid number!!!");
				}
			}
			catch(InputMismatchException e){
				System.out.println("That wasnt a number yo...");
				numObj.next();
			}
		}while(!(numPlayer == 2 || numPlayer == 3 || numPlayer == 4)); {
			//numObj.close();
			return numPlayer;
		}	
	} 


	/** Gets the target of the active player's choice
	 * 
	 * @param p Int of the number of players in the game
	 * @param currentPlayer index of active player
	 * @param players All the players in the game
	 * @return Int of the index of the active player's target
	 */
	public static int getTarget(int p, int currentPlayer, Player[] players) { //FIXME: choose 0 for guard > error

		int t = 0;

		Scanner numObj1 = new Scanner(System.in);

		do {

			System.out.println("Which player would you like to target?");

			try {

				t = numObj1.nextInt();

				if(t-1 == currentPlayer) {

					System.out.println("You cannot target your self! Please Select a valid player.");

				}else if (t < 1 || t > p) {

					System.out.println("Not a valid number!!!");

				}else if(players[t-1].getRoundInfo() == false) {

					System.out.println("This player is already out of the round.");

				}else if(players[t-1].getHandmaid() == true){

					System.out.println("Cannot target this player. Handmaid is in effect until the beginning of thier turn.");

				}
			}

			catch(InputMismatchException e){
				System.out.println("That wasnt a number yo...");
				numObj1.next();
			}

		}while(t-1 == currentPlayer|| t < 1 || t > p ||
				players[t-1].getHandmaid() == true || players[t-1].getRoundInfo() == false); 
		//numObj.close();
		return t-1;

	} 

		
	/** Method that returns the index of which card the active player wishes to play
	 * 
	 * @param b Boolean of if the player has a Countess card and a King or Prince in their hand
	 * @param p The active player
	 * @return int value that is the index of the player's card choice
	 */
	public static int getCardChoice(boolean b, Player p) {
		int choice = 0;

		Scanner scanner = new Scanner(System.in);

		if(b) {

			do {
				System.out.println("Which card would you like to play?");
				System.out.println("Card 1 or 2?");

				try {
					choice = scanner.nextInt();

					if(choice <1 || choice >2) {

						System.out.println("Not a valid choice!!!");
						System.out.println("Please choose 1 or 2.");

					}else if(p.getPlayerHand()[choice-1].getVal() == 5 || p.getPlayerHand()[choice-1].getVal() == 6) {

						System.out.println("You cannot play this card while you have the Countess in your hand!");

					}

				}
				catch(InputMismatchException e){
					System.out.println("That wasnt a number yo...");
					scanner.next();
				}
			}while(choice < 1 || choice > 2 ||p.getPlayerHand()[choice-1].getVal() == 5 || p.getPlayerHand()[choice-1].getVal() == 6); 


		}else {

			do {
				System.out.println("Which card would you like to play?");
				System.out.println("Card 1 or 2?");

				try {
					choice = scanner.nextInt();
					if(!(choice == 2 || choice == 1)) {
						System.out.println("Not a valid choice!!!");
						System.out.println("Please choose 1 or 2.");
					}

				}
				catch(InputMismatchException e){
					System.out.println("That wasnt a number yo...");
					scanner.next();
				}
			}while(!(choice == 2 || choice == 1));

		}

		return choice;
	}

	/** Prints out what card is in each index
	 * 
	 * @param z Array of Cards
	 */
	public static void printDeckIndex (Card[] z){
		for(int i = 0; i < 16;i++){
			if(z[i]==null){
				System.out.println("index " + z +" is null.");

			}else{
				System.out.println("The card in index " + i + 
						" is " + z[i].getName() );
			}         
		}
	}


	/** Shuffles the game deck
	 * 
	 * @param z The array storing all the cards in the game
	 * @return an array of the all the cards shuffled and ready to play
	 */
	public static Card[] shuffleDeck (Card[] z){

		Random rand = new Random();
		Card temp = new Card();

		for (int i = 1 ; i <z.length; i++){

			int randomNum = rand.nextInt(z.length-i); // we  but we want a range from 0-14
			temp = z[randomNum];
			z[randomNum] = z[z.length-i];
			z[z.length-i] = temp;

		}
		
		return z;
	}

	
	/**Checks the number of Handmaids activated to determine if the turn needs to be skipped
	 * 
	 * @param p The array of Players currently in the game
	 * @param n The number of active Players remaining
	 * @return A boolean representing if there are no targets because everyone else has Handmaids active
	 */
	public static boolean handmaidCheck(Player[] p, int n) { 

		int maidCount = 0;
		int playersLeft = n;

		for (int i = 0; i < p.length ; i++) {
			if (p[i].getRoundInfo() == false) {
				playersLeft -= 1;
			}
		}

		for (int i = 0; i < n; i++) {

			if (p[i].getHandmaid() == true) {

				maidCount++;
			}
		}

		if(maidCount == playersLeft-1) {
			return true;
		}else {
			return false;
		}
	}

	/** Checks to see if the active player has the Countess as well as either the Prince or King in their hand
	 * 
	 * @param hand The active player's hand
	 * @return A boolean determining if the player is required to play the Countess
	 */
	public static boolean countessCheck(Card[] hand) {

		boolean retVal = false;

		if(hand[0].getVal() == 7 ||hand[1].getVal() == 7) {

			for(int i = 0; i <2; i++) {

				if(hand[i].getVal() == 5 || hand[i].getVal() == 6 ) {

					retVal = true;
				}
			}
		}

		return retVal;
	}

	/** Checks all the Players' stillInRound status 
	 * 
	 * @param players The array of Players in the game
	 * @return A boolean to determine if there are enough players to continue playing
	 */
	public static boolean lastPlayer(Player[] players) {
		boolean retVal = false;
		int out = 0;

		for(int i = 0; i< players.length; i++) {
			if(players[i].getRoundInfo()==false) {
				out++;
			}
		}

		if(out == players.length -1) {
			retVal = true;
		}

		return retVal;

	}

	
	/** Displays the winner of the round when all but one player is knocked out of the round
	 * 
	 * @param players The array of players in the game
	 */
	private static void roundWin(Player[] players) {

		for(int i = 0; i < players.length; i++) {

			if(players[i].getRoundInfo() == true) {

				System.out.println("*****************************************");
				System.out.println(players[i].getName() + " has won the round!" );
				players[i].win();
				System.out.println("*****************************************");

			}
		}
	}

	/** Finds the winner of the round between the remaining players left by comparing card values
	 * 
	 * @param players The array of players in the game
	 */
	private static void findWinner(Player[] players) {

		int temp = 0;

		//find out how many players left in the round
		for(int i = 0; i< players.length; i++) {

			if(players[i].getRoundInfo()==true) {

				temp++;
			}
		}

		int temp2 = 0;

		Player[] compare = new Player[temp];

		//put remaining players into an appropriate sized array
		for(int i = 0; i< temp; i++) {

			if(players[i].getRoundInfo()==true) {

				compare[temp2] = players[i];
				temp2++;

			}
		}

		Player winner = compare [0];

		//compare everyone left in the array 

		for(int i = 1; i < temp2; i++) {

			if(winner.getHandVal() < compare[i].getHandVal() ) {
				winner = compare[i];
			}
		}

		System.out.println("*****************************************");
		System.out.println(winner.getName() + " has won the round!" );
		winner.win();
		System.out.println("*****************************************");


		for(int i = 0; i < compare.length; i++) {

			System.out.println(compare[i].getName() + " had the " + compare[i].getPlayerHand()[0].getName());

		}
	}



	/** Plays the card of the active player's choice
	 * 
	 * @param players The array of players in the game
	 * @param t Int of which index is the current player
	 * @param c Int of the active players card choice in their hand
	 * @param nPlayers Int if players in the game
	 * @param h Boolean if the active player's turn must be skipped
	 * @param gameDeck Stack<Card> which is the deck used the play the game
	 * @param bc The burned card at the beginning of the game
	 * 
	 */
	private static void playCard(Player[] players, int t, int c, int nPlayers, boolean h, Stack<Card> gameDeck, Card bc) {


		Scanner numObj = new Scanner(System.in);

		int targetCardVal = 0;
		Player targetPlayer = new Player();
		/*
		 * problem with putting the shift here. If index 0 is chosen, index 1 will be the card selected because the shift will occur BEFORE card has been selected and used.
		 *if (c==0) {
		 *	players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed
		 *}
		 */

		switch(players[t].getPlayerHand()[c].getVal()) {

		case 8: // if you discard this card, you are out of the round

			System.out.println(players[t].getName() + " has discarded the Princess. They are out of the round."); 

			players[t].out();

			break;

		case 1: //Player designates another player and names a type of card. If that player's hand matches the type of card specified, that player is eliminated from the round. Guard cannot be named as the type of card.

			if(h == false) {

				targetPlayer = players[getTarget(nPlayers, t, players)];

				do {

					System.out.println("Which card do you think " + targetPlayer.getName() + " has? Pick a number between 2 and 8");

					try {
						targetCardVal = numObj.nextInt();

						if(targetCardVal == 1) {

							System.out.println("Cannot guess another Guard!!!");

						}else if(targetCardVal <2 || targetCardVal >8) {

							System.out.println("Not a valid number!!!");

						}
					}
					catch(InputMismatchException e){

						System.out.println("That wasnt a number yo...");
						numObj.next();
					}

				}while(targetCardVal < 2 || targetCardVal > 8 );

				//numObj.close();
				if (targetCardVal == targetPlayer.getPlayerHand()[0].getVal()) {

					System.out.println("That's correct!" + targetPlayer.getName() + " is out of the round!");
					targetPlayer.out();

				}else {
					System.out.println("Not Quite!");
				}

			}else{

				System.out.println("The are no valid targets. Skipping turn");

			}

			if (c==0) {

				players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed

			}

			System.out.println();
			break;

		case 2:

			if(h==false) {

				targetPlayer = players[getTarget(nPlayers, t, players)];

				if (c==0) {

					players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed
				}		

				System.out.println(targetPlayer.getName() + " has " + targetPlayer.getPlayerHand()[0].getName() + ".");

			}else {

				System.out.println("The are no valid targets. Skipping turn");

			}
			break;

		case 3: //You and another player secretly compare hands. The player with the lower value is out of the round

			if (c==0) {

				players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed

			}

			if(h==false) {

				targetPlayer = players[getTarget(nPlayers, t, players)];

				if (players[t].getPlayerHand()[0].getVal() > targetPlayer.getPlayerHand()[0].getVal()) {

					System.out.println(players[t].getName() + " has the better hand!");
					targetPlayer.out();

				}else if(players[t].getPlayerHand()[0].getVal() < targetPlayer.getPlayerHand()[0].getVal()) {

					System.out.println(targetPlayer.getName() + " has the better hand!");

					players[t].out();

				}else {

					System.out.println("It was a tie! *Le Gasp*");

				}

			}else {

				System.out.println("The are no valid targets. Skipping turn");
			}

			break;

		case 4://Until your next turn, ignore all effects from other player's cards

			players[t].handmaid();

			if (c==0) {

				players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed

			}

			System.out.println(players[t].getName() + " has played Handmaid. They cannot be targeted until the beginning of thier next turn.");

			break;

		case 5://Choose any player (including yourself) to discard his or her hand and draw a new card 

			int temp = 0;

			Scanner numObj1 = new Scanner(System.in);

			do {

				System.out.println("Which player should discard thier hand?");

				try {

					temp = numObj1.nextInt();

					if (temp < 1 || temp > nPlayers) {

						System.out.println("Not a valid number!!!");

					}else if(players[temp-1].getRoundInfo() == false) {

						System.out.println("This player is already out of the round.");

					}else if(players[temp-1].getHandmaid() == true){

						System.out.println("Cannot target this player. Handmaid is in effect until the beginning of thier turn.");

					}

				}

				catch(InputMismatchException e){

					System.out.println("That wasnt a number yo...");

					numObj1.next();
				}

			}while(temp < 1 || temp > nPlayers || players[temp-1].getRoundInfo() == false
					|| players[temp-1].getHandmaid() == true); 

			if (c==0) {

				players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed

			}

			//System.out.println("Card before discard is: " + players[t].getPlayerHand()[0].getName() );

			// if the target player is discarding a princess, they are out of the round, otherwise, card is discarded and new hand is drawn.

			temp--; // ie. player 2 is index 1

			if(players[temp].getPlayerHand()[0].getVal()==8) {

				System.out.println(players[temp].getName() + " has discarded the Princess! They are out of the round!");

				players[temp].out();

			}else if(gameDeck.isEmpty()) {

				players[temp].getPlayerHand()[0] = bc;

			}else{

				players[temp].getPlayerHand()[0] = gameDeck.pop();
			}

			break;

		case 6://Trade hands with another player of your choice

			if(h==false) {
				targetPlayer = players[getTarget(nPlayers, t, players)];

				if (c==0) {
					players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed
				}

				Card tCard = players[t].getPlayerHand()[0];

				players[t].getPlayerHand()[0] = targetPlayer.getPlayerHand()[0] ;

				targetPlayer.getPlayerHand()[0] = tCard;

				System.out.println("You now have " + players[t].getPlayerHand()[0].getName() );

			}else {

				System.out.println("The are no valid targets. Skipping turn");
			}
			break;

		case 7://If you have this card and the King or Prince is in your hand, you must discard this card

			if (c==0) {
				players[t].getPlayerHand()[0] = players[t].getPlayerHand()[1]; // Cards are always dealt to [1] so it just shifts it over if needed
			}

			break;

		}

		System.out.println();

	}

}
