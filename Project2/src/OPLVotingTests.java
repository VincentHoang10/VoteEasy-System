import java.util.ArrayList;
import org.junit.*;
import java.util.Map;
import java.util.HashMap;

public class OPLVotingTests {
    /**
     * This function tests if the correct winner for a party and candidate is found for a smaller set of ballots
     * with no ties occurring in this situation.
     */
    @Test
    public void testSmallNumberOfVotes() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/OPL_test_small_number_of_votes.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();
        final int numBallots = file.getNumberOfBallots();

        // Perform seat allocations
        final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
        opl.performSeatAllocations();

        Assert.assertEquals("D", opl.getWinningParty().getPartyName());
        Assert.assertEquals(2, opl.getWinningParty().getNumSeatsAllocated());
        Assert.assertEquals(5, opl.getWinningParty().getInitialPartyVotes());
        Assert.assertEquals("Pike", opl.getWinningCandidate().getName());
        Assert.assertEquals(3, opl.getWinningCandidate().getNumVotes());
    }

    /**
     * This function tests if the correct winner for a party and candidate is found where a party will
     * only receive a maximum number of seats equal to its number of candidates with no ties occurring 
     * in this situation.
     */
    @Test
    public void testPartyWithSeatsEqualToCandidates() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/OPL_test_party_with_seats_equal_to_candidates.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();
        final int numBallots = file.getNumberOfBallots();

        // Perform seat allocations
        final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
        opl.performSeatAllocations();

        Assert.assertEquals("D", opl.getWinningParty().getPartyName());
        Assert.assertEquals(2, opl.getWinningParty().getNumSeatsAllocated());
        Assert.assertEquals(6, opl.getWinningParty().getInitialPartyVotes());
        Assert.assertEquals("Foster", opl.getWinningCandidate().getName());
        Assert.assertEquals(4, opl.getWinningCandidate().getNumVotes());
    }

    /**
     * For this test case, all the votes go towards a single party, with no ties occurring between the candidates
     * and remaining votes between parties.
     */
    @Test
    public void testAllVotesToOneParty() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/OPL_test_all_votes_to_one_party.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();
        final int numBallots = file.getNumberOfBallots();

        // Perform seat allocations
        final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
        opl.performSeatAllocations();

        Assert.assertEquals("D", opl.getWinningParty().getPartyName());
        Assert.assertEquals(3, opl.getWinningParty().getNumSeatsAllocated());
        Assert.assertEquals(9, opl.getWinningParty().getInitialPartyVotes());
        Assert.assertEquals("Foster", opl.getWinningCandidate().getName());
        Assert.assertEquals(5, opl.getWinningCandidate().getNumVotes());
    }

    /**
     * For this test case, all the votes go towards a single candidate, with no ties occurring between parties 
     * and remaining votes between parties.
     */
    @Test
    public void testAllVotesToOneCandidate() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/OPL_test_all_votes_to_one_candidate.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();
        final int numBallots = file.getNumberOfBallots();

        // Perform seat allocations
        final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
        opl.performSeatAllocations();

        Assert.assertEquals("R", opl.getWinningParty().getPartyName());
        Assert.assertEquals(3, opl.getWinningParty().getNumSeatsAllocated());
        Assert.assertEquals(9, opl.getWinningParty().getInitialPartyVotes());
        Assert.assertEquals("Borg", opl.getWinningCandidate().getName());
        Assert.assertEquals(9, opl.getWinningCandidate().getNumVotes());
    }

    /**
     * This function tests if the correct winner for a party and candidate is found after the first round of seat
     * allocations with no ties occurring in this situation.
     */
    @Test
    public void testFirstRoundWinner() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/OPL_test_first_round_winner.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();
        final int numBallots = file.getNumberOfBallots();

        // Perform seat allocations
        final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
        opl.performSeatAllocations();

        Assert.assertEquals("D", opl.getWinningParty().getPartyName());
        Assert.assertEquals(2, opl.getWinningParty().getNumSeatsAllocated());
        Assert.assertEquals(6, opl.getWinningParty().getInitialPartyVotes());
        Assert.assertEquals("Pike", opl.getWinningCandidate().getName());
        Assert.assertEquals(4, opl.getWinningCandidate().getNumVotes());
    }

    /**
     * This function tests if the correct winner for a party and candidate is found where there are more than 
     * two rounds of seat allocations with a tie occurring in this situation.
     */
    @Test
    public void testMoreThanTwoRoundsAllocations() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/OPL_test_more_than_two_rounds_allocations.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final int numSeats = file.getNumberOfSeats();
        final int numBallots = file.getNumberOfBallots();

        // Perform seat allocations
        final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
        opl.performSeatAllocations();

        Assert.assertTrue((opl.getWinningParty().getPartyName().equals("D") && opl.getWinningCandidate().getName().equals("Pike")) 
                            || (opl.getWinningParty().getPartyName().equals("R") && opl.getWinningCandidate().getName().equals("Deutsch")) 
                            || (opl.getWinningParty().getPartyName().equals("I") && opl.getWinningCandidate().getName().equals("Jones")));
        Assert.assertEquals(3, opl.getWinningParty().getNumSeatsAllocated());
        Assert.assertEquals(3, opl.getWinningParty().getInitialPartyVotes());
        Assert.assertEquals(2, opl.getWinningCandidate().getNumVotes());
    }

    /**
     * This function tests if a two candidate tie is resolved fairly after the seat allocations and a party winner
     * has been determined, with no parties or remaining votes tie occurring. By "fairly" we mean that we will simulate 
     * the tie scenario 1000 times to ensure the randomizer is not biased towards one candidate. If the percentage of times 
     * a candidate wins a tie is between 45-55%, then we can assume that the tie is fair since the chances of it being exactly 
     * 50% everytime is quite low.
     */
    @Test
    public void testTwoTiedCandidates() {
        Map<String, Integer> map = new HashMap<>();

        map.put("Pike", 0);
        map.put("Foster", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            // Read in test csv file
            final FileParser file = new FileParser("./../testing/OPL_test_two_tied_candidates.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();

            // Perform seat allocations
            final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
            opl.performSeatAllocations();

            String winningCandidateName = opl.getWinningCandidate().getName();
            map.put(winningCandidateName, map.get(winningCandidateName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 50%
        double low = 0.45;
        double high = 0.55;
        Assert.assertTrue((double)map.get("Pike")/1000 >= low && (double)map.get("Pike")/1000 <= high);
        Assert.assertTrue((double)map.get("Foster")/1000 >= low && (double)map.get("Foster")/1000 <= high);
    }

    /**
     * This function tests if a four candidate tie is resolved fairly after the seat allocations and a party winner
     * has been determined, with no parties or remaining votes tie occurring. By "fairly" we mean that we will 
     * simulate the tie scenario 1000 times to ensure the randomizer is not biased towards one candidate. Every 
     * candidate has about a 20-30% chance of winning in order to ensure fairness in the tie breaker.
     */
    @Test
    public void testFourTiedCandidates() {
        Map<String, Integer> map = new HashMap<>();

        map.put("Foster", 0);
        map.put("Deutsch", 0);
        map.put("Borg", 0);
        map.put("Jones", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            // Read in test csv file
            final FileParser file = new FileParser("./../testing/OPL_test_four_tied_candidates.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();

            // Perform seat allocations
            final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
            opl.performSeatAllocations();

            String winningCandidateName = opl.getWinningCandidate().getName();
            map.put(winningCandidateName, map.get(winningCandidateName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 25%
        double low = 0.20;
        double high = 0.30;
        Assert.assertTrue((double)map.get("Foster")/1000 >= low && (double)map.get("Foster")/1000 <= high);
        Assert.assertTrue((double)map.get("Deutsch")/1000 >= low && (double)map.get("Deutsch")/1000 <= high);
        Assert.assertTrue((double)map.get("Borg")/1000 >= low && (double)map.get("Borg")/1000 <= high);
        Assert.assertTrue((double)map.get("Jones")/1000 >= low && (double)map.get("Jones")/1000 <= high);
    }

    /**
     * This function tests if two most remaining vote ties are resolved fairly after the first round of seat allocations, 
     * with no parties or candidates tie occurring. By "fairly" we mean that we will simulate the tie scenario 1000 times 
     * to ensure the randomizer is not biased towards one party to be allocated a remaining seat. If the percentage 
     * of times a party wins a tie to be allocated a remaining seat is between 45-55%, then we can assume that the tie 
     * is fair since the chances of it being exactly 50% everytime is quite low.
     */
    @Test
    public void testTwoRemainderTies() {
        Map<String, Integer> map = new HashMap<>();

        map.put("D", 0);
        map.put("R", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            // Read in test csv file
            final FileParser file = new FileParser("./../testing/OPL_test_two_remainder_ties.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();

            // Perform seat allocations
            final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
            opl.performSeatAllocations();

            String winningPartyName = opl.getWinningParty().getPartyName();
            map.put(winningPartyName, map.get(winningPartyName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 50%
        double low = 0.45;
        double high = 0.55;
        Assert.assertTrue((double)map.get("D")/1000 >= low && (double)map.get("D")/1000 <= high);
        Assert.assertTrue((double)map.get("R")/1000 >= low && (double)map.get("R")/1000 <= high);
    }

    /**
     * This function tests if four most remaining vote ties are resolved fairly after the first round of seat allocations, 
     * with no parties or candidates tie occurring. By "fairly" we mean that we will simulate the tie scenario 1000 times 
     * to ensure the randomizer is not biased towards one party to be allocated a remaining seat. Every party has about 
     * a 20-30% chance of being allocated a remaining seat in order to ensure fairness in the tie breaker.
     */
    @Test
    public void testFourRemainderTies() {
        Map<String, Integer> map = new HashMap<>();

        map.put("D", 0);
        map.put("R", 0);
        map.put("G", 0);
        map.put("I", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            // Read in test csv file
            final FileParser file = new FileParser("./../testing/OPL_test_four_remainder_ties.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();

            // Perform seat allocations
            final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
            opl.performSeatAllocations();

            String winningPartyName = opl.getWinningParty().getPartyName();
            map.put(winningPartyName, map.get(winningPartyName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 25%
        double low = 0.20;
        double high = 0.30;
        Assert.assertTrue((double)map.get("D")/1000 >= low && (double)map.get("D")/1000 <= high);
        Assert.assertTrue((double)map.get("R")/1000 >= low && (double)map.get("R")/1000 <= high);
        Assert.assertTrue((double)map.get("G")/1000 >= low && (double)map.get("G")/1000 <= high);
        Assert.assertTrue((double)map.get("I")/1000 >= low && (double)map.get("I")/1000 <= high);
    }

    /**
     * This function tests if a two party tie is resolved fairly after the seat allocations and a party winner is being
     * determined, with no candidates or remaining votes tie occurring. By "fairly" we mean that we will simulate the tie 
     * scenario 1000 times to ensure the randomizer is not biased towards one party. If the percentage of times a party 
     * wins a tie is between 45-55%, then we can assume that the tie is fair since the chances of it being exactly 50% 
     * everytime is quite low.
     */
    @Test
    public void testTwoTiedParties() {
        Map<String, Integer> map = new HashMap<>();

        map.put("D", 0);
        map.put("R", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            // Read in test csv file
            final FileParser file = new FileParser("./../testing/OPL_test_two_tied_parties.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();

            // Perform seat allocations
            final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
            opl.performSeatAllocations();

            String winningPartyName = opl.getWinningParty().getPartyName();
            map.put(winningPartyName, map.get(winningPartyName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 50%
        double low = 0.45;
        double high = 0.55;
        Assert.assertTrue((double)map.get("D")/1000 >= low && (double)map.get("D")/1000 <= high);
        Assert.assertTrue((double)map.get("R")/1000 >= low && (double)map.get("R")/1000 <= high);
    }

    /**
     * This function tests if a four party tie is resolved fairly after the seat allocations and a party winner is being
     * determined, with no candidates or remaining votes tie occurring. By "fairly" we mean that we will simulate the tie 
     * scenario 1000 times to ensure the randomizer is not biased towards one party. Every party has about a 20-30% chance 
     * of winning in order to ensure fairness in the tie breaker. 
     */
    @Test
    public void testFourTiedParties() {
        Map<String, Integer> map = new HashMap<>();

        map.put("D", 0);
        map.put("R", 0);
        map.put("G", 0);
        map.put("I", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            // Read in test csv file
            final FileParser file = new FileParser("./../testing/OPL_test_four_tied_parties.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final int numSeats = file.getNumberOfSeats();
            final int numBallots = file.getNumberOfBallots();

            // Perform seat allocations
            final OPLVoting opl = new OPLVoting(candidateLine, ballotList, numSeats, numBallots, "");
            opl.performSeatAllocations();

            String winningPartyName = opl.getWinningParty().getPartyName();
            map.put(winningPartyName, map.get(winningPartyName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 25%
        double low = 0.20;
        double high = 0.30;
        Assert.assertTrue((double)map.get("D")/1000 >= low && (double)map.get("D")/1000 <= high);
        Assert.assertTrue((double)map.get("R")/1000 >= low && (double)map.get("R")/1000 <= high);
        Assert.assertTrue((double)map.get("G")/1000 >= low && (double)map.get("G")/1000 <= high);
        Assert.assertTrue((double)map.get("I")/1000 >= low && (double)map.get("I")/1000 <= high);
    }
}
