import java.io.*;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Scanner;

public class BibleMemory{
	private String username;				//describes the user of the program
	private ArrayList<Verse> verses;		//stores the user's verses
	private BibleMemoryData data;			//stores the data to the account, such as the number of verses
	private Calendar current;				//stores the current date and time

	public static void main(String[] args){
		BibleMemory account = new BibleMemory();	//Creates a new account
		int choice = -1;
		while(choice != 0){
			Scanner keyboard = new Scanner(System.in);
			printMenu();
			choice = Integer.parseInt(keyboard.nextLine());
			switch(choice){
				case 1:
					account.updateIsDue();
					account.reviewDueVerses();
					break;
				case 2:
					account.addVerse();
					break;
				case 3:
					account.deleteVerse();
					break;
				case 4:
					account.updateIsDue();
					account.printDueRefs();
					break;
				case 5:
					account.updateIsDue();
					account.printVerseList();
					break;
				case 6:
					account.earlyReview();
					break;
				case 7:
					account.extraReview();
					break;
				case 0:
					break;
				default:
					System.out.print("\nYour response is invalid. Please try again.\n");
			}
		}
		account.saveFile();
	}

	//Constructor: Creates a new BibleMemory object, either by loading data from a file or creating a new file
	public BibleMemory(){
		this.username = "joshua";					//Enters username automatically
		this.current = Calendar.getInstance();		//Updates the current date
		verses = new ArrayList<Verse>(0);
		//Attempts to load the file correlating to the username. If it cannot, creates a new file
		try{
			File verseFile = new File("joshua.dat");
			if(verseFile.exists()){
				FileInputStream fis = new FileInputStream(verseFile);
				ObjectInputStream ois = new ObjectInputStream(fis);

				data = (BibleMemoryData) ois.readObject();	//Inputs the file data, which stores the number of existing verses
				Verse nextVerse;
				//Loads a verse from the file into the current array list of verses
				for(int i = 0; i < data.getNum(); i++){
					nextVerse = (Verse) ois.readObject();
					verses.add(nextVerse);
				}

				System.out.print("\nSuccessfully loaded an account.\n");	//Prints message
			}


			//Creates a new account
			else{
				FileOutputStream fos = new FileOutputStream(verseFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos);

				//Creates a new data object and writes it to the account
				data = new BibleMemoryData();
				oos.writeObject(data);
				System.out.print("\nSuccessfully created a new account.\n");
			}
		}

		catch(IOException e){
			System.out.print(e);
			data.setNum(verses.size());		//Possible loading error; recalibrates the number of verses
			System.out.print("\nThe number of verses loaded incorrectly. It has been recalibrated to match the actual number. If this message shows up repeatedly, please contact Joshua Inman.");
		}

		catch(ClassNotFoundException e){
			System.out.print(e);
		}
	}

	//Adds a new verse to the Array List of verses
	public Verse addVerse(){
		Scanner keyboard = new Scanner(System.in);
		System.out.print("You have chosen to add a new verse. Please input the following info:\n");

		//User input for reference, verse and translation
		System.out.print("What is the verse reference?\n");
		String reference = keyboard.nextLine();
		System.out.print("\nPlease type the verse:\n");
		String verse = keyboard.nextLine();
		System.out.print("\nWhat translation did you use?\n");
		String trans = keyboard.nextLine();

		//User input for memory level (default: 0)
		System.out.print("\nHave you previously memorized this verse. Type 1 for 'yes' and 0 for 'no'.\n");
		int previous = Integer.parseInt(keyboard.nextLine());
		int memoryLevel;
		if(previous == 1){
			System.out.print("\nWhat memory level would you like to set this verse to? Memory increases by 1 each time you successfully review a verse.\n");
			memoryLevel = Integer.parseInt(keyboard.nextLine());
		}

		else{
			memoryLevel = 0;
		}
		//Sets the due date to the current date
		Calendar whenDue = Calendar.getInstance();

		//Creates a new verse, adds it to the list, updates the account data, and returns the new verse
		Verse newVerse = new Verse(reference, verse, trans, 0, whenDue, memoryLevel, true);
		if(newVerse.getMemoryLevel() == 0){
			double[] probs = {0.75, 0.5, 0.25};
			newVerse.memorize(probs);
		}
		verses.add(newVerse);
		data.incramentNum();
		return newVerse;
	}

	//Prints a list of verses
	public void printVerseList(){
		for(int i=0; i<verses.size(); i++){
			System.out.print("\n" + verses.get(i).getReference() + " - due in " + verses.get(i).getDaysUntilReview(current) + " days.");
		}
	}

	//Deletes a verse by matching the reference
	public Verse deleteVerse(){
		Scanner keyboard = new Scanner(System.in);
		System.out.print("\nWhat is the reference of the verse you would like to remove?");
		String reference = keyboard.nextLine();
		for(int i=0; i<verses.size(); i++){
			if(reference.equals(verses.get(i).getReference())){
				Verse deletedVerse = verses.remove(i);
				System.out.print("\nVerse successfully deleted.\n");
				return deletedVerse;
			}
		}

		System.out.print("\nThe verse you chose to delete is not in the database. Consider checking for a type and trying again.\n");
		return null;
	}

	//Saves the data and the verses to the file
	public void saveFile(){
		try{
			File verseFile = new File("joshua.dat");
			FileOutputStream fos = new FileOutputStream(verseFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(data);
			for(int i=0; i < verses.size(); i++){
				oos.writeObject(verses.get(i));
			}

			System.out.print("\nThe file has been saved.");
		}

		catch(IOException e){
			System.out.print("\nIO Exception.\n");
		}
	}

	//Updates whether the verse is due for every verse
	public void updateIsDue(){
		for(int i=0; i<verses.size(); i++){
			Calendar dueDate = verses.get(i).getWhenDue();
			if(current.after(dueDate)){
				verses.get(i).setIsDue(true);
			}
			else{
				verses.get(i).setIsDue(false);
			}
		}
	}

	//Review a verse
	public void reviewVerse(Verse memVerse){
		boolean success = memVerse.blindReview(new Scanner(System.in));
		if(success){
			memVerse.incramentMemoryLevel();
			memVerse.setDueDate();
		}

		else{
			memVerse.decramentMemoryLevel();
		}
	}

	//Reviews all verses that are due
	public boolean reviewDueVerses(){
		for(int i=0; i<verses.size(); i++){
			if(verses.get(i).getIsDue()){
				reviewVerse(verses.get(i));
				Scanner keyboard = new Scanner(System.in);
				System.out.print("\nWould you like to continue? Press 1 to continue and 0 to stop: ");
				int response = keyboard.nextInt();
				if(response == 0){
					return false;
				}
			}

		}
		return true;
	}

	public static void printMenu(){
		System.out.print("\nWhat would you like to do? Please enter the number next to the action you would like to perform:\n\t1. Review your due verses.\n\t2. Add a verse.\n\t3. Delete a verse.\n\t4. Print due verses.\n\t5. Print all verses and when they are due.\n\t6. Early review.\n\t7. Extra review.\n\t0. Save and quit.\n");
	}

	//Prints each reference and when it is due
	public void printDueRefs(){
		System.out.print("\nHere are your verses that are due: ");
		for(int i=0; i<verses.size(); i++){
			if(verses.get(i).getIsDue())
				System.out.print("\n" + verses.get(i).getReference());
		}
	}

	public Verse searchByRef(){
		Scanner keyboard = new Scanner(System.in);
		System.out.print("\nWhat is the reference of the verse you would like to review?");
		String memRef = keyboard.nextLine();
		for(int i=0; i<verses.size(); i++){
			if(verses.get(i).getReference().equals(memRef)){
				return verses.get(i);
			}
		}
		return null;
	}

	public void earlyReview(){
		Verse revVerse = this.searchByRef();
		this.reviewVerse(revVerse);
	}

	public void extraReview(){
		Verse revVerse = this.searchByRef();
		Scanner keyboard = new Scanner(System.in);
		revVerse.blindReview(keyboard);
	}
}