import java.util.ArrayList;
import org.junit.*;
import java.util.Map;
import java.util.HashMap;

public class MPOVotingTests {
    /**
     * This test checks that only one candidate wins a seat since there is only 1 seat available. The candidate
     * who wins the seat also has the highest number of votes.
     */
    @Test
    public void testOneWinner() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/MPO_test_one_winner.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();

        // Perform seat allocations
        final MPOVoting mpo = new MPOVoting(candidateLine, ballotList, numSeats, "");
        mpo.performSeatAllocations();

        ArrayList<Candidate> candidates = mpo.getCandidates();
        Candidate pike = candidates.get(0);
        Candidate foster = candidates.get(1);
        Candidate deutsch = candidates.get(2);
        Candidate borg = candidates.get(3);
        Candidate jones = candidates.get(4);
        Candidate smith = candidates.get(5);

        // Ensure votes were calculated correctly for all candidates:
        Assert.assertEquals(pike.getNumVotes(), 3);
        Assert.assertEquals(foster.getNumVotes(), 1);
        Assert.assertEquals(deutsch.getNumVotes(), 1);
        Assert.assertEquals(borg.getNumVotes(), 1);
        Assert.assertEquals(jones.getNumVotes(), 1);
        Assert.assertEquals(smith.getNumVotes(), 1);
        
        // Ensure only one candidate won seats and others didn't win anything.
        Assert.assertEquals(mpo.getWinningCandidates().size(), 1);
        Assert.assertEquals(pike.getNumSeats(), 1);
        Assert.assertEquals(foster.getNumSeats(), 0);
        Assert.assertEquals(deutsch.getNumSeats(), 0);
        Assert.assertEquals(borg.getNumSeats(), 0);
        Assert.assertEquals(jones.getNumSeats(), 0);
        Assert.assertEquals(smith.getNumSeats(), 0);
    }


    /**
     * This test ensures that when only 1 seat is available and all candidates are tied with the same number of votes that
     * each candidate has an equal chance of winning the seat. Since we have 6 candidates, each candidate should have a 
     * 11-21% of winning the seat (100/6 ~ 16.6% and we assume a + or - 5% tolerance; hence, the lower bound is 11% and 
     * upper bound is 21%).
     */
    @Test
    public void testTieFairnessWithOneWinner(){
        Map<String, Integer> map = new HashMap<>();
        map.put("Pike", 0);
        map.put("Foster", 0);
        map.put("Deutsch", 0);
        map.put("Borg", 0);
        map.put("Jones", 0);
        map.put("Smith", 0);

        for(int i = 0; i < 1000; i++) {
            // Read in test csv file
            final FileParser file = new FileParser("./../testing/MPO_test_tie_fairness_with_one_winner.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final int numSeats = file.getNumberOfSeats();

            // Perform seat allocations
            final MPOVoting mpo = new MPOVoting(candidateLine, ballotList, numSeats, "");
            mpo.performSeatAllocations();

            // Ensure only 1 candidate won
            ArrayList<Candidate> winningCandidates = mpo.getWinningCandidates();
            Assert.assertEquals(mpo.getWinningCandidates().size(), 1);

            Candidate winningCandidate = winningCandidates.get(0);
            map.put(winningCandidate.getName(), map.get(winningCandidate.getName()) + 1);
        }

        // Ensure each candidate has an equal chance of winning
        double low = 0.11;
        double high = 0.21;
        for (Map.Entry<String, Integer> entry : map.entrySet()){
            Integer winCount = entry.getValue();
            Assert.assertTrue((double) winCount/1000 >= low && (double) winCount/1000 <= high);
        }
    }

    /**
     * This test checks that multiple candidates win seats normally without any type of tie-breakers.
     */
    @Test
    public void testMultipleWinnersWithNoTieBreakers(){
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/MPO_test_multiple_winners_with_no_tie_breakers.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();

        // Perform seat allocations
        final MPOVoting mpo = new MPOVoting(candidateLine, ballotList, numSeats, "");
        mpo.performSeatAllocations();

        ArrayList<Candidate> candidates = mpo.getCandidates();
        Candidate pike = candidates.get(0);
        Candidate foster = candidates.get(1);
        Candidate jones = candidates.get(2);
        Candidate deutsch = candidates.get(4);
        Candidate borg = candidates.get(3);
        Candidate smith = candidates.get(5);

        // Ensure votes were calculated correctly for all candidates:
        Assert.assertEquals(pike.getNumVotes(), 3);
        Assert.assertEquals(foster.getNumVotes(), 2);
        Assert.assertEquals(deutsch.getNumVotes(), 0);
        Assert.assertEquals(borg.getNumVotes(), 0);
        Assert.assertEquals(jones.getNumVotes(), 1);
        Assert.assertEquals(smith.getNumVotes(), 0);

        // Ensure only 3 candidates won seats and other 3 didn't win anything.
        Assert.assertEquals(mpo.getWinningCandidates().size(), 3);
        Assert.assertEquals(pike.getNumSeats(), 1);
        Assert.assertEquals(foster.getNumSeats(), 1);
        Assert.assertEquals(jones.getNumSeats(), 1);
        Assert.assertEquals(deutsch.getNumSeats(), 0);
        Assert.assertEquals(borg.getNumSeats(), 0);
        Assert.assertEquals(smith.getNumSeats(), 0);
    }

    /**
     * This test checks that all candidates win seats after an initial round of tie-breaking is performed between
     * Deutsch and Jones. The winner of the tie breaker should win one seat and the loser's number of votes are compared 
     * against Pike's number of seats which eventually leads to Pike and the loser of the inital tie-breaker each winning
     * one seat also.
     */
    @Test
    public void testMultipleWinnersWithSingleTieBreaker(){
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/MPO_test_multiple_winners_with_single_tie_breaker.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();

        // Perform seat allocations
        final MPOVoting mpo = new MPOVoting(candidateLine, ballotList, numSeats, "");
        mpo.performSeatAllocations();

        ArrayList<Candidate> candidates = mpo.getCandidates();
        Candidate pike = candidates.get(2);
        Candidate deutsch = candidates.get(0);
        Candidate jones = candidates.get(1);
        Candidate foster = candidates.get(3);
        Candidate borg = candidates.get(4);
        Candidate smith = candidates.get(5);

        // Ensure votes were calculated correctly for all candidates:
        Assert.assertEquals(pike.getNumVotes(), 2);
        Assert.assertEquals(foster.getNumVotes(), 0);
        Assert.assertEquals(deutsch.getNumVotes(), 3);
        Assert.assertEquals(borg.getNumVotes(), 0);
        Assert.assertEquals(jones.getNumVotes(), 3);
        Assert.assertEquals(smith.getNumVotes(), 0);

        // Ensure only 3 candidates won seats and other 3 didn't win anything.
        Assert.assertEquals(mpo.getWinningCandidates().size(), 3);
        Assert.assertEquals(pike.getNumSeats(), 1);
        Assert.assertEquals(deutsch.getNumSeats(), 1);
        Assert.assertEquals(jones.getNumSeats(), 1);
        Assert.assertEquals(foster.getNumSeats(), 0);
        Assert.assertEquals(borg.getNumSeats(), 0);
        Assert.assertEquals(smith.getNumSeats(), 0);
    }

    /**
     * Test for checking if all candidates receive seats after multiple tie breakers are performed. We also ensure
     * that the last candidate, Jai, receives a seat at the end without fighting with another candidate in a tie breaker.
     */
    @Test
    public void testMultipleWinnersWithMultipleTieBreakers(){
         // Read in test csv file
        final FileParser file = new FileParser("./../testing/MPO_test_multiple_winners_with_multiple_tie_breakers.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();

        // Perform seat allocations
        final MPOVoting mpo = new MPOVoting(candidateLine, ballotList, numSeats, "");
        mpo.performSeatAllocations();

        ArrayList<Candidate> candidates = mpo.getCandidates();
        Candidate pike = candidates.get(1);
        Candidate deutsch = candidates.get(0);
        Candidate jones = candidates.get(2);
        Candidate foster = candidates.get(4);
        Candidate borg = candidates.get(5);
        Candidate smith = candidates.get(3);
        Candidate jai = candidates.get(6);

        // Ensure votes were calculated correctly for all candidates:
        Assert.assertEquals(pike.getNumVotes(), 5);
        Assert.assertEquals(foster.getNumVotes(), 3);
        Assert.assertEquals(deutsch.getNumVotes(), 6);
        Assert.assertEquals(borg.getNumVotes(), 3);
        Assert.assertEquals(jones.getNumVotes(), 5);
        Assert.assertEquals(smith.getNumVotes(), 5);
        Assert.assertEquals(jai.getNumVotes(), 1);

        // Ensure all 7 candidates won a seat.
        Assert.assertEquals(mpo.getWinningCandidates().size(), 7);
        Assert.assertEquals(pike.getNumSeats(), 1);
        Assert.assertEquals(deutsch.getNumSeats(), 1);
        Assert.assertEquals(jones.getNumSeats(), 1);
        Assert.assertEquals(foster.getNumSeats(), 1);
        Assert.assertEquals(borg.getNumSeats(), 1);
        Assert.assertEquals(smith.getNumSeats(), 1);
        Assert.assertEquals(jai.getNumSeats(), 1);
    }
}