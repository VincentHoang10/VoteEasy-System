/**
 * OPLVoting.java
 * @author Vincent Hoang (hoang317)
 * This class performs ballot calculations using the Open Party List voting protocol.
 * All tie handling and seat allocations is handled by this class along with writing
 * election process details to an audit file.
 */

import java.util.*;
import java.security.SecureRandom;
import java.lang.Math;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.text.DecimalFormat;

public class OPLVoting extends Voting {
    private String candidatePartyInfo; // Contains candidates' name and party
    private ArrayList<String> ballotList; // ArrayList of all the ballots
    private static ArrayList<Candidate> tiedCandidates; // ArrayList containing tied candidates
    private  ArrayList<Party> tiedParties; // ArrayList containing tied parties
    private ArrayList<Party> remainingVoteTies; // ArrayList containing parties with the same remaining votes
    private ArrayList<Party> receivedRemainingSeats; // ArrayList to keep track of parties that received a remaining seat
    private int numSeats; // Number of seats
    private int numBallots; // Number of ballots
    private ArrayList<Party> parties; // ArrayList of all parties
    private static Party winningParty; // Party that won
    private Party partyWithAllVotes; // Party that has all the votes
    private int quota; // Calculated quota based of the number of ballots / number of seats

    /**
     * This is the constructor that initializes the member variables of the class and 
     * the name of the audit file.
     * 
     * @param candidateLine Line containing the candidate name and party affiliations.
     * @param ballotList List of ballot lines in the CSV file.
     * @param numSeats The total number of seats that are to be allocated.
     * @param numBallots The total number of ballots cast for the election.
     * @param auditFilePath Name and path of the audit file that is generated when running the entire system.
     */
    public OPLVoting(String candidateLine, ArrayList<String> ballotList, int numSeats, int numBallots, String auditFilePath) {
        this.candidatePartyInfo = candidateLine;
        this.ballotList = ballotList;
        this.tiedCandidates = new ArrayList<Candidate>();
        this.tiedParties = new ArrayList<Party>();
        this.receivedRemainingSeats = new ArrayList<Party>();
        this.numSeats = numSeats;
        this.numBallots = numBallots;
        this.parties = processPartyInfo(candidatePartyInfo);
        this.partyWithAllVotes = null;
        this.quota = (int) Math.ceil((double) numBallots / (double) numSeats);
        this.auditFilePath = auditFilePath;

        // Delete audit file created during a previous run
        File auditFile = new File(auditFilePath);
        if (auditFile.exists()){
            auditFile.delete();
        }
    }

    /**
     * This function create an arraylist of Party objects and sets up the Party object's
     * member variables.
     * 
     * @param candidateLine Line containing the candidate name and party affiliations.
     * @return An arraylist of Party objects.
     */
    private ArrayList<Party> processPartyInfo(String candidateLine) {
        ArrayList<Party> parties = new ArrayList<Party>();
        ArrayList<String> addedParties = new ArrayList<String>();
        String[] candidateList = candidateLine.split(", ");

        for (String candidate : candidateList) { // Create a list of all the parties and assign all candidates to their parties
            String[] candidateInfo = candidate.split(" ");
            String candidateName = candidateInfo[0];
            String candidateParty = String.valueOf(candidateInfo[1].charAt(1));

            if (!addedParties.contains(candidateParty)) { // If the party has not been added yet to parties
                Party newParty = new Party(candidateParty);
                Candidate newCandidate = new Candidate(candidateName, candidateParty);
                newParty.addCandidate(newCandidate);
                parties.add(newParty);
                addedParties.add(candidateParty);
            } else { // If the party has been added to parties
                for (Party party : parties) {
                    if (party.getPartyName().equals(candidateParty)) { // If the party is the candidate's party
                        Candidate newCandidate = new Candidate(candidateName, candidateParty);
                        party.addCandidate(newCandidate);
                    }
                }
            }
        }

        return parties;
    }

    /**
     * This function calculates the number of votes each party receives, as well 
     * as update the votes of the candidates within these parties.
     * 
     * @param ballotList List of ballot lines in the CSV file.
     */
    private void processBallots(ArrayList<String> ballotList) {
        for (String ballotLine : ballotList) { // Distribute all votes to their respective parties and candidates
            String[] ballotInfo = ballotLine.split(",");
            int indexToUpdate = -1; // Get the position of the candidate that the vote is going towards

            for (int i = 0; i < ballotInfo.length; i++) { // Determine the position of the candidate that got a vote
                if (ballotInfo[i].equals("1")) {
                    indexToUpdate = i;
                    break;
                }
            }

            String[] candidateList = this.candidatePartyInfo.split(", ");
            String[] candidateInfo = candidateList[indexToUpdate].split(" ");
            String candidateName = candidateInfo[0];
            String candidateParty = String.valueOf(candidateInfo[1].charAt(1));

            for (Party party : parties) { // Find the party that the candidate is associated to that got the vote
                if (party.getPartyName().equals(candidateParty)) {
                    for (Candidate candidate : party.getCandidates()) {
                        if (candidate.getName().equals(candidateName)) {
                            candidate.incrementVote();
                            break;
                        }
                    }
                    
                    party.incrementPartyVote();
                    party.setInitialPartyVotes(party.getTotalPartyVotes());
                    break;
                }
            }
        }
    }

    /**
     * This function performs the seat allocation calculations using the “Largest remainder 
     * formula," determines the winning Party and Candidate, and write each round of information 
     * to the audit file.
     */
    public void performSeatAllocations() {
        this.processBallots(ballotList);
        this.buildAndWriteInitialResultsToAuditFile();
        int round = 1; // Keeps track of the current round
        int seatsAvailable = numSeats; // Keeps track of the seats that are available

        while (seatsAvailable != 0) { // While there are still seats left to allocate
            int totalAllocationsPerRound = 0; // Keeps track of the seats allocated to parties
            
            if (round == 1) { // First round seat allocation
                if (this.checkPartyWithAllVotes()) { // If a party has all the votes
                    for (Party party : parties) {
                        if (party.getTotalPartyVotes() != 0) { // If party does not have zero votes
                            partyWithAllVotes = party;
                            break;
                        }
                    }

                    partyWithAllVotes.incrementNumSeatsAllocated(seatsAvailable);
                    partyWithAllVotes.setPartyVote(0);
                    totalAllocationsPerRound += seatsAvailable;
                } else {
                    for (Party party : parties) { // Allocate seats using "largest remainder formula" 
                        int seatsAllocated = party.getTotalPartyVotes() / quota;
    
                        party.incrementNumSeatsAllocated(seatsAllocated);
                        int remainingPartyVotes = party.getTotalPartyVotes() % quota;
                        party.setPartyVote(remainingPartyVotes);
                        totalAllocationsPerRound += seatsAllocated;
                    }
    
                    round++;
                }
            } else { // Allocate remaining seats
                this.determineRemainingSeatAllocation();
                totalAllocationsPerRound++;
            }

            seatsAvailable -= totalAllocationsPerRound; // decrement seats based on amount of seats that were allocated to parties

            this.buildAndWriteRoundResultsToAuditFile();
        }
        
        this.findPartyWithMostSeats(); // Determine winning party
        this.findPopularCandidates(); // Determine winning candidate
        this.buildAndWritePartyResultToAuditFile();
        this.buildAndWriteCandidateResultToAuditFile();
    }

    /**
     * This function determines which party will receive the remaining seats
     * based on their remaining votes.
     */
    private void determineRemainingSeatAllocation() {
        if (this.checkRemainderTie()) { // If parties have the same most remaining votes
            this.handleRemainderTie(); // Perform a "coin flip" to determine the party that gets the seat
            this.buildAndWriteTieResultToAuditFile();
        } else { // If parties don't have the same most remaining votes
            remainingVoteTies.get(0).incrementNumSeatsAllocated(1);
            receivedRemainingSeats.add(remainingVoteTies.get(0));
        }
    }

    /**
     * This function checks if there is a tie among the remaining votes of the parties 
     * and store the parties with the same tied remainder votes in the remainderTies 
     * member variable.
     * 
     * @return True if there is a tie and false if there isn't a tie.
     */
    private boolean checkRemainderTie() {
        remainingVoteTies = new ArrayList<Party>(); // Set to an empty arraylist
        int maxRemainder = -1;

        for (Party party : parties) { // Set maxRemainder to any party's remaining votes that haven't received a remaining seat
            if (!receivedRemainingSeats.contains(party)) {
                maxRemainder = party.getTotalPartyVotes();
                break;
            }
        }

        for (Party party : parties) { // Find the party that has the greatest remaining votes that haven't received a remaining seat
            if (party.getTotalPartyVotes() > maxRemainder) {
                maxRemainder = party.getTotalPartyVotes();
            }
        }

        for (Party party : parties) { // Determine if there is a tie between any parties with the same most remaining votes that hasn't received a remaining seat
            if (party.getTotalPartyVotes() == maxRemainder && !receivedRemainingSeats.contains(party)) {
                remainingVoteTies.add(party);
            }
        }

        return remainingVoteTies.size() > 1;
    }

    /**
     * This function performs a tie breaker to handle ties for the remaining votes by performing 
     * a "coin flip," increments the party's seat that won the coin flip, and keeps track of
     * the parties that got their seats incremented.
     */
    private void handleRemainderTie() {
        SecureRandom random = new SecureRandom();
        int randomIdx = random.nextInt(remainingVoteTies.size()); // Determine random party to receive a remaining seat
        remainingVoteTies.get(randomIdx).incrementNumSeatsAllocated(1);
        receivedRemainingSeats.add(remainingVoteTies.get(randomIdx)); // Add the party that received a remaining seat to receivedRemainingSeats member variable
    }

    /**
     * This function finds the party with the highest number of seats using 
     * simple linear search and set the winningParty variable to the Party 
     * object that has the highest number of seats.
     */
    private void findPartyWithMostSeats() {
        if (this.checkPartyTie()) { // If more than one party have the same most number of seats allocated
            this.handlePartyTie(); // Perform a "coin flip" to determine the party that wins
            this.buildAndWriteTieResultToAuditFile();
        } else { // If there is only one party that has the most number of seats allocated
            winningParty = tiedParties.get(0);
        }
    }

    /**
     * This function is called to find the winning Candidate and write the result to 
     * the audit file.
     */
    private void findPopularCandidates() {
        if (this.checkTie()) { // If more than one candidate have the same most number of votes
            this.handleTie(); // Perform a "coin flip" to determine the candidate that wins
            this.buildAndWriteTieResultToAuditFile();
        } else { // If there is only one candidate that has the most number of votes
            winningCandidate = tiedCandidates.get(0);
        }
    }

    /**
     * This function checks to see if there is a party that received all
     * the votes in the first round.
     * 
     * @return True if there is and false if there isn't.
     */
    private boolean checkPartyWithAllVotes() {
        ArrayList<Party> zeroVoteParties = new ArrayList<Party>(); // Arraylist to keep track of parties with zero votes

        for (Party party : parties) {
            if (party.getTotalPartyVotes() == 0) { // Party has zero votes
                zeroVoteParties.add(party);
            }
        }

        return zeroVoteParties.size() == (parties.size() - 1); // If every party except for one has zero votes then true, otherwise false
    }

    /**
     * This function checks if there is a tie among the parties and store 
     * the tied parties in the tiedParties member variable.
     * 
     * @return True if there is a tie and false if there isn't a tie.
     */
    private boolean checkPartyTie() {
        int maxSeats = parties.get(0).getNumSeatsAllocated(); // Set maxSeats to the number of seats of the first party in parties

        for (int i = 1; i < parties.size(); i++) {
            if (parties.get(i).getNumSeatsAllocated() > maxSeats) { // If the current party's number of seats is greater than the current maxSeats, change maxSeats
                maxSeats = parties.get(i).getNumSeatsAllocated();
            }
        }

        for (Party party : parties) {
            if (party.getNumSeatsAllocated() == maxSeats) { // If the current party has the same number of seats to maxSeats
                tiedParties.add(party);
            }
        }

        return tiedParties.size() > 1; // If there is more than one party in tiedParties, then there is a tie
    }

    /**
     * This function performs a tie breaker to handle ties for parties by performing 
     * a "coin flip" and sets the winningParty member variable value to the party that 
     * won the election.
     */
    private void handlePartyTie() {
        SecureRandom random = new SecureRandom();
        int randomIdx = random.nextInt(tiedParties.size()); // Determine random party to win
        winningParty = tiedParties.get(randomIdx);
    }

    /**
     * This function checks if there is a tie among the candidates of the winning 
     * party and store the tied candidates in the tiedCandidates member variable.
     * 
     * @return True if there is a tie and false if there isn't a tie.
     */
    protected boolean checkTie() {
        int maxVotes = winningParty.getCandidates().get(0).getNumVotes(); // Set maxVotes to the number of votes of the first candidate in winningParty

        for (int i = 1; i < winningParty.getCandidates().size(); i++) {
            if (winningParty.getCandidates().get(i).getNumVotes() > maxVotes) { // If the current candidate's number of votes in winningParty is greater than the current maxVotes, change maxSeats
                maxVotes = winningParty.getCandidates().get(i).getNumVotes();
            }
        }

        for (Candidate candidate : winningParty.getCandidates()) {
            if (candidate.getNumVotes() == maxVotes) { // If the current candidate in winningParty has the same number of votes to maxVotes
                tiedCandidates.add(candidate);
            }
        }

        return tiedCandidates.size() > 1; // If there is more than one candidate in tiedCandidates, then there is a tie
    }

    /**
     * This function performs a tie breaker to handle ties for candidates by performing 
     * a "coin flip" and sets the winningCandidate member variable value to the candidate 
     * that won the election.
     */
    protected void handleTie() {
        SecureRandom random = new SecureRandom();
        int randomIdx = random.nextInt(tiedCandidates.size()); // Determine random party to win
        winningCandidate = tiedCandidates.get(randomIdx);
    }
    
    /**
     * This function write StringBuilder content to the audit file. This function is helpful 
     * for writing party, candidate, rounds of seat allocation, and tie information to the 
     * audit file.
     * 
     * @param sb StringBuilder object containing the content to be written to the audit file.
     */
    protected void writeToAuditFile(StringBuilder sb){
        // The below line is for unit testing purposes since we don't want to create an audit file everytime we
        // execute a test case for OPLVoting
        if (auditFilePath.length() == 0) {
            return;
        }

        try{
            // Write StringBuilder contents to audit file.
            FileWriter fw = new FileWriter(auditFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(sb.toString());
            bw.close();
        } catch (Exception e){
            System.out.println("[SYSTEM]: Error occurred while writing to audit file. Exiting...");
            System.exit(0);
        }
    }

    /**
     * This function uses a StringBuilder object to write the initial results of an 
     * election before seat allocation is performed. Once the StringBuilder object 
     * is constructed, we can pass to the writeToAuditFile() function to write the 
     * initial results to the audit file.
     */
    protected void buildAndWriteInitialResultsToAuditFile(){
        StringBuilder sb = new StringBuilder();

        sb.append("Voting Protocol Name: Open Party List (OPL) \n\n");
        sb.append("Displaying Statistics before seat allocation...\n\n");
        sb.append(String.format("%-30s %-30s %s\n", "Party", "Number of Votes", "Seats Allocated"));

        for (Party party : parties) {       
            String partyString = party.getPartyName();
            int partyVotes = party.getTotalPartyVotes();

            sb.append(String.format("%-37s %-30d %d\n", partyString, partyVotes, 0));
        }

        this.writeToAuditFile(sb);
    }

    /**
     * This function uses a StringBuilder object to build a string containing 
     * information from all parties such as number of seats allocated in 
     * the current round and remaining votes left for the next round. This 
     * StringBuilder object is later on passed to writeToAuditFile() to write 
     * the round results to the audit file.
     */
    private void buildAndWriteRoundResultsToAuditFile() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nAfter round of seat allocation:\n\n");
        sb.append(String.format("%-30s %-30s %s\n", "Party", "Remaining Votes", "Seats Allocated"));

        for (Party party : parties) {
            String partyString = party.getPartyName();
            int partyVotes = party.getTotalPartyVotes();
            int partySeats = party.getNumSeatsAllocated();

            sb.append(String.format("%-37s %-30d %d\n", partyString, partyVotes, partySeats));
        }

        this.writeToAuditFile(sb);
    }
    
    /**
     * This function uses a StringBuilder object to build a string containing 
     * information for all parties such as party name, number of votes, number 
     * of seats allocated after seat allocation is complete and % of Votes to % of 
     * seats. We also append a string containing the winning party’s name, number 
     * seats won and number of votes received. This StringBuilder object is later 
     * on passed to writeToAuditFile() to write the final seat allocation results 
     * to the audit file.
     */
    private void buildAndWritePartyResultToAuditFile() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nFinal result of the election after completing all seat allocations including tie-breakers:\n\n");
        sb.append(String.format("%-30s %-30s %-30s %s\n", "Party", "Number of Votes", "Number of Seats", "% of Votes to % of Seats"));

        DecimalFormat df = new DecimalFormat("#.00");
        for (Party party : parties) {
            String percentOfVotes = df.format(((double) party.getInitialPartyVotes() / numBallots) * 100);
            String percentOfSeats = df.format(((double) party.getNumSeatsAllocated() / numSeats) * 100);
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

        this.writeToAuditFile(sb);
    }

    /**
     * This function uses a StringBuilder object to build a string containing the 
     * winning candidate’s information such as name, party name, number of votes, and
     * % of votes won. We do the same for the rest of the candidates too. This StringBuilder 
     * object is later on passed to writeToAuditFile() to write the final candidate popularity 
     * results to the audit file.
     */
    private void buildAndWriteCandidateResultToAuditFile() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-30s %-30s %s\n", "Candidate & Party", "Number of Votes", "% of votes won"));

        DecimalFormat df = new DecimalFormat("#.00");
        for (Candidate candidate : winningParty.getCandidates()) {
            // Format 0.00% to write to the audit file
            String percentOfVotes = df.format(((double) candidate.getNumVotes() / numBallots) * 100);
            if (percentOfVotes.equals(".00")){
                percentOfVotes = "0.00";
            }
            
            // Build string containing candidate name and party affiliation information
            String candidateString = candidate.getName() + " (" + candidate.getParty() + ")";
            int candidateVotes = candidate.getNumVotes();

            sb.append(String.format("%-37s %-28d %s\n", candidateString, candidateVotes, percentOfVotes));
        }

        sb.append(String.format("\nWinning candidate is %s from the %s party who wins with %d votes to their name.", winningCandidate.getName(), winningCandidate.getParty(), winningCandidate.getNumVotes()));

        this.writeToAuditFile(sb);
    }

    /**
     * This function uses a StringBuilder object to build a string with the names 
     * of the tied candidates or parties and the winner of the tie. The StringBuilder 
     * object can then be passed to the writeToAuditFile() function to write the tie 
     * result to the audit file.
     */
    protected void buildAndWriteTieResultToAuditFile(){
        StringBuilder sb = new StringBuilder();

        if (tiedCandidates.size() > 0) { // If it is a candidate tie result
            sb.append("\nThe following candidates were tied: \n\n");

            for (Candidate candidate : tiedCandidates) {
                sb.append(candidate.getName() + "\n");
            }

            sb.append(String.format("\nThe winner of the tie result is %s.\n", winningCandidate.getName()));
        } else if (tiedParties.size() > 0) { // If is a party tie result
            sb.append("\nThe following parties were tied: \n\n");

            for (Party party : tiedParties) {
                sb.append(party.getPartyName() + "\n");
            }

            sb.append(String.format("\nThe winner of the tie result is %s.\n", winningParty.getPartyName()));
        } else if (remainingVoteTies.size() > 0) { // If it is a remaining votes tie result
            sb.append("\nThe following parties' remaining votes were tied: \n\n");

            for (Party party : remainingVoteTies) {
                sb.append(party.getPartyName() + "\n");
            }
            
            Party partyReceivedSeat = null;
            for (int i = 0; i < receivedRemainingSeats.size(); i++) {
                for (Party party : parties) {
                    if (receivedRemainingSeats.get(i).getPartyName().equals(party.getPartyName())) {
                        partyReceivedSeat = receivedRemainingSeats.get(i);
                        break;
                    }
                }

                if (partyReceivedSeat != null) {
                    break;
                }
            }

            sb.append(String.format("\nThe winner of the tie result is %s.\n", partyReceivedSeat.getPartyName()));
        }

        // Write tie information to audit file
        writeToAuditFile(sb);
    }

    /**
     * This function returns the Party that won the election.
     * 
     * @return The winning Party object.
     */
    public Party getWinningParty() {
        return winningParty;
    }

    /**
     * This function returns an arraylist of Party objects.
     * 
     * @return An arraylist of Party objects.
     */
    public ArrayList<Party> getParties() {
        return parties;
    }
}
