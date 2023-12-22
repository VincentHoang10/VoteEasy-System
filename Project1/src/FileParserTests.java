import org.junit.*;
import java.util.ArrayList;

public class FileParserTests {

    @Test
    public void fileHeaderTest(){
        String filename1 = "./../testing/test_file_parser_IR_file.csv";
        String filename2 = "./../testing/test_file_parser_OPL_file.csv";
        FileParser ir = new FileParser(filename1);
        FileParser opl = new FileParser(filename2);
        Assert.assertEquals(ir.getFileHeader(), "IR");
        Assert.assertEquals(opl.getFileHeader(), "OPL");
    }

    @Test
    public void numberOfCandidatesTest(){
        String filename1 = "./../testing/test_file_parser_IR_file.csv";
        String filename2 = "./../testing/test_file_parser_OPL_file.csv";
        FileParser ir = new FileParser(filename1);
        FileParser opl = new FileParser(filename2);
        Assert.assertEquals(ir.getNumberOfCandidates(), 4);
        Assert.assertEquals(opl.getNumberOfCandidates(), 6);
    }

    @Test
    public void candidateLineTest(){
        String filename1 = "./../testing/test_file_parser_IR_file.csv";
        String filename2 = "./../testing/test_file_parser_OPL_file.csv";
        FileParser ir = new FileParser(filename1);
        FileParser opl = new FileParser(filename2);
        Assert.assertEquals(ir.getCandidateLine(), "Rosen (D), Kleinberg (R), Chou (I), Royce (L)");
        Assert.assertEquals(opl.getCandidateLine(), "Pike (D), Foster (D), Deutsch (R), Borg (R), Jones (R), Smith (I)");
    }

    @Test
    public void numberOfBallotsTest(){
        String filename1 = "./../testing/test_file_parser_IR_file.csv";
        String filename2 = "./../testing/test_file_parser_OPL_file.csv";
        FileParser ir = new FileParser(filename1);
        FileParser opl = new FileParser(filename2);
        Assert.assertEquals(ir.getNumberOfBallots(), 6);
        Assert.assertEquals(opl.getNumberOfBallots(), 9);
    }

    @Test
    public void numberOfSeatsTest(){
        String filename1 = "./../testing/test_file_parser_IR_file.csv";
        String filename2 = "./../testing/test_file_parser_OPL_file.csv";
        FileParser ir = new FileParser(filename1);
        FileParser opl = new FileParser(filename2);
        Assert.assertEquals(ir.getNumberOfSeats(), 0);
        Assert.assertEquals(opl.getNumberOfSeats(), 3);
    }

    @Test
    public void ballotListTest(){
        String filename1 = "./../testing/test_file_parser_IR_file.csv";
        String filename2 = "./../testing/test_file_parser_OPL_file.csv";
        FileParser ir = new FileParser(filename1);
        FileParser opl = new FileParser(filename2);

        ArrayList<String> irballotList = new ArrayList<String>();
        irballotList.add("1,3,4,2");
        irballotList.add("1,,2,");
        irballotList.add("1,2,3,");
        irballotList.add("3,2,1,4");
        irballotList.add(",,1,2");
        irballotList.add(",,,1");

        ArrayList<String> oplballotList = new ArrayList<String>();
        oplballotList.add("1,,,,,");
        oplballotList.add("1,,,,,");
        oplballotList.add(",1,,,,");
        oplballotList.add(",,,,1,");
        oplballotList.add(",,,,,1");
        oplballotList.add(",,,1,,");
        oplballotList.add(",,,1,,");
        oplballotList.add("1,,,,,");
        oplballotList.add(",1,,,,");

        Assert.assertEquals(ir.getBallotList(), irballotList);
        Assert.assertEquals(opl.getBallotList(), oplballotList);
    }

}
