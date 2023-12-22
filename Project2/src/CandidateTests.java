import org.junit.*;

public class CandidateTests {

    @Test
    public void testGetName() {
        String candidateName = "Rosen";
        String partyName = "Independent";
        Candidate testCandidate = new Candidate(candidateName, partyName);
        Assert.assertEquals(testCandidate.getName(), candidateName);
    }

    @Test
    public void testGetParty() {
        String candidateName = "Rosen";
        String partyName = "Independent";
        Candidate testCandidate = new Candidate(candidateName, partyName);
        Assert.assertEquals(testCandidate.getParty(), partyName);
    }

    @Test
    public void testNumVotesIncrement() {
        Candidate testCandidate = new Candidate("Kleinberg", "Party A");
        testCandidate.incrementVote();
        testCandidate.incrementVote();
        Assert.assertEquals(testCandidate.getNumVotes(), 2);
    }

    @Test
    public void testIncrementRedistributedVotes() {
        Candidate testCandidate = new Candidate("Kleinberg", "Party A");
        testCandidate.incrementRedistributedVotes();
        testCandidate.incrementRedistributedVotes();
        Assert.assertEquals(testCandidate.getRedistributedVotes(), 2);
    }

    @Test
    public void testResetRedistributedVotes() {
        Candidate testCandidate = new Candidate("Kleinberg", "Party A");
        testCandidate.incrementRedistributedVotes();
        testCandidate.incrementRedistributedVotes();
        testCandidate.resetRedistributedVotes();
        Assert.assertEquals(testCandidate.getRedistributedVotes(), 0);
    }

    @Test
    public void testEliminationStatus() {
        Candidate testCandidate = new Candidate("Kleinberg", "Party A");
        testCandidate.setElimination(true);
        Assert.assertTrue(testCandidate.isEliminated());
        testCandidate.setElimination(false);
        Assert.assertFalse(testCandidate.isEliminated());
    }
}
