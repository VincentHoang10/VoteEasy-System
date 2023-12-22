/**
 * Candidate.java
 * @author Steven Petzold
 * The Candidate class represents a candidate participating in an election.
 */

public class Candidate {
    private String name; // Name of the candidate
    private String party; // Political party of the candidate
    private int numVotes; // Number of votes received by the candidate
    private int redistributedVotes; // Number of votes redistributed to this candidate
    private boolean eliminated; // Boolean indicating if the candidate is eliminated from the election
    private int candidateRank;

    /**
     * Constructs a Candidate with a given name and party.
     *
     * @param name  The name of the candidate.
     * @param party The political party of the candidate.
     */
    public Candidate(String name, String party) {
        this.name = name; // Set the name attribute
        this.party = party; // Set the party attribute
        this.numVotes = 0; // Initializing the number of votes to zero
        this.redistributedVotes = 0; // Initializing redistributed votes to zero
        this.eliminated = false; // Initializing the elimination status as false
    }

    /**
     * Retrieves the name of the candidate.
     *
     * @return The name of the candidate.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Retrieves the political party affiliation of the candidate.
     *
     * @return The party affiliation of the candidate.
     */
    public String getParty() {
        return this.party;
    }

    /**
     * Retrieves the number of votes received by the candidate.
     *
     * @return The number of votes received.
     */
    public int getNumVotes() {
        return this.numVotes;
    }

    /**
     * Increases the number of votes received by the candidate by 1.
     */
    public void incrementVote() {
        this.numVotes++;
    }

    /**
     * Increases the count of redistributed votes for the candidate by 1.
     */
    public void incrementRedistributedVotes() {
        this.redistributedVotes++;
    }

    /**
     * Resets the count of redistributed votes to zero for the candidate.
     */
    public void resetRedistributedVotes() {
        this.redistributedVotes = 0;
    }

    /**
     * Retrieves the count of redistributed votes for the candidate.
     *
     * @return The count of redistributed votes.
     */
    public int getRedistributedVotes() {
        return this.redistributedVotes;
    }

    /**
     * Sets the elimination status of the candidate.
     *
     * @param value The elimination status (true for eliminated, false for active).
     */
    public void setElimination(boolean value) {
        this.eliminated = value;
    }

    /**
     * Checks if the candidate has been eliminated from the election.
     *
     * @return True if the candidate is eliminated, otherwise false.
     */
    public boolean isEliminated() {
        return this.eliminated;
    }
}
