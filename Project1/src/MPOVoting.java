import java.util.*;
import java.security.SecureRandom;
import java.lang.Math;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.text.DecimalFormat;


public class MPOVoting extends Voting{
    private String candidatePartyInfo;
    private ArrayList<String> ballotList;
    private int numSeats; // Number of seats
    private int numBallots; // Number of ballots
    
        public MPOVoting(String candidateLine, ArrayList<String> ballotList, int numSeats, int numBallots, String auditFilePath) {
        this.candidatePartyInfo = candidateLine;
        this.ballotList = ballotList;
        this.numSeats = numSeats;
        this.numBallots = numBallots;
        this.auditFilePath = auditFilePath;

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

        for (String candidate : candidateList) {
            // Extract candidate name and party from the format "[Name, Party]"
            candidate = candidate.substring(1, candidate.length() - 1); // Remove square brackets
            String[] candidateInfo = candidate.split(", ");
            
            String candidateName = candidateInfo[0];
            String candidateParty = String.valueOf(candidateInfo[1].charAt(0));

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
        
}

