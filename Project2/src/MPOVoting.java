/**
 * MPOVoting.java
 * @author Jashwin Acharya (achar061) and Steven Petzold (petzo017)
 * This class performs ballot calculations using the Multiple Preferential Ordering voting protocol.
 * All tie handling and seat allocations is handled by this class along with writing
 * election process details to an audit file.
 */

import java.util.*;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * MPOVoting class represents the Multiple Preferential Ordering voting system.
 * It extends the Voting class and implements the functionality specific to the MPO voting protocol.
 */
public class MPOVoting extends Voting {
    private String candidatePartyInfo;
    private ArrayList<String> ballotList;
    private int numSeats; // Number of seats
    private ArrayList<Candidate> candidates;
    private ArrayList<Party> parties;
    private ArrayList<Candidate> winningCandidates;
    private int initialNumberOfSeats;

    /**
     * Constructor for MPOVoting class.
     *
     * @param candidateLine   The line containing Candidate name and party affiliation information.
     * @param ballotList      List of ballot strings.
     * @param numSeats        Number of seats to be allocated.
     * @param auditFilePath   Path to the audit file.
     */
    public MPOVoting(String candidateLine, ArrayList<String> ballotList, int numSeats, String auditFilePath) {
        // Remove starting and ending brackets from candidateLine for ease of processing
        candidatePartyInfo = candidateLine;
        candidatePartyInfo = candidatePartyInfo.substring(1, candidatePartyInfo.length()-1);
        this.ballotList = ballotList;
        this.numSeats = numSeats;
        this.auditFilePath = auditFilePath;
        parties = new ArrayList<>();
        winningCandidates = new ArrayList<>();
        initialNumberOfSeats = numSeats;
        // Process Part and Candidate information.
        processCandidateInfo();

        // Process ballot list for all parties
        processBallots();

        // Initialize audit file
        File auditFile = new File(auditFilePath);
        if (auditFile.exists()){
            auditFile.delete();
        }
    }

    /**
     * This function parses the line of the input CSV file containing the Candidate name and party affiliation information.
     */
    private void processCandidateInfo(){
        candidates = new ArrayList<>();
        String[] candidateInfo = candidatePartyInfo.split("], \\[");
        
        for (int i = 0; i < candidateInfo.length; i++) {
            String candidatePartyElement = candidateInfo[i];

            // candidatePatyElement has the form: "[Foster, D]", so we split it to extract candidate and party name
            String[] element_split = candidatePartyElement.split(", ");
            String candidateName = element_split[0];
            String party = String.valueOf(element_split[1].charAt(0));

            // Add new candidate to list of candidates
            Candidate candidate = new Candidate(candidateName,party);
            candidates.add(candidate);
        }
    }

    /**
     * This function calculates the number of votes each candidate receives.
     */
    private void processBallots() {
        for (String ballotLine : ballotList) { // Distribute all votes to their respective parties and candidates
            String[] ballotInfo = ballotLine.split(",");
            int indexToUpdate = -1; // Get the position of the candidate that the vote is going towards

            for (int i = 0; i < ballotInfo.length; i++) { // Determine the position of the candidate that got a vote
                if (ballotInfo[i].equals("1")) {
                    indexToUpdate = i;
                    break;
                }
            }

            // Increment candidate vote
            candidates.get(indexToUpdate).incrementVote();
        }
    }

    /**
     * Perform the seat allocation process using the MPO voting protocol.
     */
    public void performSeatAllocations(){
        // Sort list of candidates in descending order by the number of votes each have.
        Collections.sort(candidates, (candidateOne, candidateTwo) -> {
            return candidateTwo.getNumVotes() - candidateOne.getNumVotes();
        });

        for(int i = 0; i < candidates.size() - 1; i++) {
            // If no seats are left to assign, just break out of the loop
            if (numSeats == 0) {
                break;
            }

            // Get first and second candidate for performing comparisons
            Candidate firstCandidate = candidates.get(i);
            Candidate secondCandidate = candidates.get(i + 1);

            // If the Candidate at position i has received seats, then there is no point in comparing
            // it with the second candidate
            if (firstCandidate.getNumSeats() != 0){
                continue;
            }

            // Write candidate information to the audit file
            buildAndWriteRoundResultsToAuditFile();
        
            // If first Candidate's votes are greater than the second candidate's votes, then assign a seat to the first candidate
            if (firstCandidate.getNumVotes() > secondCandidate.getNumVotes()) {
                firstCandidate.incrementNumSeats();
                winningCandidates.add(firstCandidate);
                numSeats -= 1;
        
                // Display that a seat has been allocated to the first candidate
                StringBuilder seatAllocationInfo = new StringBuilder();
                seatAllocationInfo.append(String.format("\n1 seat allocated to %s since they have a higher number of votes than the candidate after them.\n\n", firstCandidate.getName()));
                seatAllocationInfo.append(String.format("Remaining Seats: %d\n", numSeats));
        
                // Write seat allocation information to the audit file
                writeToAuditFile(seatAllocationInfo);
            } else {
                // Handle tie breaker
                handleTie(i, firstCandidate.getNumVotes());
            }
        }

        // Handle edge case where it's possible that the last candidate in the list doesn't receive a seat
        // when the number of candidates is an odd number and a seat is still available
        Candidate lastCandidate = candidates.get(candidates.size()-1);
        if (numSeats > 0 && lastCandidate.getNumSeats() == 0 && lastCandidate.getNumVotes() != 0){
            lastCandidate.incrementNumSeats();
            winningCandidates.add(lastCandidate);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("\n1 last seat allocated to the last remaining candidate %s.\n", lastCandidate.getName()));
            writeToAuditFile(sb);
            numSeats -= 1;
        }

        // Write final results to audit file along with the winner candidate names
        buildAndWriteRoundResultsToAuditFile();
    }

    /**
     * Handles tie-breaking for candidates with the same number of votes.
     * Retrieves candidates currently tied with the same number of votes and who haven't been assigned any seats yet.
     * Performs seat allocations until no seats are left or no tied candidates are left to assign seats to.
     *
     * @param candidatePosition The position of the candidate in the sorted list.
     * @param numVotes          The number of votes the tied candidates have.
     */
    protected void handleTie(int candidatePosition, int numVotes){
        // Get all candidates currently tied with the same number of votes as the candidate at candidatePosition and who haven't been assigned any seats yet.
        // Example, if list of candidates = [Pike, Borg, Foster, Deutsch, Harry] and Borg, Foster and Deutsch are tied at the same number of votes,
        // in that case, candidatePosition will be 1 (i.e., Borg) and tiedCandidatess would contain [Borg, Foster, Deutsch] since they all have the same
        // number of votes.
        ArrayList<Candidate> tiedCandidatess = getTiedCandidatess(candidatePosition, numVotes);

        // Perform seat allocations until no seats are left or no tied candidates are left to assign seats to
        while (numSeats > 0 && tiedCandidatess.size() > 0) {
            if (tiedCandidatess.size() > 1) {
                buildAndWriteCandidateTieInformation(tiedCandidatess);

                // Perform tie breaker to identify a winning candidate who receives seats
                SecureRandom random = new SecureRandom();
                int randomIdx = random.nextInt(tiedCandidatess.size());
                Candidate chosenCandidate = tiedCandidatess.get(randomIdx);
                chosenCandidate.incrementNumSeats();
                winningCandidates.add(chosenCandidate);

                // Write chosen candidate name to audit file as the winner of the tie breaker
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("The winner of the tie result is %s and wins 1 seat.%n", chosenCandidate.getName()));
                writeToAuditFile(sb);

                // After a candidate has been assigned a seat, we don't want to include it in subsequent tie-breaker round(s).
                tiedCandidatess.remove(randomIdx);
            } else if (tiedCandidatess.size() == 1) {
                // If there is only one candidate left in the tiedCandidatess list, assign them 1 seat
                int lastIdx = tiedCandidatess.size() - 1;
                Candidate lastCandidate = tiedCandidatess.get(lastIdx);
                lastCandidate.incrementNumSeats();
                winningCandidates.add(lastCandidate);
                tiedCandidatess.remove(lastIdx);

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("\n%s wins 1 seat since they aren't currently tied with any other candidate.\n", lastCandidate.getName()));
                writeToAuditFile(sb);
            }

            // Decrement number of seats
            numSeats -= 1;
        }
    }

    /**
     * Retrieves all candidates that are tied with the same number of votes as the candidate at candidatePosition.
     * @param candidatePosition The position of the candidate in the sorted list.
     * @param numVotes          The number of votes the candidate at candidatePosition in the candidates arraylist has.
     * @return                  List of candidates currently tied at the same number of votes as the candidate at candidatePosition in the list of candidates.
     * 
     */
    private ArrayList<Candidate> getTiedCandidatess(int candidatePosition, int numVotes){
        ArrayList<Candidate> tiedCandidatess = new ArrayList<>();
        for(int i = candidatePosition; i < candidates.size(); i++){
            if (candidates.get(i).getNumVotes() == 0 || candidates.get(i).getNumVotes() != numVotes) {
                break;
            } else if (candidates.get(i).getNumVotes() == numVotes){
                tiedCandidatess.add(candidates.get(i));
            }
        }
        return tiedCandidatess;
    }

    /**
     * This function write StringBuilder content to the audit file. This function is helpful 
     * for writing party, candidate, rounds of seat allocation, and tie information to the 
     * audit file.
     * 
     * @param sb StringBuilder object containing the content to be written to the audit file.
     */
    @Override
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
     * This function uses a StringBuilder object to build a string containing 
     * information such as the candidate's information, number/% of votes received,
     * number/% of seats received and the number of seats left for the next round. This 
     * StringBuilder object is later on passed to writeToAuditFile() to write 
     * the round results to the audit file.
     */
    private void buildAndWriteRoundResultsToAuditFile() {
        StringBuilder sb = new StringBuilder();

        // If the initial number of seats available and number of seats left to be allocated are the same number,
        // that means we have no started allocating seats yet.
        if (numSeats == initialNumberOfSeats){
            sb.append("Voting Protocol Name: MPO (Multiple Popularity Only)\n");
            sb.append("Initial Vote Calculation Results:\n");
        } else if (numSeats == 0) {
            sb.append("\nFinal Round of Seat Allocation Results:\n");
        } else{
            sb.append("\nCurrent Round Seat Allocation Results:\n");
        }

        sb.append(String.format("\n%-30s %-30s %-30s %-30s %s\n", "Candidates", "Number of Votes", "Number of Seats Won", "% of Total Votes", "% of Total Seats"));

        DecimalFormat df = new DecimalFormat("#.00");
        for (Candidate candidate : candidates) {
            // Build string containing candidate name and party affiliation information
            String percentOfVotes = df.format(((double)candidate.getNumVotes() / ballotList.size()) * 100);
            String percentOfSeats = df.format(((double)candidate.getNumSeats() / initialNumberOfSeats) * 100);

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
        
        // All seats allocated, so write Winner candidate information to audit file
        if (numSeats == 0){
            sb.append("\nThe following candidates won seats: \n\n");
            for (Candidate candidate : winningCandidates){
                sb.append(String.format("%s from the %s party.\n", candidate.getName(), candidate.getParty()));
            }
        } else {
            sb.append(String.format("\nRemaining Seats: %d\n", numSeats));
        }
        writeToAuditFile(sb);
    }

    /**
     * This function uses a StringBuilder object to build a string containing
     * information about candidates that are tied. This StringBuilder object 
     * is later on passed to writeToAuditFile() to write 
     * the round results to the audit file.
     * @param tiedCandidatess
     */
    private void buildAndWriteCandidateTieInformation( ArrayList<Candidate> tiedCandidatess){
        StringBuilder tiedCandidatesInfo = new StringBuilder("\nList of candidates currently tied with the same number of votes: ");
        for (Candidate candidate : tiedCandidatess) {
            tiedCandidatesInfo.append(candidate.getName()).append(", ");
        }
        tiedCandidatesInfo.delete(tiedCandidatesInfo.length() - 2, tiedCandidatesInfo.length()); // Remove the trailing comma and space
        tiedCandidatesInfo.append("\n");
        this.writeToAuditFile(tiedCandidatesInfo);
    }
    
    /**
     * This function returns the parties in the election.
     * 
     * @return The party array.
     */
    public ArrayList<Party> getParties(){
        return parties;
    }

     /**
     * This function returns the candidates in the election.
     * 
     * @return The candidate array.
     */
    public ArrayList<Candidate> getCandidates(){
        return candidates;
    }

     /**
     * This function returns the winning candidates in the election.
     * 
     * @return The winning candidate array.
     */
    public ArrayList<Candidate> getWinningCandidates(){
        return winningCandidates;
    }
}

