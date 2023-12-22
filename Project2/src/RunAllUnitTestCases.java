import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

// compile with: javac -cp ./../lib/junit-4.13.2.jar:. RunAllUnitTestCases.java
// run with:     java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunAllUnitTestCases <Candidate|Party|FileParser|IRVoting|OPLVoting|MPOVoting>
public class RunAllUnitTestCases {
    public static void main(String[] args){
        if (args.length == 1){
            String className = args[0].toLowerCase();
            switch (className){
                case "candidate":
                    runTests(CandidateTests.class, "Candidate");
                    break;
                case "party":
                    runTests(PartyTests.class, "Party");
                    break;
                case "fileparser":
                    runTests(FileParserTests.class, "FileParser");
                    break;
                case "irvoting":
                    runTests(IRVotingTests.class, "IRVoting");
                    break;
                case "oplvoting":
                    runTests(OPLVotingTests.class, "OPLVoting");
                    break;
                case "mpovoting":
                    runTests(MPOVotingTests.class, "MPOVoting");
                    break;
                default:
                    System.out.println("[SYSTEM]: You can only run tests from Candidate, Party, FileParser, IRVoting and OPLVoting classes.");
                    printUsageList();
                    System.exit(0);
            }
            
        }
        else if (args.length == 0){
            runTests(FileParserTests.class, "FileParser");
            runTests(CandidateTests.class, "Candidate");
            runTests(PartyTests.class, "Party");
            runTests(IRVotingTests.class, "IRVoting");
            runTests(OPLVotingTests.class, "OPLVoting");
            runTests(MPOVotingTests.class, "MPOVoting");
        }
        else{
            System.out.println("[SYSTEM]: You can enter only one argument.");
            printUsageList();
            System.exit(0);
        }
    }

    private static void printUsageList(){
        System.out.println("[SYSTEM]: Compile with: javac -cp ./../lib/junit-4.13.2.jar:. RunAllUnitTestCases.java");
        System.out.println("[SYSTEM]: Run with: java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunAllUnitTestCases <Candidate | Party | FileParser | IRVoting | OPLVoting | MPOVoting>");
    }

    private static void runTests(Class<?> classType, String className){
        Result result = JUnitCore.runClasses(classType);
        if (result.wasSuccessful()) {
            System.out.printf("All %d tests passed successfully for class %s.\n", result.getRunCount(), className);
        }
        else {
            System.out.printf("%d tests failed for class %s.\n", result.getRunCount(), className);
        }
    }
}
