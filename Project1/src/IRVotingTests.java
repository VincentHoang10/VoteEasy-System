import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.junit.*;

import static org.junit.Assert.assertNull;
import java.io.File;

public class IRVotingTests {
    /**
     * This test ensures that Ballots are parsed correctly and candidates are ranked according to a voter's
     * preference for each ballot. For example: If we have 4 candidates: Rosen, Kleinberg, Chou and Royce and 
     * one of the ballots has the form 1,3,2,4, then we should ensure that after the ballot is processed, the
     * ordering of candidates for this ballot is [Rosen,Royce,Kleinberg,Chou].
     */
    @Test
    public void testBallotDistributionOrder(){
        String candidateLine = "Rosen (D), Kleinberg (R), Chou (I), Royce (L)";
        try{
            // Each line of testBallotDistributionOrder.txt has the form <Ballot>:<Expected candidate order>
            File f = new File("./../testing/IR_test_ballot_distribution_order.txt");
            Scanner scanner = new Scanner(f);
            scanner.useDelimiter("\n");
            while(scanner.hasNextLine()){
                // Example of a line from the file: 1,3,4,2;Rosen,Royce,Kleinberg,Chou
                String line = scanner.nextLine();
                String[] parts = line.split(";");

                // Splitting the example line above gives us an array of the form: ["1,3,4,2", "Rosen,Royce,Kleinberg,Chou"]
                String ballot = parts[0];

                // This gives us an array of the form: ["Rosen", "Royce", "Kleinberg", "Chou"]
                String[] candidateNames = parts[1].split(",");
                
                // Parse candidate and ballot information
                ArrayList<String> ballotList = new ArrayList<>(){{add(ballot);}};
                final IRVoting ir = new IRVoting(candidateLine, ballotList, "");

                // Retrieve ballots variable for testing purposes to ensure it was parsed correctly
                ArrayList<ArrayList<Candidate>> ballots = ir.getBallots();

                // The ballots arraylist only contains one arraylist of candidate objects that are ordered by candidate preference
                ArrayList<Candidate> candidates = ballots.get(0);

                for (int candidateIdx = 0; candidateIdx < candidates.size(); candidateIdx++){
                    // If no candidate preference was given for the current position, then ensure that it indeed has a null value
                    if (candidateNames[candidateIdx].equals("null")){
                        assertNull(candidates.get(candidateIdx));
                    }
                    else{
                        // If candidate preference was provided in the current position, then ensure that it is the expected
                        // candidate
                        Assert.assertEquals(candidates.get(candidateIdx).getName(), candidateNames[candidateIdx]);
                    }
                }
            }
            scanner.close();
        } catch (Exception e){
            System.out.println("Test file not found in \"testing\" directory.");
        }
       
    }

    /**
     * This function tests if the correct winner is found when a majority occurs in the first round of
     * calculations itself. 
     */
    @Test
    public void testFirstRoundMajorityWinner() {
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/IR_test_first_round_majority_winner.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();

        // Calculat ballots
        final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
        ir.calculateBallots();

        Assert.assertEquals("Rosen", ir.getWinningCandidate().getName());
        Assert.assertEquals(4, ir.getWinningCandidate().getNumVotes());
    }

    /**
     * For this test case, every voter votes for the same candidate (Rosen) which results in a landslide victory for Rosen.
     */
    @Test
    public void testLandslideMajority(){
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/IR_test_landslide_majority.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();

        // Calculate ballots
        final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
        ir.calculateBallots();

        Assert.assertEquals("Rosen", ir.getWinningCandidate().getName());
        Assert.assertEquals(6, ir.getWinningCandidate().getNumVotes());
    }

    /**
     * For this test case, there is only one candidate for the election, so all the votes go to that candidate
     */
    @Test
    public void testOnlyOneCandidateMajority(){
        // Read in test csv file
        final FileParser file = new FileParser("./../testing/IR_test_only_one_candidate_majority.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();

        // Calculate ballots
        final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
        ir.calculateBallots();

        Assert.assertEquals("Rosen", ir.getWinningCandidate().getName());
        Assert.assertEquals(6, ir.getWinningCandidate().getNumVotes());
    }

    /**
     * This function tests if a two candidate tie is resolved fairly after a first round majority is not found.
     * By "fairly" we mean that we will simulate the tie scenario 1000 times to ensure the randomizer is not 
     * biased towards one candidate. If the percentage of times a candidate wins a tie is between 45-55%, then
     * we can assume that the tie is fair since the chances of it being exactly 50% everytime is quite low.
     */
    @Test
    public void testTwoCandidateTieAfterNoFirstRoundMajority(){
        Map<String, Integer> map = new HashMap<>();

        map.put("Rosen", 0);
        map.put("Kleinberg", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            final FileParser file = new FileParser("./../testing/IR_test_two_candidate_tie.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
            ir.calculateBallots();

            String winningCandidateName = ir.getWinningCandidate().getName();
            map.put(winningCandidateName, map.get(winningCandidateName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 50%
        double low = 0.45;
        double high = 0.55;
        Assert.assertTrue((double)map.get("Rosen")/1000 >= low && (double)map.get("Rosen")/1000 <= high);
        Assert.assertTrue((double)map.get("Kleinberg")/1000 >= low && (double)map.get("Kleinberg")/1000 <= high);
    }

    /**
     * This function tests if a four candidate tie is resolved fairly after a first round majority is not found.
     * By "fairly" we mean that we will simulate the tie scenario 1000 times to ensure the randomizer is not 
     * biased towards one candidate. Every candidate has about a 20-30% chance of winning in order to ensure
     * fairness in the tie breaker.
     */
    @Test
    public void testFourCandidateTieAfterNoFirstRoundMajority(){
        Map<String, Integer> map = new HashMap<>();

        map.put("Rosen", 0);
        map.put("Kleinberg", 0);
        map.put("Chou", 0);
        map.put("Royce", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            final FileParser file = new FileParser("./../testing/IR_test_four_candidate_tie.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
            ir.calculateBallots();

            String winningCandidateName = ir.getWinningCandidate().getName();
            map.put(winningCandidateName, map.get(winningCandidateName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 25%
        double low = 0.20;
        double high = 0.30;
        Assert.assertTrue((double)map.get("Rosen")/1000 >= low && (double)map.get("Rosen")/1000 <= high);
        Assert.assertTrue((double)map.get("Kleinberg")/1000 >= low && (double)map.get("Kleinberg")/1000 <= high);
        Assert.assertTrue((double)map.get("Chou")/1000 >= low && (double)map.get("Chou")/1000 <= high);
        Assert.assertTrue((double)map.get("Royce")/1000 >= low && (double)map.get("Royce")/1000 <= high);
    }

    /**
     * For this test case, candidate Kleinberg is eliminated since they have 0 votes and then Royce is eliminated. 
     * The ballot that had Royce as the first choice candidate is also deleted since there are no other candidates
     * ranked on that ballot. Hence, Rosen wins the election since they have 3/5 votes for a majority.
     */
    @Test
    public void testMajorityWinnerAfterOneRoundRedistribution(){
        final FileParser file = new FileParser("./../testing/IR_test_majority_winner_after_one_round_redistribution.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
        ir.calculateBallots();
        Assert.assertEquals("Rosen", ir.getWinningCandidate().getName());
        Assert.assertEquals(3, ir.getWinningCandidate().getNumVotes());
    }

    /*
     * For this test case, Rosen leads at first, but doesn't have a majority. After redistribution, Kleinberg ends 
     * up winning despite Rosen leading in the beginning after the first round of vote calculations.
     */
    @Test
    public void testOneLeadingCandidateLossAfterRedistribution(){
        final FileParser file = new FileParser("./../testing/IR_test_one_leading_candidate_loss_after_redistribution.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        IRVoting ir = new IRVoting(candidateLine, ballotList, "");
        ir.calculateBallots();
        Assert.assertEquals("Kleinberg", ir.getWinningCandidate().getName());
        Assert.assertEquals(6, ir.getWinningCandidate().getNumVotes());
    }

    /*
     * For this test case, Rosen and Royce are tied at the highest number of votes, but neither have a majority yet. 
     * Chou and Kleinberg are tied at the lowest number of votes. Kleinberg gets eliminated and their votes are transferred 
     * to Chou who ends up winning the election.
     */
    @Test
    public void testTwoLeadingCandidateLossAfterRedistribution(){
        final FileParser file = new FileParser("./../testing/IR_test_two_leading_candidate_loss_after_redistribution.csv");
        final String candidateLine = file.getCandidateLine();
        final ArrayList<String> ballotList = file.getBallotList();
        IRVoting ir = new IRVoting(candidateLine, ballotList, "");
        ir.calculateBallots();
        Assert.assertEquals("Chou", ir.getWinningCandidate().getName());
        Assert.assertEquals(6, ir.getWinningCandidate().getNumVotes());
    }

    /**
     * Test for when a tie is found after one round of redistribution itself. We ensure that each candidate has a fair chance 
     * of winning a tie and thus perform the tie 1000 times. Every tied candidate has about a 45-55% chance of winning in order 
     * to ensure fairness in the tie breaker.
     */
    @Test
    public void testCandidateTieAfterOneRoundOfRedistribution(){
        Map<String, Integer> map = new HashMap<>();
        map.put("Rosen", 0);
        map.put("Chou", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            final FileParser file = new FileParser("./../testing/IR_test_candidate_tie_after_one_round_of_redistribution.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
            ir.calculateBallots();

            String winningCandidateName = ir.getWinningCandidate().getName();
            map.put(winningCandidateName, map.get(winningCandidateName) + 1);
        }
        
        // Assume a plus or minus 5% tolerance from 50%
        double low = 0.45;
        double high = 0.55;
        Assert.assertTrue((double)map.get("Rosen")/1000 >= low && (double)map.get("Rosen")/1000 <= high);
        Assert.assertTrue((double)map.get("Chou")/1000 >= low && (double)map.get("Chou")/1000 <= high);
    }

    /**
     * Test for when a tie is found after multiple rounds of redistribution. We ensure that each candidate has a fair chance 
     * of winning a tie and thus perform the tie 1000 times. Every tied candidate has about a 45-55% chance of winning in order 
     * to ensure fairness in the tie breaker.
     */
    @Test
    public void testCandidateTieAfterMultipleRoundsOfRedistribution(){
        Map<String, Integer> map = new HashMap<>();
        map.put("Rosen", 0);
        map.put("Kleinberg", 0);

        // Simulate tie 1000 times
        for (int i = 0; i < 1000; i++){
            final FileParser file = new FileParser("./../testing/IR_test_candidate_tie_after_multiple_rounds_of_redistribution.csv");
            final String candidateLine = file.getCandidateLine();
            final ArrayList<String> ballotList = file.getBallotList();
            final IRVoting ir = new IRVoting(candidateLine, ballotList, "");
            ir.calculateBallots();

            String winningCandidateName = ir.getWinningCandidate().getName();
            map.put(winningCandidateName, map.get(winningCandidateName) + 1);
        }

        // Assume a plus or minus 5% tolerance from 50%
        double low = 0.45;
        double high = 0.55;
        Assert.assertTrue((double)map.get("Rosen")/1000 >= low && (double)map.get("Rosen")/1000 <= high);
        Assert.assertTrue((double)map.get("Kleinberg")/1000 >= low && (double)map.get("Kleinberg")/1000 <= high);
    }
}