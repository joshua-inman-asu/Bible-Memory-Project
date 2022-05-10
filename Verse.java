import java.util.ArrayList;
import java.util.Scanner;
import java.util.Calendar;
import java.io.*;

public class Verse implements Serializable{
	private String reference;
	private String verse;
	private String translation;
	private int daysRemaining;
	private Calendar whenDue;
	private int memoryLevel;
	private boolean isDue;

	public Verse(String reference, String verse, String translation, int daysRemaining, Calendar whenDue, int memoryLevel, boolean isDue){
		this.reference = reference;
		this.verse = verse;
		this.translation = translation;
		this.daysRemaining = daysRemaining;
		this.whenDue = whenDue;
		this.memoryLevel = memoryLevel;
		this.isDue = isDue;
	}

	public String getReference(){
		return reference;
	}

	public String getVerse(){
		return verse;
	}

	public String getTranslation(){
		return translation;
	}

	public int getDaysRemaining(){
		return daysRemaining;
	}

	public Calendar getWhenDue(){
		return whenDue;
	}

	public int getMemoryLevel(){
		return memoryLevel;
	}

	public boolean getIsDue(){
		return isDue;
	}

	public int getDaysUntilReview(Calendar current){
		if(current.get(Calendar.YEAR) == whenDue.get(Calendar.YEAR)){
			return whenDue.get(Calendar.DAY_OF_YEAR) - current.get(Calendar.DAY_OF_YEAR);
		}

		else{
			return 365 - current.get(Calendar.DAY_OF_YEAR) + whenDue.get(Calendar.DAY_OF_YEAR);
		}
	}

	public void setVerse(String newVerse){
		verse = newVerse;
	}

	public void setRef(String newReference){
		reference = newReference;
	}

	public void setMemoryLevel(int level){
		memoryLevel = level;
	}

	public void incramentMemoryLevel(){
		memoryLevel += 1;
	}

	public void decramentMemoryLevel(){
		if(memoryLevel > 0)
			memoryLevel -= 1;
	}

	//Updates the due date after a successful review
	public void setDueDate(){
		whenDue = Calendar.getInstance();
		if(this.memoryLevel < 8){
			whenDue.add(Calendar.DAY_OF_MONTH, memoryLevel);
		}
		else if(this.memoryLevel < 11){
			int incramentAmount = (memoryLevel-6)*7; 				//Translates memory level into the number of weeks to incrament by
			whenDue.add(Calendar.DAY_OF_MONTH, incramentAmount);
		}
		else if(this.memoryLevel < 13){
			int incramentAmount = (memoryLevel-9);
			whenDue.add(Calendar.MONTH, incramentAmount);
		}
		else if(this.memoryLevel > 13){
			whenDue.add(Calendar.MONTH, 3);
		}
	}

	public void setIsDue(boolean newResult){
		isDue = newResult;
	}


	//Initial memory sequence
	public void memorize(double[] probs){
		Scanner keyboard = new Scanner(System.in);
		boolean success = false;
		while(!success)
			success = this.completeReview(keyboard);
		int i = 0;
		while(i < probs.length){
			success = false;
			while(!success)
				success = partialReview(probs[i], keyboard);
			i++;
		}
		success = false;
		while(!success)
			success = this.blindReview(keyboard);
	}

	//Changes a verse (given by the object) to a list of the first letter of every word
	private ArrayList<Character> parseVerseToChar(){
		int stringCnt = 0;
		ArrayList<Character> parsedVerse = new ArrayList<Character>();
		//Continues adding letters until the verse (string) is empty
		while(stringCnt < this.verse.length()){
			//Extracts first letter of word and adds it to the array list of characters
			parsedVerse.add(Character.toLowerCase(this.verse.charAt(stringCnt)));
			stringCnt++;
			//Skips until the next word
			while(stringCnt < this.verse.length() && !(this.verse.substring(stringCnt, stringCnt+1).equals(" "))){
				stringCnt++;

			}

			stringCnt++;	// Skips over the space that was found

			//Skips over open/closed quotations if they are present
			if(stringCnt < this.verse.length() && this.verse.substring(stringCnt, stringCnt+1).equals("\"")){
				stringCnt++;
			}
		}
		return parsedVerse;
	}

	//Changes the verse to an Array List of strings
	private ArrayList<String> parseVerseToString(){
		int wordStart = 0;
		int wordFinish = 0;
		ArrayList<String> parsedVerse = new ArrayList<String>();
		//Continues adding letters until the verse (string) is empty
		while(wordFinish < this.verse.length()){
			if(this.verse.substring(wordFinish, wordFinish+1).equals(" ")){
				parsedVerse.add(this.verse.substring(wordStart, wordFinish));
				wordFinish++;
				wordStart = wordFinish;
			}
			else
				wordFinish++;
		}
		parsedVerse.add(this.verse.substring(wordStart, wordFinish));
		return parsedVerse;
	}

	//Prints out the verse with an inputted proportion hidden
	private void printPartialVerse(ArrayList<String> verse, double proportion){
		//Loops through every word in the array list, and either prints it or prints an appropriate number of spaces
		for(int i = 0; i<verse.size(); i++){
			double rand = Math.random();		//Random number

			//If the random number is smaller than the proportion, prints the word
			if(rand < proportion)
				System.out.print(verse.get(i) + " ");

			//If the random number is larger, prints out an appropriate number of spaces
			else{
				for(int j=0; j<verse.get(i).length(); j++)
					System.out.print(" ");
			}
		}
	}

	//Changes the list of first letters with potential spaces to just a list of first letters (of a verse, inputted by user)
	private ArrayList<Character> parseUserVerse(String userVerse){
		ArrayList<Character> parsedVerse = new ArrayList<Character>();
		int i = 0;
		//While the user verse is not finished, add to the parsedVerse string unless it is a space
		while(i < userVerse.length()){
			while(i < userVerse.length() && !(userVerse.substring(i, i+1).equals(" "))){
				parsedVerse.add(Character.toLowerCase(userVerse.charAt(i)));
				i++;
			}
			i++;
		}
		return parsedVerse;
	}

	//Calculates the proportion of the verse that is accurately typed
	private double checkVerse(Scanner keyboard){
		System.out.print("\n");
		int[] contentAccuracy = this.checkContent(keyboard); //Data from the actual verse
		int[] referenceAccuracy = this.checkReference(keyboard);
		double accuracy = (double)(contentAccuracy[0]+referenceAccuracy[0])/(double)(contentAccuracy[1]+referenceAccuracy[1]);
		return accuracy;
	}

	//Gets input from the user and reports data on the user's performance
	private int[] checkContent(Scanner keyboard){
		//Creates an array list of the first character in every word and of every word in the array list
		ArrayList<Character> parsedVerseChar = this.parseVerseToChar();
		ArrayList<String> parsedVerseString = this.parseVerseToString();
		int verseLength = parsedVerseChar.size();
		//Statistics of user performance
		int numCorrect = 0;
		int totalNum = 0;

		//Loops through the verse as the user inputs the verse (may input first letter or word--only checks first letter)
		while(parsedVerseChar.size() > 0){
			try{
				char nextChar = (char)keyboard.nextLine().charAt(0); //Finds next character		//UPDATE
				//If the character is correct, it prints out the word normally
				if(Character.toLowerCase(nextChar) == (parsedVerseChar.get(0))){
					System.out.print(parsedVerseString.get(0) + " ");
					parsedVerseChar.remove(0);
					parsedVerseString.remove(0);
					numCorrect++;
				}
				//If the character is wrong, prints out the word in ALL CAPS
				else{
					System.out.print(parsedVerseString.get(0).toUpperCase()+ " ");
					parsedVerseChar.remove(0);
					parsedVerseString.remove(0);
				}
				totalNum++;
			}
			catch(StringIndexOutOfBoundsException e){
			}
		}
		//Returns data on the user's accuracy
		int[] accuracyData = {numCorrect, totalNum};
		return accuracyData;
	}

	//Inputs a reference from the user and returns the number of correct inputs compared to the number of total inputs
	private int[] checkReference(Scanner keyboard){
		//splits the reference info into appropriate pieces (Book name, chapter, verse)
		String[] referenceInfo = this.reference.split("[' ':-]");
		int numCorrect = 0;
		int totalNum = 0;

		//As the user types, compares the reference to the correct reference
		for(int i=0; i<referenceInfo.length; i++){
			//Inputs the next word from the user, only stores the first character
			char userChar = (char)keyboard.nextLine().charAt(0);					//UPDATE
			//If the first character of the reference is correct, prints out the word and incraments numCorrect
			if(Character.toLowerCase(userChar) == referenceInfo[i].toLowerCase().charAt(0)){
				System.out.print(referenceInfo[i] + " ");
				numCorrect++;
			}

			//If the first character is wrong, prints out the word capitalized
			else{
				System.out.print(referenceInfo[i].toUpperCase() + " ");
			}

			totalNum++;	//Incraments the total number of components
		}
		//Stores the data in an array and returns it
		int[] referenceData = {numCorrect, totalNum};
		return referenceData;
	}

	//Complete review: prints out the entire verse and reference and then has the user re-type it
	public boolean completeReview(Scanner keyboard){
		System.out.print("\nGuided review of " + this.reference + ":");
		System.out.print("\n" + this.verse + " " + this.reference + "\n");
		double proportionCorrect = this.checkVerse(keyboard);
		boolean success = printResults(proportionCorrect);
		return success;
	}

	//Partial review: prints out a certain proportion of the verse and reference, and then has the user type it out from there
	public boolean partialReview(double probs, Scanner keyboard){
		System.out.print("\nIntermediate review of " + this.reference + ":\n");
		ArrayList<String> verse = this.parseVerseToString();
		this.printPartialVerse(verse, probs);
		System.out.print("\n");
		double proportionCorrect = this.checkVerse(keyboard);
		boolean success = printResults(proportionCorrect);
		return success;
	}

	//A blind review is where the user types the entire verse without assistance
	public boolean blindReview(Scanner keyboard){
		System.out.print("\nBlind review of " + this.reference + ".\nPress any key to begin.");
		keyboard.nextLine();					//UPDATE
		for(int i=0; i<35; i++){
			System.out.print("\n");
		}
		System.out.print("Type the verse (first letter only):\n");
		double proportionCorrect = this.checkVerse(keyboard);
		boolean success = printResults(proportionCorrect);
		return success;
	}

	private boolean printResults(double prop){
		if(prop > 0.9){
			System.out.print("\nCongratulations! You finished the review with " + prop + " accuracy!");
			return true;
		}
		System.out.print("\nGood try! You finished the review with " + prop + " accuracy.");
		return false;
	}

	//toString returns a string with information about the verse
	public String toString(){
		return("Reference: " + this.getReference() +
							"\nVerse: " + this.getVerse() +
							"\nTranslation: " + this.getTranslation() +
							"\nDays Before Next Review: " + this.getDaysRemaining() +
							"\nDate of Next Reveiw: " + this.getWhenDue() +
							"\nMemory Level: " + this.getMemoryLevel() +
							"\nDue?: " + this.getIsDue());
	}

	public static void main(String[] args){
		Verse j19 = new Verse("Joshua 1:9", "Have I not commanded you? Be strong and courageous. Do not be frightened, and do not be dismayed, for the LORD your God is with you wherever you go.", "ESV", 0, Calendar.getInstance(), 0, true);
		System.out.print(j19);
		double[] probs = {0.75, 0.50, 0.25};
		j19.memorize(probs);
	}
}