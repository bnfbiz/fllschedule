package wifllscheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
// import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
// import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

// Handle Excel Files
public class ExcelFileReader {
    private XSSFWorkbook inputWorkbook;
    private XSSFSheet teamSheet;
    private XSSFSheet tournamentSetupSheet;
    private int teamNumberCol = 1;
    private int teamNameCol = 2;
    private int rowEnd = 0;
    private int rowNumber = 1;

    public ExcelFileReader(String filename) throws IOException {
        FileInputStream file = new FileInputStream(
            new File(filename)
        );
        // Read the workbook and the teamSheet
        inputWorkbook = new XSSFWorkbook(file);
        teamSheet = inputWorkbook.getSheet("Team List");
        tournamentSetupSheet = inputWorkbook.getSheet("TournamentSetup");
    };

    public boolean isEmpty() {
        if (rowNumber > rowEnd) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Team> getTeams() {
        DataFormatter formatter = new DataFormatter();
        ArrayList<Team> teams = new ArrayList<Team>();

        // Process the header row to determine the proper columns to find the data
        rowEnd = teamSheet.getLastRowNum();
        Row headerRow = teamSheet.getRow(0);
        int lastColumn = headerRow.getLastCellNum();
        for (int cn = 0; cn <= lastColumn; cn++) {
            Cell c = headerRow.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (c != null) {
                String cellValue = formatter.formatCellValue(c);
                if (cellValue.equals("Team #")) {
                    teamNumberCol = cn;
                } else if (cellValue.equals("Team Name")) {
                    teamNameCol = cn;
                }
            }
        }

        System.out.println("getTeams: rowNumber = " + rowNumber + " rowEnd = " + rowEnd);
        while(rowNumber <= rowEnd) {
            Row row = teamSheet.getRow(rowNumber);
            Cell teamNumberCell =row.getCell(teamNumberCol, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell teamNameCell = row.getCell(teamNameCol, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String teamNumber = formatter.formatCellValue(teamNumberCell);
            String teamName = formatter.formatCellValue(teamNameCell);
            if ((!teamNumber.isBlank()) && (!teamName.isBlank())) {
                    teams.add(new Team(Integer.parseInt(teamNumber), teamName));
            }
            rowNumber++;
        }
        return teams;
    }

    public Scheduler getSchedulingData() {
        Scheduler scheduleData = new Scheduler();
        DataFormatter formatter = new DataFormatter();
        
        // Get the Judging settings
        rowEnd = tournamentSetupSheet.getLastRowNum();
        int judgingHeaderColumn = 0;
        for (int rn = 0; rn <= rowEnd; rn++) {
            Row row = tournamentSetupSheet.getRow(rn);
            Cell colorCell;
            Cell roomCell;
            Cell timeCell;
            String time;
            Cell c = row.getCell(judgingHeaderColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (c != null) {
                String cellValue = formatter.formatCellValue(c);
                switch (cellValue) {
                        case "Judging Panels":
                        c = row.getCell(judgingHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        int numJudgingRooms = Integer.parseInt(formatter.formatCellValue(c));
                        scheduleData.setJudgingPanelNum(numJudgingRooms);
                        break;
                    case "Judging Timeslots":
                        c = row.getCell(judgingHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        int numJudgingTimeSlots = Integer.parseInt(formatter.formatCellValue(c));
                        scheduleData.setNumberOfJudgingTimes(numJudgingTimeSlots);
                        break;
                    case "Judging Length":
                        timeCell = row.getCell(judgingHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setJudgingDuration(time);
                        break;
                    case "Minimum Judging Discussion":
                        timeCell = row.getCell(judgingHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setJudgingMinimumDiscussionTime(time);
                        break;
                    case "Judging Colors":
                        /**
                         * Loop through the rest of the rows and gather the judging colors (judgingHeaderColumn),
                         * the Judging Rooms (judgingHeaderColumn + 1)
                         * the Judging Times (judgingHeaderColumn + 2)
                         */
                        for (rn++; rn <= rowEnd; rn++) {
                            row = tournamentSetupSheet.getRow(rn);
                            // Process the judging colors
                            colorCell = row.getCell(judgingHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            String color = formatter.formatCellValue(colorCell);
                            
                            // Process the Judging rooms
                            roomCell = row.getCell(judgingHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            String room = formatter.formatCellValue(roomCell);
                            
                            // Process the judging times
                            timeCell = row.getCell(judgingHeaderColumn + 2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            String judgingTime = formatter.formatCellValue(timeCell);
                            scheduleData.addJudgingTime(judgingTime);
                        }
                        break;

                    default:
                        // ignore unknown cells
                        break;
                }
            }
        }

        // Get the robot match settings
        rowEnd = tournamentSetupSheet.getLastRowNum();
        int matchHeaderColumn = 4;
        for (int rn = 0; rn <= rowEnd; rn++) {
            Row row = tournamentSetupSheet.getRow(rn);
            Cell c = row.getCell(matchHeaderColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell timeCell;
            String time;
            if (c != null) {
                String cellValue = formatter.formatCellValue(c);
                switch (cellValue) {
                    case "Day start time":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setDayStartTime(time);
                        break;
                    case "Minimum time between activities":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setMinTimeBetweenActivities(time);
                        break;
                    case "Lunchtime":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setLunchTime(time);
                        break;
                    case "Number of table pairs":
                        c = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        int numberOfTablePairs = Integer.parseInt(formatter.formatCellValue(c));
                        scheduleData.setNumberOfGameTablePairs(numberOfTablePairs);;
                        break;
                    case "Number of concurrent table pairs":
                        // not currently used
                        break;
                    case "Coaches meeting time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setCoachMeetingTime1(time);
                        break;
                    case "Coaches meeting time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setCoachMeetingTime2(time);
                        break;
                    case "Match start to start time":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setMatchAlternateTime(time);
                        break;
                    case "Practice round start time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setPracticeMatchTime1(time);
                        System.out.println("Setting practicematchtime1 " + scheduleData.getPracticeMatchTime1());
                        break;
                    case "Practice round start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setPracticeMatchTime2(time);
                        break;
                    case "Round 1 start time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound1MatchTime1(time);
                        break;
                    case "Round 1 start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound1MatchTime2(time);
                        break;
                    case "Round 2 start time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound2MatchTime1(time);
                        break;
                    case "Round 2 start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound2MatchTime2(time);
                        break;
                    case "Round 3 start time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound3MatchTime1(time);
                        break;
                    case "Round 3 start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound3MatchTime2(time);
                        break;
                    default:
                        // ignore unknown cells
                        break;
                }
            }
        }

        return scheduleData;
    }
}
