/**
 * Party.java
 * @author Steven Petzold
 * The Party class represents a political party participating in an election.
 */

import java.util.ArrayList;
import java.util.List;

public class Party {
    private String partyName; // Name of the political party
    private ArrayList<Candidate> candidates; // List of candidates associated with the party
    private int totalPartyVotes; // Total votes obtained by the party
    private int numSeatsAllocated; // Number of seats allocated to the party
    private int previousPartyVotes; // Previous total votes obtained by the party

    /**
     * Constructs a Party with a given party name.
     *
     * @param partyName The name of the political party.
     */
    public Party(String partyName) {
        this.partyName = partyName;
        this.candidates = new ArrayList<>(); // Initializing the list of candidates
        this.totalPartyVotes = 0; // Initializing the total votes to zero
        this.numSeatsAllocated = 0; // Initializing the allocated seats to zero
        this.previousPartyVotes = 0; // Initializing the previous total votes to zero
    }

    /**
     * Retrieves the name of the political party.
     *
     * @return The name of the political party.
     */
    public String getPartyName() {
        return this.partyName;
    }

    /**
     * Retrieves the list of candidates associated with the party.
     *
     * @return The list of candidates in the party.
     */
    public List<Candidate> getCandidates() {
        return this.candidates;
    }

    /**
     * Retrieves the total votes obtained by the party.
     *
     * @return The total votes obtained by the party.
     */
    public int getTotalPartyVotes() {
        return this.totalPartyVotes;
    }

    /**
     * Increases the total votes obtained by the party by 1.
     */
    public void incrementPartyVote() {
        this.totalPartyVotes++;
    }

    /**
     * Sets the total votes obtained by the party.
     *
     * @param numVotes The total votes to set for the party.
     */
    public void setPartyVote(int numVotes) {
        this.totalPartyVotes = numVotes;
    }

    /**
     * Adds a candidate to the party's list of candidates.
     *
     * @param candidate The candidate to add to the party.
     */
    public void addCandidate(Candidate candidate) {
        this.candidates.add(candidate);
    }

    /**
     * Retrieves the number of seats allocated to the party.
     *
     * @return The number of seats allocated to the party.
     */
    public int getNumSeatsAllocated() {
        return this.numSeatsAllocated;
    }

    /**
     * Increases the number of seats allocated to the party by a specified number.
     *
     * @param numSeats The number of seats to increment.
     */
    public void incrementNumSeatsAllocated(int numSeats) {
        this.numSeatsAllocated += numSeats;
    }

    /**
     * Retrieves the previous total votes obtained by the party.
     *
     * @return The previous total votes obtained by the party.
     */
    public int getInitialPartyVotes() {
        return this.previousPartyVotes;
    }

    /**
     * Sets the previous total votes obtained by the party.
     *
     * @param numVotes The previous total votes to set for the party.
     */
    public void setInitialPartyVotes(int numVotes) {
        this.previousPartyVotes = numVotes;
    }

    /**
     * Override equals() method.
     *
     * @param obj The object that is being compared.
     */
    @Override
    public boolean equals(Object obj) {
        Party otherParty = (Party) obj;

        if (otherParty.getPartyName().equals(this.getPartyName())) {
            return true;
        } else {
            return false;
        }
    }
}
