/**
 * IRVoting.java
 * @author Jashwin Acharya (achar061)
 * This class performs ballot calculations using the Instant Runoff voting protocol.
 * All tie handling and vote redistribution is handled by this class along with writing
 * election process details to an audit file.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;

public class IRVoting extends Voting {
    private ArrayList<Candidate> candidates; // List of Candidate objects for storing the candidate's name, party affiliation and number of votes information.
    private ArrayList<ArrayList<Candidate>> ballots; // List of ballots where each element is a list of Candidate objects ordered by a voter's preference

    /**
     * Constructor that initializes the important variables needed for this class.
     * @param candidateLine - String that contains candidate name and party affiliation information.
     * @param ballotList - Arraylist of ballots parsed from the CSV file where each element contains the voter's preference
     * for a candidate.
     * @param auditFilePath - Name and path of the audit file that is generated when running the entire system.
     */
    public IRVoting(final String candidateLine, final ArrayList<String> ballotList, final String auditFilePath){
        this.candidates = processCandidateInfo(candidateLine);
        this.ballots = processBallotOrder(ballotList);
        this.auditFilePath = auditFilePath;

        // Delete audit file created during a previous run
        File auditFile = new File(auditFilePath);
        if (auditFile.exists()){
            auditFile.delete();
        }
    }

    /**
     * This function parses the line of the input CSV file containing the Candidate name and party affiliation information.
     * @param candidateLine - The line containing Candidate name and party affiliation information.
     * @return - List of Candidate objects created using the candidate's name and party information.
     */
    private ArrayList<Candidate> processCandidateInfo(String candidateLine){
        ArrayList<Candidate> candidates = new ArrayList<>();
        String[] candidatePartyInfo = candidateLine.split(", ");
        
        for (String candidatePartyElement : candidatePartyInfo){
            // candidatePatyElement has the form: "Rosen (D)", so we split it to extract candidate and party name
            String[] element_split = candidatePartyElement.split(" ");
            String candidate = element_split[0];
            String party = String.valueOf(element_split[1].charAt(1));

            // Add new candidate to list of candidates
            candidates.add(new Candidate(candidate, party));
        }

        return candidates;
    }

    /**
     * This function processes each line of the file that contains ballot information and constructs arraylists of 
     * Candidate objects where each arraylist contains Candidate objects ranked by voter preference.
     * @param ballotLines - The list of ballot lines in the CSV file.
     * @return - A arraylist where each element is an arraylist of Candidate objectes ranked by voter preference.
     */
    private ArrayList<ArrayList<Candidate>> processBallotOrder(ArrayList<String> ballotLines){
        ArrayList<ArrayList<Candidate>> ballots = new ArrayList<>();
        for (String ballotLine : ballotLines){
            // Example of a ballotLine String: 1,2,3,4 or 1,,,2
            String[] currentBallot = ballotLine.split(",", -1);
            ArrayList<Candidate> ballotOrder = new ArrayList<>(Collections.nCopies(candidates.size(), null));

            for (int voteIdx = 0; voteIdx < currentBallot.length; voteIdx++){
                String currentPreference = currentBallot[voteIdx];

                // Ensure that a preference has been set for a candidate by the voter
                if (currentPreference.length() != 0){
                    int candidateIdx = Integer.parseInt(currentPreference) - 1;
                    ballotOrder.set(candidateIdx, candidates.get(voteIdx));
                }
            }
            ballots.add(ballotOrder);
        }   
        return ballots;
    }

    /**
     * This function is the entry point for our ballot calulcations and calls other private functions for checking
     * if a majority or tie has arrived after the first round of calulcations, or if redistribution needs to be performed
     * to declare a winner
     */
    public void calculateBallots(){
        // perform first round of ballot calculation
        performFirstRoundCalculations();
        buildAndWriteInitialResultsToAuditFile();

        // Check if majority occurred after first round of ballot calculations.
        if (checkMajority()){
            buildAndWriteInitialResultsToAuditFile();
        }
        else if (checkTie()){
            // Handle tie if it occurred after first round of ballot calculations.
            handleTie();
            buildAndWriteTieResultToAuditFile();
        }
        else {
            // Redistribute votes if a tie or majority was not found during the first round of ballot calculations.
            redistributeVotes();
        }
    }

    /**
     * This function performs the first round of ballot calulcations which is necessary to determine
     * whether a candidate won in the first round or not, or if there's a tie.
     */
    private void performFirstRoundCalculations(){
        for (ArrayList<Candidate> ballot : ballots){
            Candidate currentCandidate = ballot.get(0);
            // Ensure that we only increment votes for Candidates the voter set a preference for
            if (currentCandidate != null){
                currentCandidate.incrementVote();
            }
        }
    }

    /**
     * If a tie or majority is not found during the first round of ballot calculations, this 
     * function is called to perform the redistribution of votes using other private helper functions.
     */
    private void redistributeVotes(){
        while (true) {
            // Check tie to ensure a previous redistribution didn't result in a tie
            if (checkTie()){
                handleTie();
                buildAndWriteTieResultToAuditFile();
                return;
            }

            // Find all candidates who have the lowest number of votes
            ArrayList<Candidate> eliminatedCandidates = findCandidateForElimination();

            // If more than 1 candidate is tied for the lowest number of votes, then perform a tie breaker
            Candidate tieWinnerCandidate = null;
            if (eliminatedCandidates.size() > 1){
                tieWinnerCandidate = handleIntermediateTie(eliminatedCandidates);

                // Remove tie winner from elimination candidate list so that their votes are not accidentally redistributed.
                eliminatedCandidates.remove(tieWinnerCandidate);
            }

            for (Candidate eliminatedCandidate : eliminatedCandidates){
                eliminatedCandidate.setElimination(true);

                // Find all ballots that have the eliminated candidate as their first preference
                ArrayList<ArrayList<Candidate>> eliminatedCandidateBallots = findEliminatedCandidateBallots(eliminatedCandidate);

                // If we have at least 1 valid ballot, perform redistribution
                if (eliminatedCandidateBallots.size() != 0){
                    updateVotesForCandidate(eliminatedCandidateBallots);
                }
            }

            // Write redistribution round results to audit file
            buildAndWriteRedistributedVotesToAuditFile(eliminatedCandidates, tieWinnerCandidate);

            // Once redistribution is performed, reset each candidate's votes
            for (Candidate candidate : candidates) {
                if (!candidate.isEliminated()){
                    candidate.resetRedistributedVotes();
                } 
            }

            // If we have a majority, then write the result of the election to the audit file; otherwise, perform a tie breaker
            // to determine a winner after redistribution
            if (checkMajority()){
                buildAndWriteInitialResultsToAuditFile();
                return;
            }
        }
    }

    /**
     * This private function checks whether any candidate received a majority number of votes.
     * @return: True if a majority is achieved; otherwise, False
     */
    private boolean checkMajority(){
        for (Candidate candidate : candidates){
            double fractionOfVotes = (double) candidate.getNumVotes() / ballots.size();
            // If candidate hasn't been eliminated and has a majority, return True
            if (!candidate.isEliminated() && fractionOfVotes > 0.5){
                winningCandidate = candidate;
                return true;
            }
        }
        return false;
    }

    /**
     * This function checks if all non-eliminated candidates are currently tied.
     * @return - True if election is currently tied; otherwise, False
     */
    protected boolean checkTie(){
        Candidate maxVotesCandidate = Collections.max(candidates, Comparator.comparingInt(Candidate::getNumVotes));
        int numVotes = maxVotesCandidate.getNumVotes();
        for (Candidate candidate : candidates){
            // If even one non-eliminated candidate with non-zero votes doesn't have the same number of votes as the other
            // candidates, that means we can definitely still perform redistributions, so return False.
            if (!candidate.isEliminated() && candidate.getNumVotes() != 0 && candidate.getNumVotes() != numVotes){
                return false;
            }
        }
        return true;
    }

    /**
     * If a tie is found, then we perform a tie break by using the SecureRandom class.SecureRandom is
     *  designed to be a cryptographically secure random number generator and should help ensure that
     *  we are choosing candidates fairly by essentially simulating a “coin toss” where each tied candidate
     *  has an equal probability of being chosen as the winner.
     */
    protected void handleTie(){
        // Filter out candidates that are currently tied
        ArrayList<Candidate> activeCandidates = new ArrayList<>();
        for (Candidate candidate : candidates){
            if (!candidate.isEliminated() && candidate.getNumVotes() != 0){
                activeCandidates.add(candidate);
            }
        }

        // Find random candidate to declare as the winner.
        SecureRandom random = new SecureRandom();
        int randomIdx = random.nextInt(activeCandidates.size());
        winningCandidate = activeCandidates.get(randomIdx);
    }

    /**
     * This method ensures that we perform our redistribution correctly by picking the lowest vote
     * candidate(s) to remove from our election.
     * @return - Arraylist that returns a list of candidate objects who are currently tied for the lowest number of votes.
     */
    private ArrayList<Candidate> findCandidateForElimination(){
        int lowestNumVotes = Integer.MAX_VALUE;
        for (Candidate candidate : candidates){
            int currentCandidateVotes = candidate.getNumVotes();
            if (!(candidate.isEliminated()) && currentCandidateVotes < lowestNumVotes){
                // Eliminate candidates with 0 current votes and look through the rest of the candidats
                // to find the candidate whose votes can be redistributed
                if (currentCandidateVotes == 0){
                    candidate.setElimination(true);

                    // Write eliminated candidate's name to the audit file.
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("\n\nCandidate %s has been eliminated since they recieved 0 votes.\n", candidate.getName()));
                    writeToAuditFile(sb);
                }
                else {
                    // Find candidate who can be eliminated.
                    lowestNumVotes = currentCandidateVotes;
                }
            }
        }
    
        // Find all candidates who have the lowest votes in-case a tie exists between multiple candidates for the lowest vote
        ArrayList<Candidate> elimCandidates = new ArrayList<>();
        for (Candidate cand : candidates){
            if (!(cand.isEliminated()) && cand.getNumVotes() == lowestNumVotes){
                elimCandidates.add(cand);
            }
        }

        return elimCandidates;
    }

    /**
     * Private function for handling ties between candidates who are tied for elimination when they all have the same
     * smallest number of votes to their name. The candidate who wins the toss stays in the election, whereas the rest
     * tied candidates are eliminated.
     * @param elimCandidates - Arraylist of candidate objects who are all currently tied for the lowest number of votes
     * @return - The candidate that will not be eliminated from the election and can participate in subsequent redistribution
     * rounds.
     */
    private Candidate handleIntermediateTie(ArrayList<Candidate> elimCandidates){
        // Perform tie breaker between lowest vote candidates
        SecureRandom random = new SecureRandom();
        int randomIdx = random.nextInt(elimCandidates.size());
        Candidate tieWinnerCandidate = elimCandidates.get(randomIdx);
        return tieWinnerCandidate;
    }

    /**
     * This function checks if the passed in ballot is valid or not i.e., does it have at least one 
     * ranked candidate who votes can be redistributed to. If the ballot is invalid, then it is removed
     *  from our ballots list entirely.
     * @param ballot - Array of candidate objects ranked by voter preference
     * @return - True if the ballot is valid and can be redistributed; otherwise, False
     */
    private boolean isValidBallot(ArrayList<Candidate> ballot){
        for (Candidate candidate : ballot){
            // If a candidate has been set as a preference and hasn't been eliminated yet, we return True
            if (candidate != null && !(candidate.isEliminated())){
                return true;
            }
        }
        // Invalid ballot, so return False
        return false;
    }

    /**
     * This function helps find the ballots that indicated our eliminated candidate as their 
     * first choice vote. Since there could be multiple ballots that indicated our candidate
     * as their first choice, we return a subset of the “ballots” arraylist member variable; 
     * hence the return type is ArrayList<Arraylist<Candidate>>
     * @param eliminatedCandidate - The candidate object that has been eliminated from the election and
     * whose votes can be redistributed.
     * @return - A list of all relevant ballots that have the eliminated candidate set as their first
     * preference.
     */
    private ArrayList<ArrayList<Candidate>> findEliminatedCandidateBallots(Candidate eliminatedCandidate){
        ArrayList<ArrayList<Candidate>> relevantBallots = new ArrayList<>();
        ArrayList<ArrayList<Candidate>> ballotsToRemove = new ArrayList<>();
        
        for (ArrayList<Candidate> ballot : ballots){
            // Ensure that the ballot's first choice is set to the eliminated candidate object
            // and the ballot is valid i.e., it has at least one candidate ranked who votes
            // can be redistributed to.
            if ((ballot.get(0) == eliminatedCandidate)){
                if (isValidBallot(ballot)) {
                    relevantBallots.add(ballot);
                }
                else {
                    // Invalid ballot
                    ballotsToRemove.add(ballot);
                }
            }
        }

        // Remove all invalid ballots from our overall collection of ballots
        ballots.removeAll(ballotsToRemove);
        return relevantBallots;
    }

    /**
     * This function simply loops through the candidate ballots that had the eliminated candidate
     *  as their first choice and redistibutes each voter’s ballot to their next valid choice candidate.
     * @param eliminatedCandidateBallots - Candidate ballots that had the eliminated candidate listed as their
     * first choice.
     */
    private void updateVotesForCandidate(ArrayList<ArrayList<Candidate>> eliminatedCandidateBallots){
        for (ArrayList<Candidate> ballot : eliminatedCandidateBallots){
            int candidateIdx = 0;
            for (int i = 0; i < ballot.size(); i++){
                Candidate candidate = ballot.get(i);
                if (candidate != null && !(candidate.isEliminated())){
                    candidate.incrementVote();
                    candidate.incrementRedistributedVotes();
                    candidateIdx = i;
                    break;
                }
            }

            // Reassign current ballot to the voter's next choice candidate
            // Example: Lets say the ballot was  [Royce, Rosen, Kleinberg, None]
            // Royce gets eliminated so the ballot goes to Rosen and the new ballot
            // would look like [Rosen, Kleinberg, None, None] indicating that the
            // ballot has now been assigned to Rosen.
            List<Candidate> reassignedBallot = ballot.subList(candidateIdx, ballot.size());
            for (int i = 0; i < ballots.size() - candidateIdx; i++){
                reassignedBallot.add(null);
            }

            // Remove old ballot
            ballots.remove(ballot);

            // Add new ballot
            ballots.add(new ArrayList<>(reassignedBallot));
        }
    }

    /**
     * Write StringBuilder content to the audit file. This function is helpful for writing first round
     * of calculation information, tie information and vote redistribution information to the audit file.
     * @param sb - StringBuilder object containing the content to be written to the audit file.
     */
    protected void writeToAuditFile(StringBuilder sb){
        // The below line is for unit testing purposes since we don't want to create an audit file everytime we
        // execute a test case for IRVoting
        if (auditFilePath.length() == 0) return;
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
     * This function is useful for building the string that details the number/% of votes a candidate
     * scored as well as the candidate's name and party affiliation.
     */
    protected void buildAndWriteInitialResultsToAuditFile(){
        StringBuilder sb = new StringBuilder();

        // If no winning candidate has been found yet, print initial messages to the audit file.
        if (winningCandidate == null){
            sb.append("Voting Protocol Name: Instant Runoff (IR) \n\n");
            sb.append("Displaying Statistics after first round of vote calculations...\n\n");
        }
        else{
            sb.append("\nFinal result of the election after completing all calculations including redistribution or tie-breakers:\n\n");
        }

        sb.append(String.format("%-30s %-30s %s%n", "Candidate & Party", "Number of Votes", "% of votes won"));

        DecimalFormat df = new DecimalFormat("#.00");
        for (Candidate candidate : candidates){
            // Format 0.00% to write to the audit file
            String percentOfVotes = df.format(((double)candidate.getNumVotes()/ballots.size()) * 100);
            if (percentOfVotes.equals(".00")){
                percentOfVotes = "0.00";
            }
            
            // Build string containing candidate name and party affiliation information
            String candidateString = candidate.getName() + " (" + candidate.getParty() + ")";
            int candidateVotes = candidate.getNumVotes();

            // Display candidate votes as 0 if they were eliminated during redistribution
            if (candidate.isEliminated()){
                candidateVotes = 0;
                percentOfVotes = "0.00 (Eliminated)";
            }
            sb.append(String.format("%-37s %-28d %s\n", candidateString, candidateVotes, percentOfVotes));
        }

        if (winningCandidate != null){
            sb.append(String.format("\nWinning candidate is %s from the %s. party who wins with %d votes to their name.", winningCandidate.getName(), winningCandidate.getParty(), winningCandidate.getNumVotes()));
        }
        writeToAuditFile(sb);
    }

    /**
     * This function is useful for building the string that details result of a tie to the audit file
     */
    protected void buildAndWriteTieResultToAuditFile(){
        // Store tied candidate information
        StringBuilder sb = new StringBuilder();
        sb.append("\nThe following candidates were tied: \n\n");
        for (Candidate candidate : candidates){
            if (!candidate.isEliminated() && candidate.getNumVotes() != 0){
                sb.append(candidate.getName() + "\n");
            }
        }
        sb.append(String.format("\nThe winner of the tie result is %s.\n", winningCandidate.getName()));
        
        // Write tie information to audit file
        writeToAuditFile(sb);

        // Write the final round results to the audit file once tie result has been written to it
        buildAndWriteInitialResultsToAuditFile();
    }

    /**
     * Write redistributed vote information for each candidate to the audit file
     * @param eliminatedCandidate - The eliminated candidate whose votes are redistributed
     */
    private void buildAndWriteRedistributedVotesToAuditFile(ArrayList<Candidate> eliminatedCandidates, Candidate tieWinnerCandidate){
        StringBuilder sb = new StringBuilder();
        sb.append("\nCurrent Redistribution Round Results:\n");

        if (tieWinnerCandidate != null){
            sb.append("\nFollowing Candidates are tied for the lowest votes:\n\n");
            sb.append(tieWinnerCandidate.getName() + "\n");
            for (Candidate candidate : eliminatedCandidates){
                sb.append(candidate.getName() + "\n");
            }
            sb.append("\nWinner of tie breaker is " + tieWinnerCandidate.getName() + ".\n");
        }
        sb.append("\nName(s) of candidate(s) eliminated during this round: \n\n");

        for (Candidate candidate : eliminatedCandidates){
            sb.append(candidate.getName() + "\n");
        }
        sb.append("\n");
        sb.append(String.format("%-30s %-40s %-30s %s%n", "Candidate & Party", "Number of redistributed votes", "Number of Votes", "% of votes won"));

        DecimalFormat df = new DecimalFormat("#.00");
        for (Candidate candidate : candidates){
            // Format 0.00% when writing to audit file
            String percentOfVotes = df.format(((double)candidate.getNumVotes()/ballots.size()) * 100);
            if (percentOfVotes.equals(".00")){
                percentOfVotes = "0.00";
            }

            // Build string containing candidate name and party affiliation information
            String candidateString = candidate.getName() + " (" + candidate.getParty() + ")";
            int candidateVotes = candidate.getNumVotes();
            int redistributedVotes = candidate.getRedistributedVotes();

            // Display candidate votes as 0 if they were eliminated during redistribution
            if (candidate.isEliminated()){
                candidateVotes = 0;
                redistributedVotes = 0;
                percentOfVotes = "0.00 (Eliminated)";
            }
            sb.append(String.format("%-37s %-40d %-28d %s\n", candidateString, redistributedVotes, candidateVotes, percentOfVotes));
        }

        // Write redistribution information to audit file
        writeToAuditFile(sb);
    }

    /**
     * Returns the list of candidates.
     * @return - List of candidates who participated in the election.
     */
    public ArrayList<Candidate> getCandidates(){
        return candidates;
    }

    /**
     * Returns list of ballots.
     * @return - List of ballots where each element is an arraylist of candidates ordered by voter preference.
     */
    public ArrayList<ArrayList<Candidate>> getBallots(){
        return ballots;
    }
}
