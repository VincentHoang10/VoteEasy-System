/**
 * FileParser.java
 * @author Carlos Chasi-Mejia
 * FileParser class parses election file and retrieves needed data
 */

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.util.*;

public class FileParser {
    private String fileHeader;  //Voting Protocol of the election
    private int numberOfCandidates;  //Number of candidates in the election
    private String candidateLine;  //Name and Party of every candidate in the election
    private int numberOfSeats;  //Number of seats for the election
    private int numberOfBallots;  //Number of ballots in the election
    private ArrayList<String> ballotList;  //List of all the ballot's information in the election

    /** 
     * Constructs file parser object
     * @param filename Name of the election csv file
     */
    public FileParser(String filename){
        parseFile(filename);
    }

     /** 
     * Parses election file line by line and assigns data to associated variable 
     * @param filename Name of the election csv file
     * @throws FileNotFoundException if file is not in directory 
     */
    private void parseFile(String filename){
        try{
            //creates ArrayList to store every line in file
            ArrayList<String> fileLines = new ArrayList<>();
            File f = new File(filename);
            Scanner scanner = new Scanner(f);

            scanner.useDelimiter("\n");
            //scans every line and adds it to ArrayList
            while(scanner.hasNextLine()){
                fileLines.add(scanner.nextLine());
            }
            int size = fileLines.size();

            this.fileHeader = fileLines.get(0); //Set the file Header to first line
            this.numberOfCandidates = Integer.parseInt(fileLines.get(1)); //Set the number of candidates to second line
            this.candidateLine = fileLines.get(2); //Set the line of candidates to third line

            if (fileHeader.equals("IR")){
                this.numberOfBallots = Integer.parseInt(fileLines.get(3)); //Set the number of ballots to fourth line
                List<String> str = fileLines.subList(4, size); //Save list of ballots to every line starting from fifth line
                this.ballotList = new ArrayList<String>(str);  //Convert List to Array List and set ballot list to it
            }
            else{
                this.numberOfSeats = Integer.parseInt(fileLines.get(3)); //Set the number of seats to fourth line
                this.numberOfBallots = Integer.parseInt(fileLines.get(4)); //Set the number of ballots to fifth line
                List<String> str = fileLines.subList(5, size); //Save list of ballots to every line starting from sixth line
                this.ballotList = new ArrayList<String>(str);  //Convert List to Array List and set ballot list to it
            }
            scanner.close();
        }
        catch (FileNotFoundException f){
            System.out.println("[SYSTEM]: File not found.");
        }
    }

    /** 
    * Retrieves the voting protocol of the election
    * @return The voting protocol
    */
    public String getFileHeader(){
        return this.fileHeader;
    }

    /** 
    * Retrieves the number of candidates in the election
    * @return The number of candidates
    */
    public int getNumberOfCandidates(){
        return this.numberOfCandidates;
    }

    /** 
    * Retrieves all the candidates names and party of the election
    * @return The candiate line
    */  
    public String getCandidateLine(){
        return this.candidateLine;
    }

    /** 
    * Retrieves the number of ballots in the election
    * @return The number of ballots
    */
    public int getNumberOfBallots(){
        return this.numberOfBallots;
    }

    /** 
    * Retrieves the number of seats available in the election
    * @return The number of seats
    */
    public int getNumberOfSeats(){
        return this.numberOfSeats;
    }

    /** 
    * Retrieves all the ballots in the election in a form of a list
    * @return The list of ballot information
    */  
    public ArrayList<String> getBallotList(){
        return this.ballotList;
    }
}
