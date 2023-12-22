/**
 * VoteEasy.java
 * @author Jashwin Acharya (achar061)
 * This class houses the main function and is responsible for calling helper functions and other classes for getting user input,
 * parsing the input CSV file, performing ballot calculations, and displaying winner information on the screen.
 */

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class VoteEasy{
    /**
     * The main entry point of the VoteEasy system.
     * @param args - Command line arguments passed to the VoteEasy system.
     */
    public static void main(String[] args){
        // Build Command Line Interface
        buildCLI();

        // Prompt user for CSV file name
        final String fileName = receiveAndValidateUserPrompt();

        // Parse the file
        final FileParser file = new FileParser(fileName);

        // Retrieve pertinent information
        final String fileHeader = file.getFileHeader();
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final String auditFilePath = "./audit_file.txt";

        if (fileHeader.equals("IR")){
            System.out.println("\n[SYSTEM]: Voting protocol chosen is Instant Runoff (IR)");
            final IRVoting ir = new IRVoting(candidateLine, ballotList, auditFilePath);
            ir.calculateBallots();
            displayIRWinner(ir.getWinningCandidate(), ir.getCandidates(), ir.getBallots().size());
        } else if (fileHeader.equals("OPL")) {
            System.out.println("\n[SYSTEM]: Voting protocol chosen is Open Party List (OPL)");
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();
            final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, auditFilePath);
            opl.performSeatAllocations();
            displayOPLWinner(opl.getWinningParty(), opl.getWinningCandidate(), opl.getParties(), numBallots, numSeats);
        } else if (fileHeader.equals("MPO")){
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();
            final MPOVoting mpo = new MPOVoting(candidateLine, ballotList, numSeats, auditFilePath);
            mpo.performSeatAllocations();
            displayMPOWinners(mpo.getWinningCandidates(), mpo.getCandidates(), numBallots, numSeats);
        }
    }

    /**
     * This system prints the necessary statements for the VoteEasy UI.
     */
    private static void buildCLI(){
        System.out.println("[SYSTEM]: Welcome to the VoteEasy System!");
        System.out.println("[SYSTEM]: You simply have to enter the name of the CSV file containing ballots and candidate information");
        System.out.println("[SYSTEM]: Type \"help\" (not case sensitive) if you need guidance on what file types are allowed.");
    }

    /**
     * This function is responsible for prompting a user for entering a CSV file and validating the name. If an invalid file name is
     * entered or if the file is missing from the "src" folder, the user is prompted indefinitely until the correct file name is 
     * entered.
     * @return - The name of the CSV file containing candidate and ballot information.
     */
    private static String receiveAndValidateUserPrompt(){
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("\n[SYSTEM]: Enter name of the CSV file: ");
            String fileName = scanner.nextLine();

            if (fileName.equals("help")){
                System.out.println("[SYSTEM]: You should simply type the name of the CSV file that contains ballot information. Example: \"voting.csv\"");
            }
            else if (!fileName.endsWith("csv")){
                System.out.println("[SYSTEM]: Input file can only be a CSV file. Please enter the file name again.");
            } 
            else if (fileName.endsWith("csv")){
                File file = new File(fileName);

                if (file.exists()){
                    return fileName;
                } 
                else {
                    System.out.println("[SYSTEM]: The CSV file name you entered does not exist in the current directory.");
                    System.out.println("[SYSTEM]: Please ensure the file is present in the \"src\" folder.");
                }
            }
        }
    }   

    /**
     * This function is responsible for formatting the output that is displayed to the user once vote calculations are complete.
     * @param winningCandidate - The Candidate object that won the election. This is useful for displaying the winner's information
     * such as Candidate name, party affiliation and number of votes received to the election official.
     * @param candidates - The list of Candidate objects who particpated in the election. This is useful for displaying what % of votes
     * other candidates received along with their names and party affiliations.
     * @param numberOfBallots - The number of ballots cast for the election.
     */
    private static void displayIRWinner(Candidate winningCandidate, ArrayList<Candidate> candidates, int numberOfBallots){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%-30s %-30s %s%n", "Candidate & Party", "Number of Votes", "% of votes won"));

        DecimalFormat df = new DecimalFormat("#.00");
        for (Candidate candidate : candidates){
            String percentOfVotes = df.format(((double)candidate.getNumVotes() / numberOfBallots) * 100);
            if (percentOfVotes.equals(".00")){
                percentOfVotes = "0.00";
            }
            
            String candidateString = candidate.getName() + " (" + candidate.getParty() + ")";
            int candidateVotes = candidate.getNumVotes();

            // Display candidate votes as 0 if they were eliminated during redistribution
            if (candidate.isEliminated()){
                candidateVotes = 0;
                percentOfVotes = "0.00 (Eliminated)";
            }
            sb.append(String.format("%-37s %-28d %s\n", candidateString, candidateVotes, percentOfVotes));
        }

        sb.append(String.format("\nWinning candidate is %s from the %s. party who wins with %d votes to their name.\n", winningCandidate.getName(), winningCandidate.getParty(), winningCandidate.getNumVotes()));

        System.out.println(sb.toString());
    }  

    /**
     * This function is helpful for displaying the winning Party and Candidate for the election along with details of how many seats
     * each party received as well as the number of votes.
     * @param winningParty - The name of the party that won the election. 
     * @param winningCandidate - The name of the candidate that won the election.
     * @param parties - The list of Parties that participated in the election. 
     * @param numBallots - The number of ballots cast for the election.
     * @param numSeats - The number of seats to be allocated.
     */
    private static void displayOPLWinner(Party winningParty, Candidate winningCandidate, ArrayList<Party> parties, int numBallots, int numSeats) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%-30s %-30s %-30s %s\n", "Parties", "Number of Votes", "Number of Seats", "% of Votes to % of Seats"));

        DecimalFormat df1 = new DecimalFormat("#.00");
        for (Party party : parties) {
            String percentOfVotes = df1.format(((double) party.getInitialPartyVotes() / numBallots) * 100);
            String percentOfSeats = df1.format(((double) party.getNumSeatsAllocated() / numSeats) * 100);
            if (percentOfVotes.equals(".00")){
                percentOfVotes = "0.00";
            } else if (percentOfSeats.equals(".00")) {
                percentOfSeats = "0.00";
            }

            String partyString = party.getPartyName();
            int partyTotalVotes = party.getInitialPartyVotes();
            int partySeats = party.getNumSeatsAllocated();
            String percentVotesAndString = percentOfVotes + "/" + percentOfSeats;

            sb.append(String.format("%-37s %-30d %-29d %s\n", partyString, partyTotalVotes, partySeats, percentVotesAndString));
        }

        sb.append(String.format("\nThe %s party has won the election with %d votes and %d seats.\n\n", winningParty.getPartyName(), winningParty.getInitialPartyVotes(), winningParty.getNumSeatsAllocated()));
        sb.append("The winning party's candidates list details are below:\n");
        sb.append(String.format("\n%-30s %-30s %s%n", "Candidate & Party", "Number of Votes", "% of votes won"));

        DecimalFormat df2 = new DecimalFormat("#.00");
        for (Candidate candidate : winningParty.getCandidates()){
            String percentOfVotes = df2.format(((double) candidate.getNumVotes() / numBallots) * 100);
            if (percentOfVotes.equals(".00")){
                percentOfVotes = "0.00";
            }
            
            String candidateString = candidate.getName() + " (" + candidate.getParty() + ")";
            int candidateVotes = candidate.getNumVotes();

            sb.append(String.format("%-37s %-28d %s\n", candidateString, candidateVotes, percentOfVotes));
        }

        sb.append(String.format("\nWinning candidate is %s from the %s party who wins with %d votes to their name.\n", winningCandidate.getName(), winningCandidate.getParty(), winningCandidate.getNumVotes()));

        System.out.println(sb.toString());
    }


 /**
     * This function is helpful for displaying the winning Candidates for the election along with details of how many seats
      each candidates received as well as the number of votes.
     * @param winningCandidates - All the names of the candidates that won sesats during the election 
     * @param candidates - The list of candidates that participated in the election. 
     * @param numBallots - The number of ballots cast for the election.
     * @param numSeats - The number of seats to be allocated.
     */
    private static void displayMPOWinners(ArrayList<Candidate> winningCandidates, ArrayList<Candidate> candidates, int numBallots, int numSeats) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nPercentage of votes and seats won each candidate received are shown below\n");
        sb.append(String.format("\n%-30s %-30s %-30s %-30s %s\n", "Candidates", "Number of Votes", "Number of Seats Won", "% of Total Votes", "% of Total Seats"));
        
        DecimalFormat df1 = new DecimalFormat("#.00");

        for(Candidate candidate : candidates){
            String percentOfVotes = df1.format(((double)candidate.getNumVotes() / numBallots) * 100);
            String percentOfSeats = df1.format(((double)candidate.getNumSeats() / numSeats) * 100);
            if (percentOfVotes.equals(".00")){
                percentOfVotes = "0.00";
            }
            if (percentOfSeats.equals(".00")) {
                percentOfSeats = "0.00";
            }

            String candidateString = candidate.getName() + " (" + candidate.getParty() + ")";
            int candidateVotes = candidate.getNumVotes();
            int candidateSeats = candidate.getNumSeats();
            sb.append(String.format("%-37s %-30d %-29d %-29s %s\n", candidateString, candidateVotes, candidateSeats, percentOfVotes, percentOfSeats));
        }

        sb.append("\nList of all " + winningCandidates.size() + " candidates who won seats are shown below:\n");
        for(Candidate candidate : winningCandidates){
            String candidateString = candidate.getName() + " (" + candidate.getParty() + ")";
            sb.append(String.format("%s\n", candidateString));
        }
        System.out.println(sb.toString());
    }
}
