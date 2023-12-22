import org.junit.*;

public class PartyTests {
    
    private String partyName = "testParty";

    Party testParty = new Party(partyName);
    Candidate testCandidate = new Candidate("TestCandidate", partyName);
    Candidate testCandidate2 = new Candidate("TestCandidate", partyName);
    Candidate testCandidate3 = new Candidate("TestCandidate", partyName);
    
    @Test
    public void testGetPartyName() {
        Assert.assertEquals(testParty.getPartyName(), partyName);
    }

    @Test
    public void testAddAndGetCandidates() {
        testParty.addCandidate(testCandidate);
        testParty.addCandidate(testCandidate2);
        testParty.addCandidate(testCandidate3);

        Assert.assertEquals(testParty.getCandidates().get(0), testCandidate);
        Assert.assertEquals(testParty.getCandidates().get(1), testCandidate2);
        Assert.assertEquals(testParty.getCandidates().get(2), testCandidate3);
    }
    @Test
    public void testTotalPartyVotes() {
        Party testParty = new Party(partyName);
        testParty.incrementPartyVote();
        testParty.incrementPartyVote();
        
        Assert.assertEquals(testParty.getTotalPartyVotes(), 2);
    }

    @Test
    public void testSetPartyVote() {
        Party testParty = new Party(partyName);
        testParty.setPartyVote(5);
        
        Assert.assertEquals(testParty.getTotalPartyVotes(), 5);
    }

    @Test
    public void testNumSeatsAllocated() {
        Party testParty = new Party(partyName);
        testParty.incrementNumSeatsAllocated(3);
        
        Assert.assertEquals(testParty.getNumSeatsAllocated(), 3);
    }

    @Test
    public void testInitialPartyVotes() {
        Party testParty = new Party(partyName);
        testParty.setInitialPartyVotes(10);
        
        Assert.assertEquals(testParty.getInitialPartyVotes(), 10);
    }
}
