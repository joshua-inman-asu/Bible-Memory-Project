import java.io.*;

public class BibleMemoryData implements Serializable{
	private int numVerses;

	public BibleMemoryData(){
		numVerses = 0;
	}

	public int getNum(){
		return numVerses;
	}

	public void incramentNum(){
		numVerses += 1;
	}

	public void decreaseNum(){
		numVerses -= 1;
	}

	public void setNum(int newNum){
		numVerses = newNum;
	}

	public static void main(String[] args){
	}
}