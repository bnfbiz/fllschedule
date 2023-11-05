package wifllscheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wifllscheduler.ScheduleSlot.SlotType;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
// import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
// import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

// Handle Excel Files
public class ExcelFileReader {
    private XSSFWorkbook inputWorkbook;
    private XSSFSheet teamSheet;
    private XSSFSheet tournamentSetupSheet;
    private int teamNumberCol = 1;
    private int teamNameCol = 2;
    private int rowEnd = 0;
    private int rowNumber = 1;
    private ArrayList<String> judingColorTable = new ArrayList<String>();
    private ArrayList<String> matchColorTable = new ArrayList<String>();

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
            XSSFRow row = tournamentSetupSheet.getRow(rn);
            XSSFCell colorCell;
            XSSFCell roomCell;
            XSSFCell timeCell;
            String time;
            XSSFCell c = row.getCell(judgingHeaderColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
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
                            String color;

                            row = tournamentSetupSheet.getRow(rn);
                            // Process the judging colors
                            colorCell = row.getCell(judgingHeaderColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            color = formatter.formatCellValue(colorCell);
                            judingColorTable.add(color);
                            
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
            XSSFRow row = tournamentSetupSheet.getRow(rn);
            XSSFCell c = row.getCell(matchHeaderColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            XSSFCell timeCell;
            XSSFCell colorCell;
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
                        scheduleData.setCellLocation(SlotType.COACHES_MEETING_TIME1, "TournamentSetup!F" + rn);
                        break;
                    case "Coaches meeting time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setCoachMeetingTime2(time);
                        scheduleData.setCellLocation(SlotType.COACHES_MEETING_TIME2, "TournamentSetup!F" + rn);
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
                        scheduleData.setCellLocation(SlotType.PRACTICE_MATCH_TIME1, "TournamentSetup!F" + rn);
                        break;
                    case "Practice round start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setPracticeMatchTime2(time);
                        scheduleData.setCellLocation(SlotType.PRACTICE_MATCH_TIME2, "TournamentSetup!F" + rn);
                        break;
                    case "Round 1 start time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound1MatchTime1(time);
                        scheduleData.setCellLocation(SlotType.COMPETITION_MATCH1_TIME1, "TournamentSetup!F" + rn);
                        break;
                    case "Round 1 start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound1MatchTime2(time);
                        scheduleData.setCellLocation(SlotType.COMPETITION_MATCH1_TIME2, "TournamentSetup!F" + rn);
                        break;
                    case "Round 2 start time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound2MatchTime1(time);
                        scheduleData.setCellLocation(SlotType.COMPETITION_MATCH2_TIME1, "TournamentSetup!F" + rn);
                        break;
                    case "Round 2 start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound2MatchTime2(time);
                        scheduleData.setCellLocation(SlotType.COMPETITION_MATCH2_TIME2, "TournamentSetup!F" + rn);
                        break;
                    case "Round 3 start time 1":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound3MatchTime1(time);
                        scheduleData.setCellLocation(SlotType.COMPETITION_MATCH3_TIME1, "TournamentSetup!F" + rn);
                        break;
                    case "Round 3 start time 2":
                        timeCell = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        time = formatter.formatCellValue(timeCell);
                        scheduleData.setRound3MatchTime2(time);
                        scheduleData.setCellLocation(SlotType.COMPETITION_MATCH3_TIME2, "TournamentSetup!F" + rn);
                        break;
                    case "Table colors":
                        System.out.println("Processing Table colors");
                        for (rn++; rn <= rowEnd; rn++) {
                            String color;

                            row = tournamentSetupSheet.getRow(rn);
                            // Process the judging colors
                            colorCell = row.getCell(matchHeaderColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            color = formatter.formatCellValue(colorCell);
                            if (color.length() > 0) {
                                matchColorTable.add(color + " A");
                                matchColorTable.add(color + " B");
                            }
                        }
                        break;
                    default:
                        // ignore unknown cells
                        break;
                }
            }
        }

        return scheduleData;
    }

    public void updateScheduleTab(Schedule scheduleInfo) {

        String ret = "";
        
        // delete the schedule sheet and recreate it
        int sheetIndex = inputWorkbook.getSheetIndex("Schedule");
        inputWorkbook.removeSheetAt(sheetIndex);
        XSSFSheet sheet = inputWorkbook.createSheet("Schedule");
        // put the sheet back in the same position in the spreadsheet
        inputWorkbook.setSheetOrder("Schedule", sheetIndex);
        // inputWorkbook.setActiveSheet(sheetIndex);

        int teamCount = scheduleInfo.getTeamCount();
        // Create a row for each team and the header
        XSSFRow rows[];

        rows = new XSSFRow[scheduleInfo.getTeamCount()+2];
        for (int c = 0; c < teamCount+1; c++) {
            int teamRow = c + 1;
            rows[c]= sheet.createRow(c);
            if (c > 0) {
                String formula;
                XSSFCell cell = rows[c].createCell(0);
                formula = "_xlfn.CONCAT(Team List!B" + teamRow + ", \" - \" , Team List!C" + teamRow + ")";
                cell.setCellFormula(formula);
                XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                formulaEvaluator.evaluateFormulaCell(cell);
            }
        }

        // add in the wild card team
        rows[teamCount+1] = sheet.createRow(teamCount+1);
        rows[teamCount+1].createCell(0).setCellValue("Wild Card");
        rows[0].createCell(0).setCellValue("Team");
        rows[0].createCell(1).setCellValue("Coach Meeting");
        rows[0].createCell(2).setCellValue("Judging Start");
        rows[0].createCell(3).setCellValue("Judging Color");
        rows[0].createCell(4).setCellValue("Practice Time");
        rows[0].createCell(5).setCellValue("Practice Table");
        rows[0].createCell(6).setCellValue("Round 1 Time");
        rows[0].createCell(7).setCellValue("Round 1 Table");
        rows[0].createCell(8).setCellValue("Round 2 Time");
        rows[0].createCell(9).setCellValue("Round 2 Table");
        rows[0].createCell(10).setCellValue("Round 3 Time");
        rows[0].createCell(11).setCellValue("Round 3 Table");

        // Process the teams
        for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
            for (int team = 0; team < teamCount; team++) {
                try {
                    // Create a row and put some cells in it. Rows are 0 based.
                    // Create a cell and put a value in it.
                    ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                    int row = team + 1;

                    if (slot.isCoachMeetingSlot()) {
                        XSSFCell cell = rows[row].createCell(1);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                    } else if (slot.isJudgingSlot()) {
                        XSSFCell cell = rows[row].createCell(2);
                        // cell.setCellValue(scheduleInfo.getTimeForSlot(t).format(DateTimeFormatter.ofPattern("hh:mm a")));
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        XSSFCell cell2 = rows[row].createCell(3);
                        int judgingLocation = slot.getJudgingIndex();
                        cell2.setCellValue(judingColorTable.get(judgingLocation));
                    } else if (slot.isPracticeMatch()) {
                        XSSFCell cell = rows[row].createCell(4);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        XSSFCell cell2 = rows[row].createCell(5);
                        int matchLocation = slot.getTableIndex();
                        cell2.setCellValue(matchColorTable.get(matchLocation));
                    } else if (slot.isMatch1()) {
                        XSSFCell cell = rows[row].createCell(6);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        XSSFCell cell2 = rows[row].createCell(7);
                        int matchLocation = slot.getTableIndex();
                        cell2.setCellValue(matchColorTable.get(matchLocation));
                    } else if (slot.isMatch2()) {
                        XSSFCell cell = rows[row].createCell(8);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        XSSFCell cell2 = rows[row].createCell(9);
                        int matchLocation = slot.getTableIndex();
                        cell2.setCellValue(matchColorTable.get(matchLocation));
                        cell2.setCellValue(matchColorTable.get(matchLocation));
                    } else if (slot.isMatch3()) {
                        XSSFCell cell = rows[row].createCell(10);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        XSSFCell cell2 = rows[row].createCell(11);
                        int matchLocation = slot.getTableIndex();
                        cell2.setCellValue(matchColorTable.get(matchLocation));
                    }
                }
                catch (NullPointerException e){
                    // timeslot doesn't exist so stop processing
                    // t = timeSlots;
                    // team = teamCount;
                }
            }
        }

        // handle the wildcard team which can have multiple matches per round
        String practiceMatchTimes = "";
        String practiceMatchTables = "";
        String comp1MatchTimes = "";
        String comp1MatchTables = "";
        String comp2MatchTimes = "";
        String comp2MatchTables = "";
        String comp3MatchTimes = "";
        String comp3MatchTables = "";
        CellStyle cellStyle = inputWorkbook.createCellStyle();
        cellStyle.setWrapText(true);
        
        for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
            int team = teamCount;
            int row = team + 1;
            try {
                ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                if (slot.isPracticeMatch()) {
                    int matchLocation = slot.getTableIndex();
                    if (practiceMatchTimes.length() > 0) {
                        practiceMatchTimes = practiceMatchTimes + "\n" + slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        practiceMatchTables = practiceMatchTables + "\n" + matchColorTable.get(matchLocation);
                    } else {
                        practiceMatchTimes = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        practiceMatchTables = matchColorTable.get(matchLocation);
                    }
                    XSSFCell cell = rows[row].createCell(4);
                    XSSFCell cell2 = rows[row].createCell(5);
                    cell.setCellValue(practiceMatchTimes);
                    cell2.setCellValue(practiceMatchTables);

                    // turn on word wrap
                    cell.setCellStyle(cellStyle);
                    cell2.setCellStyle(cellStyle);
                } else if (slot.isMatch1()) {
                    int matchLocation = slot.getTableIndex();
                    if (comp1MatchTables.length() > 0) {
                        comp1MatchTimes = comp1MatchTimes + "\n" + slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        comp1MatchTables = comp1MatchTables + "\n" + matchColorTable.get(matchLocation);
                    } else {                        
                        comp1MatchTimes = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        comp1MatchTables = matchColorTable.get(matchLocation);
                    }
                    XSSFCell cell = rows[row].createCell(6);
                    XSSFCell cell2 = rows[row].createCell(7);
                    cell.setCellValue(comp1MatchTimes);
                    cell2.setCellValue(comp1MatchTables);

                    // turn on word wrap
                    cell.setCellStyle(cellStyle);
                    cell2.setCellStyle(cellStyle);
                } else if (slot.isMatch2()) {
                    int matchLocation = slot.getTableIndex();
                    if (comp2MatchTimes.length() > 0) {                        
                        comp2MatchTimes = comp2MatchTimes + "\n" + slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        comp2MatchTables = comp2MatchTables + "\n" + matchColorTable.get(matchLocation);
                    } else {
                        comp2MatchTimes = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        comp2MatchTables = matchColorTable.get(matchLocation);
                    }
                    XSSFCell cell = rows[row].createCell(8);
                    XSSFCell cell2 = rows[row].createCell(9);
                    cell.setCellValue(comp2MatchTimes);
                    cell2.setCellValue(comp2MatchTables);

                    // turn on word wrap
                    cell.setCellStyle(cellStyle);
                    cell2.setCellStyle(cellStyle);
                } else if (slot.isMatch3()) {
                    int matchLocation = slot.getTableIndex();
                    if (comp3MatchTimes.length() > 0) {
                        comp3MatchTimes = comp3MatchTimes + "\n" + slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        comp3MatchTables = comp3MatchTables + "\n" + matchColorTable.get(matchLocation);
                    } else {
                        comp3MatchTimes = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                        comp3MatchTables = matchColorTable.get(matchLocation);
                    }
                    XSSFCell cell = rows[row].createCell(10);
                    XSSFCell cell2 = rows[row].createCell(11);
                    cell.setCellValue(comp3MatchTimes);
                    cell2.setCellValue(comp3MatchTables);                

                    // turn on word wrap
                    cell.setCellStyle(cellStyle);
                    cell2.setCellStyle(cellStyle);
                }
            } catch (NullPointerException e){
                // timeslot doesn't exist so stop processing
                // t = timeSlots;
                // team = teamCount;
            }
        }
        // Auto size the columns
        for (int c = 0; c <= rows[0].getLastCellNum(); c++) {
            sheet.autoSizeColumn(c);
        }
        // Auto size the Wildcard team row
        // rows[teamCount+1].setHeightInPoints(rows[teamCount+1].getHeightInPoints()*2);
    }

    public void createUpdatedWorkbook(String filename) {
        // Workbook wb = new XSSFWorkbook();
        Workbook wb = inputWorkbook;
        try (OutputStream fileOut = new FileOutputStream(filename)) {
            wb.write(fileOut);
        } catch (Exception e) {
            System.out.println("Error saving output file" + filename);
            System.exit(-1);
        }
    }
}
