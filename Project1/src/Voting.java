/**
 * Voting.java
 * @author Jashwin Acharya (achar061)
 * This abstract class defines the necessary variables that the derived IRVoting and OPLVoting classes can use, along with the 
 * necessary functions they need to define.
 */

public abstract class Voting {
    protected Candidate winningCandidate; // This stores the information of the winning candidate such as name, party affialiation and number of votes received.
    protected String auditFilePath; // Name and path of the audit file that is generated when running the entire system.

    /**
     * This function returns the Candidate object that won the election.
     * @return - Candidate object that won the election.
     */
    public Candidate getWinningCandidate(){
        return winningCandidate;
    }

    /**
     * This function checks if all non-eliminated candidates are currently tied.
     * @return - True if election is currently tied; otherwise, False
     */
    protected boolean checkTie(){return false;};

    /**
     * If a tie is found, then we perform a tie break by using the SecureRandom class.SecureRandom is
     *  designed to be a cryptographically secure random number generator and should help ensure that
     *  we are choosing candidates fairly by essentially simulating a “coin toss” where each tied candidate
     *  has an equal probability of being chosen as the winner.
     */
    protected void handleTie(){};

    /**
     * This function is useful for building the string that details result of a tie to the audit file
     */
    protected void buildAndWriteTieResultToAuditFile(){};

    /**
     * This function is useful for building the string that details the number/% of votes a candidate
     * scored as well as the candidate's name and party affiliation.
     */
    protected void buildAndWriteInitialResultsToAuditFile(){};

    /**
     * Write StringBuilder content to the audit file. This function is helpful for writing first round
     * of calculation information, tie information and vote redistribution information to the audit file.
     * @param sb - StringBuilder object containing the content to be written to the audit file.
     */
    protected void writeToAuditFile(StringBuilder sb){};
}
