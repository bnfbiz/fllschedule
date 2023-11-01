/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package wifllscheduler;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;

public class App {
    public String getGreeting() {
        return "wifflscheduler";
    }
    
    public static void main(String[] args) {
        
        ExcelFileReader excelFile = null;
        TeamList teams;
        Schedule schedule;
        String inputFilename = "";
        Scheduler scheduler;
    
        
        // Process the command line
        Options options = new Options();
        options.addOption("i", "inputFile", true, "The name of the XLS Input file");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = commandLineParser.parse(options, args);
            if (cmd.hasOption("i")) {
                inputFilename = cmd.getOptionValue("i");
            }
        } catch ( Exception e) {
            System.out.println("Got exception " + e);
            System.exit(-1);
        }

        if (inputFilename.length() <= 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("wifllscheduler", options);
            System.exit(-1);
        }
        System.out.println("Processing the file: " + inputFilename);

        // Read the Excel file
        try {
            excelFile = new ExcelFileReader(inputFilename);
        } catch ( Exception e) {
            System.out.println("Got exception" + e);
            System.exit(-1);
        }

        scheduler = excelFile.getSchedulingData();
        teams = new TeamList(24);
        System.out.println(new App().getGreeting());
        // scheduler.setDayStartTime(8, 0);
        // scheduler.setCoachMeetingTime1(8, 0);
        // scheduler.setMinTimeBetweenActivities(1,0);
        // scheduler.setLunchTime(11, 30);
        // scheduler.addJudgingTime(9, 0);
        // scheduler.addJudgingTime(10, 0);
        // scheduler.addJudgingTime(10, 45);
        // scheduler.addJudgingTime(12, 30);
        // scheduler.addJudgingTime(13, 45);
        // scheduler.addJudgingTime(14, 30);
        // scheduler.setPracticeMatchTime1(9, 0);
        // scheduler.setPracticeMatchTime2(10,30);
        // scheduler.setRound1MatchTime1(10,45);
        // scheduler.setRound1MatchTime2(12,30);
        // scheduler.setRound2MatchTime1(12,30);
        // scheduler.setRound2MatchTime2(13,30);
        // scheduler.setRound3MatchTime1(13,30);
        // scheduler.setRound3MatchTime2(14,30);
        // scheduler.setMatchAlternateTime(5);
        System.out.println(scheduler);
        System.out.println("Got teams: " + teams.toString());

        // Schedule the judging times
        schedule = new Schedule(scheduler, teams);
        System.out.println(schedule);
    }
}