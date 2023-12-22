package wifllscheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.time.LocalTime;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HeaderFooter;

import wifllscheduler.ScheduleSlot.SlotType;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PaperSize;
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

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
            XSSFCell timeCell;
            String time;
            XSSFCell c = row.getCell(judgingHeaderColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (c != null) {
                String cellValue = formatter.formatCellValue(c);
                switch (cellValue) {
                    case "Tournament Date":
                        c = row.getCell(judgingHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        Date tournamentDate = c.getDateCellValue();
                        scheduleData.setTournamentDate(tournamentDate, "TournamentSetup!B" + (rn + 1));
                        break;
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
                            judingColorTable.add("TournamentSetup!A" + (rn + 1));
                                                        
                            // Process the judging times
                            timeCell = row.getCell(judgingHeaderColumn + 2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            String judgingTime = formatter.formatCellValue(timeCell);
                            scheduleData.addJudgingTime(judgingTime, "TournamentSetup!C" + rn);
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
                    case "Practice round team offset":
                        c = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        int practiceTeamOffset = Integer.parseInt(formatter.formatCellValue(c));
                        scheduleData.setPracticeTeamOffset(practiceTeamOffset);
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
                    case "Round 1 team offset":
                        c = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        int round1TeamOffset = Integer.parseInt(formatter.formatCellValue(c));
                        scheduleData.setRound1TeamOffset(round1TeamOffset);
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
                    case "Round 2 team offset":
                        c = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        int round2TeamOffset = Integer.parseInt(formatter.formatCellValue(c));
                        scheduleData.setRound2TeamOffset(round2TeamOffset);
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
                    case "Round 3 team offset":
                        c = row.getCell(matchHeaderColumn + 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        int round3TeamOffset = Integer.parseInt(formatter.formatCellValue(c));
                        scheduleData.setRound3TeamOffset(round3TeamOffset);
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
                                matchColorTable.add("TournamentSetup!E" + (rn + 1) + ",\" A\"");
                                matchColorTable.add("TournamentSetup!E" + (rn + 1) + ",\" B\"");
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

    public void updateScheduleTab(Schedule scheduleInfo, Scheduler schedulerInfo) {
        final int teamNumberCol = 0;
        final int teamNameCol = 1;
        final int coachesMeetingTimeCol = 2;
        final int judgingTimeCol = 3;
        final int judgingRoomCol = 4;
        final int practiceTimeCol = 5;
        final int practiceTableCol = 6;
        final int comp1TimeCol = 7;
        final int comp1TableCol = 8;
        final int comp2TimeCol = 9;
        final int comp2TableCol = 10;
        final int comp3TimeCol = 11;
        final int comp3TableCol = 12;
        
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
        String formula;
        
        XSSFCellStyle my_style = inputWorkbook.createCellStyle();
        XSSFFont my_font=inputWorkbook.createFont();
        /* set the weight of the font */
        my_font.setBold(true);
        /* attach the font to the style created earlier */
        my_style.setFont(my_font);
        /* At this stage, we have a bold style created which we can attach to a cell */
        
        /**
         * +1 for header
         * Team Count
         * +1 for wild card team
         * +2 for potential upto 3 wild card matches
         */
        rows = new XSSFRow[scheduleInfo.getTeamCount()+ 1 + 1 + 2];
        for (int c = 0; c < teamCount+1 + 1 + 2; c++) {
            int teamRow = c + 1;
            rows[c]= sheet.createRow(c);
            if (c > 0) {
                // team number
                XSSFCell cell = rows[c].createCell(teamNumberCol);
                formula = "_xlfn.CONCAT(Team List!B" + teamRow + ")";
                cell.setCellFormula(formula);
                XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                formulaEvaluator.evaluateFormulaCell(cell);

                // team name
                cell = rows[c].createCell(teamNameCol);
                formula = "_xlfn.CONCAT(Team List!C" + teamRow + ")";
                cell.setCellFormula(formula);
                formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                formulaEvaluator.evaluateFormulaCell(cell);
            }
        }
        
        // add in the wild card match rows
        rows[teamCount+1] = sheet.createRow(teamCount+1);
        rows[teamCount+1].createCell(teamNameCol).setCellValue("Wild Card");
        rows[teamCount+2] = sheet.createRow(teamCount+2);
        rows[teamCount+3] = sheet.createRow(teamCount+3);
        rows[0].createCell(teamNumberCol).setCellValue("Team #");
        rows[0].createCell(teamNameCol).setCellValue("Team Name");
        rows[0].createCell(coachesMeetingTimeCol).setCellValue("Coach Meeting");
        rows[0].createCell(judgingTimeCol).setCellValue("Judging Start");
        rows[0].createCell(judgingRoomCol).setCellValue("Judging Color");
        rows[0].createCell(practiceTimeCol).setCellValue("Practice Time");
        rows[0].createCell(practiceTableCol).setCellValue("Practice Table");
        rows[0].createCell(comp1TimeCol).setCellValue("Round 1 Time");
        rows[0].createCell(comp1TableCol).setCellValue("Round 1 Table");
        rows[0].createCell(comp2TimeCol).setCellValue("Round 2 Time");
        rows[0].createCell(comp2TableCol).setCellValue("Round 2 Table");
        rows[0].createCell(comp3TimeCol).setCellValue("Round 3 Time");
        rows[0].createCell(comp3TableCol).setCellValue("Round 3 Table");        

        /**
         * Setup gray scale line formatting
         */
        XSSFCellStyle matchColorStyle = inputWorkbook.createCellStyle();
        matchColorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        matchColorStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        // Process the teams
        for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
            for (int team = 0; team < teamCount; team++) {
                try {
                    // Create a row and put some cells in it. Rows are 0 based.
                    // Create a cell and put a value in it.
                    ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                    int row = team + 1;

                    if (slot.isCoachMeetingSlot()) {
                        XSSFCell cell = rows[row].createCell(coachesMeetingTimeCol);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                    } else if (slot.isJudgingSlot()) {
                        XSSFCell cell = rows[row].createCell(judgingTimeCol);
                        // cell.setCellValue(scheduleInfo.getTimeForSlot(t).format(DateTimeFormatter.ofPattern("hh:mm a")));
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        XSSFCell cell2 = rows[row].createCell(judgingRoomCol);
                        // cell.setCellValue(schedulerInfo.getJudgingTimeCellLocation(slot.getStartTime()));
                        int judgingLocation = slot.getJudgingIndex();
                        formula = "_xlfn.CONCAT(" + judingColorTable.get(judgingLocation) + ")";
                        cell2.setCellFormula(formula);
                        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                        formulaEvaluator.evaluateFormulaCell(cell2);
                    } else if (slot.isPracticeMatch()) {
                        XSSFCell cell = rows[row].createCell(practiceTimeCol);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));

                        XSSFCell cell2 = rows[row].createCell(practiceTableCol);
                        int matchLocation = slot.getTableIndex();
                        formula = "_xlfn.CONCAT( " + matchColorTable.get(matchLocation) + ")";
                        cell2.setCellFormula(formula);
                        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                        formulaEvaluator.evaluateFormulaCell(cell2);
                    } else if (slot.isMatch1()) {
                        XSSFCell cell = rows[row].createCell(comp1TimeCol);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));

                        XSSFCell cell2 = rows[row].createCell(comp1TableCol);
                        int matchLocation = slot.getTableIndex();
                        formula = "_xlfn.CONCAT( " + matchColorTable.get(matchLocation) + ")";
                        cell2.setCellFormula(formula);
                        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                        formulaEvaluator.evaluateFormulaCell(cell2);
                    } else if (slot.isMatch2()) {
                        XSSFCell cell = rows[row].createCell(comp2TimeCol);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        
                        XSSFCell cell2 = rows[row].createCell(comp2TableCol);
                        int matchLocation = slot.getTableIndex();
                        formula = "_xlfn.CONCAT( " + matchColorTable.get(matchLocation) + ")";
                        cell2.setCellFormula(formula);
                        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                        formulaEvaluator.evaluateFormulaCell(cell2);
                    } else if (slot.isMatch3()) {
                        XSSFCell cell = rows[row].createCell(comp3TimeCol);
                        cell.setCellValue(slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                        
                        XSSFCell cell2 = rows[row].createCell(comp3TableCol);
                        int matchLocation = slot.getTableIndex();
                        formula = "_xlfn.CONCAT( " + matchColorTable.get(matchLocation) + ")";
                        cell2.setCellFormula(formula);
                        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                        formulaEvaluator.evaluateFormulaCell(cell2);
                    }
                    if ((row % 2) == 1) {
                        for (int c = 0; c < rows[row].getLastCellNum(); c++){
                            rows[row].getCell(c).setCellStyle(matchColorStyle);
                        }
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
        String matchTime = "";
        String matchTable = "";
        CellStyle cellStyle = inputWorkbook.createCellStyle();
        cellStyle.setWrapText(true);

        XSSFCell cell = null;
        XSSFCell cell2 = null;

        int wildcardPraticeMatches = 0;
        int wildcardRound1Matches = 0;
        int wildcardRound2Matches = 0;
        int wildcardRound3Matches = 0;
        for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
            int team = teamCount;
            int row = team + 1;
            try {
                ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                if (slot.isRobotMatch()) {
                    int matchLocation = slot.getTableIndex();
                    matchTime = slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                    matchTable = matchColorTable.get(matchLocation);
                    if (slot.isPracticeMatch()) {
                        cell = rows[row + wildcardPraticeMatches].createCell(practiceTimeCol);
                        cell2 = rows[row + wildcardPraticeMatches].createCell(practiceTableCol);
                        wildcardPraticeMatches++;
                    } else if (slot.isMatch1()) {
                        cell = rows[row + wildcardRound1Matches].createCell(comp1TimeCol);
                        cell2 = rows[row + wildcardRound1Matches].createCell(comp1TableCol);
                        wildcardRound1Matches++;
                    } else if (slot.isMatch2()) {
                        cell = rows[row + wildcardRound2Matches].createCell(comp2TimeCol);
                        cell2 = rows[row + wildcardRound2Matches].createCell(comp2TableCol);
                        wildcardRound2Matches++;
                    } else if (slot.isMatch3()) {
                        cell = rows[row + wildcardRound3Matches].createCell(comp3TimeCol);
                        cell2 = rows[row + wildcardRound3Matches].createCell(comp3TableCol);
                        wildcardRound3Matches++;
                    }
                    cell.setCellValue(matchTime);
                    matchLocation = slot.getTableIndex();
                    formula = "_xlfn.CONCAT( " + matchTable + ")";
                    cell2.setCellFormula(formula);
                    XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
                    formulaEvaluator.evaluateFormulaCell(cell2);
                }
            } catch (NullPointerException e){
                // timeslot doesn't exist so stop processing
                // t = timeSlots;
                // team = teamCount;
            }
        }
        // setup printing
        sheet.setPrintGridlines(false);
        
        // Auto size the columns
        for (int c = 0; c < rows[0].getLastCellNum(); c++) {
            sheet.autoSizeColumn(c);
            rows[0].getCell(c).setCellStyle(my_style);
        }
        // Set column width (50 characters * character unit size of 1/256)
        sheet.setColumnWidth(1, 50*256);
        rows[0].getCell(teamNameCol).setCellStyle(my_style);

        /**
        * set the border lines to delineate judging and matching to make
        * it easier to read.
        */
        // coaches meeting
        CellRangeAddress region = new CellRangeAddress(0, teamCount, coachesMeetingTimeCol, coachesMeetingTimeCol);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        // judging
        region = new CellRangeAddress(0, teamCount, judgingTimeCol, judgingTimeCol);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        // practice round
        region = new CellRangeAddress(0, teamCount, practiceTimeCol, practiceTimeCol);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        // comp round 1
        region = new CellRangeAddress(0, teamCount, comp1TimeCol, comp1TimeCol);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        // comp round 2
        region = new CellRangeAddress(0, teamCount, comp2TimeCol, comp2TimeCol);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        // comp round 3
        region = new CellRangeAddress(0, teamCount, comp3TimeCol, comp3TimeCol);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);

        sheet.setColumnBreak(rows[0].getLastCellNum() - 1);
        XSSFPrintSetup pageSetup = sheet.getPrintSetup();
        sheet.setFitToPage(true);
        // sheet.setAutobreaks(true);
        inputWorkbook.setPrintArea(sheetIndex, "$A$1:$M$" + rows.length);
        pageSetup.setFitHeight((short)1);
        pageSetup.setFitWidth((short)1);
        pageSetup.setPaperSize(PaperSize.LEGAL_PAPER);
        pageSetup.setLandscape(true);
        pageSetup.setOrientation(PrintOrientation.LANDSCAPE);
        // due to a bug this needs to be after setting the orientation
        sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));
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

    public void UpdateTournamentImportSheet(Schedule scheduleInfo, Scheduler schedulerInfo) {
        String sheetName = "_DONOTEDITFLLTournamentImport";
        
        // delete the schedule sheet and recreate it
        int sheetIndex = inputWorkbook.getSheetIndex(sheetName);
        inputWorkbook.removeSheetAt(sheetIndex);
        XSSFSheet sheet = inputWorkbook.createSheet(sheetName);
        // put the sheet back in the same position in the spreadsheet
        inputWorkbook.setSheetOrder(sheetName, sheetIndex);

        int teamCount = scheduleInfo.getTeamCount();
        XSSFRow rows[];
        /**
         * One row for each item:
         * 1 - header row
         * Per team: 
         * 1 - coaches meeting
         * 1 - judging session
         * 1 - practice round
         * 3 - competition rounds
         */
        int rowCount = 1 + (1 + 1 + 1 + 3 + 1) * teamCount;
        rows = new XSSFRow[rowCount+1];

        for (int c = 0; c < rowCount; c++) {
            rows[c]= sheet.createRow(c);
        }

        // put in the headers
        rows[0].createCell(0).setCellValue("Date");
        rows[0].createCell(1).setCellValue("Begin Time");
        rows[0].createCell(2).setCellValue("End Time");
        rows[0].createCell(3).setCellValue("Type");
        rows[0].createCell(4).setCellValue("Round");
        rows[0].createCell(5).setCellValue("Description");
        rows[0].createCell(6).setCellValue("Room");
        rows[0].createCell(7).setCellValue("Team #");
        
        int row = 1;
        String tournamentDate = schedulerInfo.getTournamentDateCellLoc();
        for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
            for (int team = 0; team < teamCount; team++) {
                try {
                    ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                    LocalTime startTime = slot.getStartTime();
                    LocalTime endTime = slot.getEndTime();
                    if (slot.isCoachMeetingSlot()) {
                        // setting round to 1 due to a loader but
                        InsertInventoryDataToRow(rows[row], tournamentDate, startTime, endTime, "General", 1, "Coaches Meeting", "", team);
                    } else if (slot.isJudgingSlot()) {
                        int judgingSlotForTime = scheduleInfo.scheduleSlot(startTime);
                        if (judgingSlotForTime == t) {
                            int judgingLocation = slot.getJudgingIndex();
                            InsertInventoryDataToRow(rows[row], tournamentDate, startTime, endTime, "Core", 1, "Judging Session", judingColorTable.get(judgingLocation), team);
                        } else {
                            row--;
                        }
                    } else if (slot.isPracticeMatch()) {
                        int matchLocation = slot.getTableIndex();
                        InsertInventoryDataToRow(rows[row], tournamentDate, startTime, endTime, "Practice", 1, "Practice Round", matchColorTable.get(matchLocation), team);
                    } else if (slot.isMatch1()) {
                        int matchLocation = slot.getTableIndex();
                        InsertInventoryDataToRow(rows[row], tournamentDate, startTime, endTime, "Table", 1, "Competition Match 1", matchColorTable.get(matchLocation), team);
                    } else if (slot.isMatch2()) {
                        int matchLocation = slot.getTableIndex();
                        InsertInventoryDataToRow(rows[row], tournamentDate, startTime, endTime, "Table", 2, "Competition Match 2", matchColorTable.get(matchLocation), team);
                    } else if (slot.isMatch3()) {
                        int matchLocation = slot.getTableIndex();
                        InsertInventoryDataToRow(rows[row], tournamentDate, startTime, endTime, "Table", 3, "Competition Match 3", matchColorTable.get(matchLocation), team);
                    }
                    row++;
                } catch (NullPointerException e){
                    // timeslot doesn't exist so stop processing
                    // t = timeSlots;
                    // team = teamCount;
                }
            }
        }
        // Auto size the columns
        for (int c = 0; c <= rows[0].getLastCellNum(); c++) {
            sheet.autoSizeColumn(c);
        }
    }
    
    private void InsertInventoryDataToRow(XSSFRow row, String dateCellLocation, LocalTime startTime, LocalTime endTime, String type, int round, String description, String room, int teamNumber) {

        XSSFCell dateCell = row.createCell(0);
        XSSFCell startTimeCell = row.createCell(1);
        XSSFCell endTimeCell = row.createCell(2);
        XSSFCell typeCell = row.createCell(3);
        XSSFCell roundCell = row.createCell(4);
        XSSFCell descriptionCell = row.createCell(5);
        XSSFCell roomCell = row.createCell(6);
        XSSFCell teamNumberCell = row.createCell(7);

        // Cell formatting
        CreationHelper createHelper = inputWorkbook.getCreationHelper();  
        CellStyle cellStyle = inputWorkbook.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));  

        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
        String formula;
        int teamRow = teamNumber + 2;

        dateCell.setCellFormula(dateCellLocation);
        dateCell.setCellStyle(cellStyle);
        formulaEvaluator.evaluateFormulaCell(dateCell);
        startTimeCell.setCellValue(startTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        endTimeCell.setCellValue(endTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        typeCell.setCellValue(type);
        if (round > 0) {
            roundCell.setCellValue(round);
        } else {
            roundCell.setCellValue("");
        }
        descriptionCell.setCellValue(description);
        
        if (room.length() > 0) {
            formula = "_xlfn.CONCAT(" + room + ")";
            roomCell.setCellFormula(formula);
            formulaEvaluator.evaluateFormulaCell(roomCell);
        } else {
            roomCell.setCellValue(room);
        }

        formula = "_xlfn.CONCAT(Team List!B" + teamRow + ")";
        teamNumberCell.setCellFormula(formula);
        formulaEvaluator.evaluateFormulaCell(teamNumberCell);
    }

    public void UpdateMatchQueueingTab(Schedule scheduleInfo, Scheduler schedulerInfo) {
        final int teamNumberCol = 0;
        final int teamNameCol = 1;
        final int roundCol = 2;
        final int matchStartCol = 3;
        final int tableCol = 4;
        String sheetName = "MatchQueueing";
        
        // delete the match queueing sheet and recreate it
        int sheetIndex = inputWorkbook.getSheetIndex(sheetName);
        if (sheetIndex >= 0) {
            inputWorkbook.removeSheetAt(sheetIndex);
        }
        XSSFSheet sheet = inputWorkbook.createSheet(sheetName);
        // put the sheet back in the same position in the spreadsheet
        if (sheetIndex >= 0) {
            inputWorkbook.setSheetOrder(sheetName, sheetIndex);
        }

        int teamCount = scheduleInfo.getTeamCount();
        int row = 0;
        XSSFRow titleRow = sheet.createRow(row++);
        XSSFRow headerRow = sheet.createRow(row++);
        XSSFRow matchRow;
        int matchLocation;

        XSSFCellStyle my_style = inputWorkbook.createCellStyle();
        XSSFFont my_font=inputWorkbook.createFont();
        /* set the weight of the font */
        my_font.setBold(true);
        /* attach the font to the style created earlier */
        my_style.setFont(my_font);
        /* At this stage, we have a bold style created which we can attach to a cell */
        
        // setup title row
        sheet.addMergedRegion(CellRangeAddress.valueOf("A1:E1"));
        titleRow.createCell(teamNumberCol).setCellValue("Per Table Match Schedule");
        my_style.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(teamNumberCol).setCellStyle(my_style);
        titleRow.createCell(teamNameCol).setCellStyle(my_style);
        titleRow.createCell(roundCol).setCellStyle(my_style);
        titleRow.createCell(matchStartCol).setCellStyle(my_style);
        titleRow.createCell(tableCol).setCellStyle(my_style);
        
        // put in the headers
        my_style.setAlignment(HorizontalAlignment.LEFT);
        headerRow.createCell(teamNumberCol).setCellValue("Team #");
        headerRow.getCell(teamNumberCol).setCellStyle(my_style);
        headerRow.createCell(teamNameCol).setCellValue("Team Name");
        headerRow.getCell(teamNameCol).setCellStyle(my_style);
        headerRow.createCell(roundCol).setCellValue("Round");
        headerRow.getCell(roundCol).setCellStyle(my_style);
        headerRow.createCell(matchStartCol).setCellValue("Match Start");
        headerRow.getCell(matchStartCol).setCellStyle(my_style);
        headerRow.createCell(tableCol).setCellValue("Table");
        headerRow.getCell(tableCol).setCellStyle(my_style);
        for (int table = 0; table < matchColorTable.size(); table++) {
            for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
                // include wild card matches
                for (int team = 0; team < teamCount + 1; team++) {
                    try {
                        ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                        LocalTime startTime = slot.getStartTime();
                        matchLocation = slot.getTableIndex();
                        int teamNumberRow = slot.getTeamNumber();
                        
                        if (matchLocation == table) {
                            if (slot.isPracticeMatch()) {
                                matchRow = sheet.createRow(row++);
                                FillMatchDataRow(matchRow, startTime, "Practice", matchColorTable.get(matchLocation), teamNumberRow);
                            } else if (slot.isMatch1()) {
                                matchRow = sheet.createRow(row++);
                                FillMatchDataRow(matchRow, startTime, "Round 1", matchColorTable.get(matchLocation), teamNumberRow);
                            } else if (slot.isMatch2()) {
                                matchRow = sheet.createRow(row++);
                                FillMatchDataRow(matchRow, startTime, "Round 2", matchColorTable.get(matchLocation), teamNumberRow);
                            } else if (slot.isMatch3()) {
                                matchRow = sheet.createRow(row++);
                                FillMatchDataRow(matchRow, startTime, "Round 3", matchColorTable.get(matchLocation), teamNumberRow);
                            }
                        }
                    } catch (NullPointerException e){
                        // timeslot doesn't exist so stop processing
                        // t = timeSlots;
                        // team = teamCount;
                    }
                }
            }
            sheet.setRowBreak(row - 1);
        }
        // setup printing
        sheet.setRepeatingRows(CellRangeAddress.valueOf("1:2"));
        sheet.setPrintGridlines(true);
        for (int c = 0;c < headerRow.getLastCellNum(); c++) {
            sheet.autoSizeColumn(c);
        }
        // Set column width (50 characters * character unit size of 1/256)
        sheet.setColumnWidth(teamNameCol, 50*256);
    }
    
    public void UpdateJudgingQueueingTab(Schedule scheduleInfo, Scheduler schedulerInfo) {
        final int teamNumberCol = 0;
        final int teamNameCol = 1;
        final int judgingStartTimeCol = 2;
        final int judgingRoomCol = 3;
        String sheetName = "JudgeQueueing";
        
        // delete the match queueing sheet and recreate it
        int sheetIndex = inputWorkbook.getSheetIndex(sheetName);
        if (sheetIndex >= 0) {
            inputWorkbook.removeSheetAt(sheetIndex);
        }
        XSSFSheet sheet = inputWorkbook.createSheet(sheetName);
        // put the sheet back in the same position in the spreadsheet
        if (sheetIndex >= 0) {
            inputWorkbook.setSheetOrder(sheetName, sheetIndex);
        }

        int teamCount = scheduleInfo.getTeamCount();
        int row = 0;
        XSSFRow titleRow = sheet.createRow(row++);
        XSSFRow headerRow = sheet.createRow(row++);
        XSSFRow roomRow;
        int roomColor;

        XSSFCellStyle my_style = inputWorkbook.createCellStyle();
        XSSFFont my_font=inputWorkbook.createFont();
        /* set the weight of the font */
        my_font.setBold(true);
        /* attach the font to the style created earlier */
        my_style.setFont(my_font);
        /* At this stage, we have a bold style created which we can attach to a cell */
        // setup title row
        sheet.addMergedRegion(CellRangeAddress.valueOf("A1:C1"));
        titleRow.createCell(teamNumberCol).setCellValue("Per Judging Room Schedule");
        my_style.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(my_style);
        titleRow.createCell(1).setCellStyle(my_style);
        titleRow.createCell(2).setCellStyle(my_style);
        titleRow.createCell(3).setCellStyle(my_style);
        
        // put in the headers
        my_style.setAlignment(HorizontalAlignment.LEFT);
        headerRow.createCell(teamNumberCol).setCellValue("Team #");
        headerRow.getCell(teamNumberCol).setCellStyle(my_style);
        headerRow.createCell(teamNameCol).setCellValue("Team Name");
        headerRow.getCell(teamNameCol).setCellStyle(my_style);
        headerRow.createCell(judgingStartTimeCol).setCellValue("Judging Start Time");
        headerRow.getCell(judgingStartTimeCol).setCellStyle(my_style);
        headerRow.createCell(judgingRoomCol).setCellValue("Judging Room");
        headerRow.getCell(judgingRoomCol).setCellStyle(my_style);
        for (int room = 0; room < judingColorTable.size(); room++) {
            for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
                // include wild card matches
                for (int team = 0; team < teamCount + 1; team++) {
                    try {
                        ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                        LocalTime startTime = slot.getStartTime();
                        roomColor = slot.getJudgingIndex();
                        int teamNumberRow = slot.getTeamNumber();
                        
                        int judgingSlotForTime = scheduleInfo.scheduleSlot(startTime);
                        if (judgingSlotForTime == t) {
                            if (roomColor == room) {
                                if (slot.isJudgingSlot()) {
                                    roomRow = sheet.createRow(row++);
                                    FillJudgingDataRow(roomRow, startTime, judingColorTable.get(roomColor), teamNumberRow);
                                }
                            } 
                        }
                    }
                    catch (NullPointerException e){
                        // timeslot doesn't exist so stop processing
                        // t = timeSlots;
                        // team = teamCount;
                    }
                }
            }
            sheet.setRowBreak(row - 1);
        }
        // setup printing
        sheet.setRepeatingRows(CellRangeAddress.valueOf("1:2"));
        sheet.setPrintGridlines(true);
        for (int c = 1;c < headerRow.getLastCellNum(); c++) {
            sheet.autoSizeColumn(c);
        }
        // Set column width (50 characters * character unit size of 1/256)
        sheet.setColumnWidth(teamNameCol, 50*256);
    }

    public void UpdateEmceeTab(Schedule scheduleInfo, Scheduler schedulerInfo) {
        ArrayList<ScheduleSlot> matches = new ArrayList<ScheduleSlot>();
        String sheetName = "Emcee Tab";

        final int teamNumberCol = 0;
        final int teamNameCol = 1;
        final int roundCol = 2;
        final int matchStartCol = 3;
        final int tableCol = 4;
        
        // delete the match queueing sheet and recreate it
        int sheetIndex = inputWorkbook.getSheetIndex(sheetName);
        if (sheetIndex >= 0) {
            inputWorkbook.removeSheetAt(sheetIndex);
        }
        XSSFSheet sheet = inputWorkbook.createSheet(sheetName);
        // put the sheet back in the same position in the spreadsheet
        if (sheetIndex >= 0) {
            inputWorkbook.setSheetOrder(sheetName, sheetIndex);
        }

        int teamCount = scheduleInfo.getTeamCount();
        int row = 0;
        XSSFRow titleRow = sheet.createRow(row++);
        XSSFRow headerRow = sheet.createRow(row++);
        XSSFRow matchRow = null;
        int matchLocation;

        XSSFCellStyle my_style = inputWorkbook.createCellStyle();
        XSSFFont my_font=inputWorkbook.createFont();
        /* set the weight of the font */
        my_font.setBold(true);
        /* attach the font to the style created earlier */
        my_style.setFont(my_font);
        /* At this stage, we have a bold style created which we can attach to a cell */
        
        // setup title row
        sheet.addMergedRegion(CellRangeAddress.valueOf("A1:E1"));
        titleRow.createCell(teamNumberCol).setCellValue("Emcee Sheet");
        my_style.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(teamNumberCol).setCellStyle(my_style);
        titleRow.createCell(teamNameCol).setCellStyle(my_style);
        titleRow.createCell(roundCol).setCellStyle(my_style);
        titleRow.createCell(matchStartCol).setCellStyle(my_style);
        titleRow.createCell(tableCol).setCellStyle(my_style);
        
        // put in the headers
        my_style.setAlignment(HorizontalAlignment.LEFT);
        headerRow.createCell(teamNumberCol).setCellValue("Team #");
        headerRow.getCell(teamNumberCol).setCellStyle(my_style);
        headerRow.createCell(teamNameCol).setCellValue("Team Name");
        headerRow.getCell(teamNameCol).setCellStyle(my_style);
        headerRow.createCell(roundCol).setCellValue("Round");
        headerRow.getCell(roundCol).setCellStyle(my_style);
        headerRow.createCell(matchStartCol).setCellValue("Match Start");
        headerRow.getCell(matchStartCol).setCellStyle(my_style);
        headerRow.createCell(tableCol).setCellValue("Table");
        headerRow.getCell(tableCol).setCellStyle(my_style);
        for (int t = 0; t < scheduleInfo.getNumberOfTimeSlots(); t++) {
            // include the wildcard team
            for (int team = 0; team < teamCount + 1; team++) {
                try {
                    ScheduleSlot slot = scheduleInfo.getSlotInfo(t, team);
                    if (slot.isRobotMatch()) {
                        matches.add(slot);
                    }
                } catch (NullPointerException e){
                    // timeslot doesn't exist so stop processing
                    // t = timeSlots;
                    // team = teamCount;
                }
            }
        }
        
        /**
         * Setup gray scale line formatting
         */
        XSSFCellStyle matchColorStyle = inputWorkbook.createCellStyle();
        matchColorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        matchColorStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        
        Collections.sort(matches);
        int matchesOnPage = 0;
        LocalTime oldStartTime = null;
        for (ScheduleSlot slot : matches) {
            LocalTime startTime = slot.getStartTime();
            matchLocation = slot.getTableIndex();
            int team = slot.getTeamNumber();

            if (oldStartTime == null) {
                oldStartTime = startTime;
            }
            if (oldStartTime.isBefore(startTime)) {
                if (matchesOnPage == 14) {
                    sheet.setRowBreak(row-1);
                    matchesOnPage = 0;
                } else {
                    matchRow = sheet.createRow(row++);
                    matchesOnPage++;
                }
                oldStartTime = startTime;
            }
            if (slot.isPracticeMatch()) {
                matchRow = sheet.createRow(row++);
                FillMatchDataRow(matchRow, startTime, "Practice", matchColorTable.get(matchLocation), team);
            } else if (slot.isMatch1()) {
                matchRow = sheet.createRow(row++);
                FillMatchDataRow(matchRow, startTime, "Round 1", matchColorTable.get(matchLocation), team);
            } else if (slot.isMatch2()) {
                matchRow = sheet.createRow(row++);
                FillMatchDataRow(matchRow, startTime, "Round 2", matchColorTable.get(matchLocation), team);
            } else if (slot.isMatch3()) {
                matchRow = sheet.createRow(row++);
                FillMatchDataRow(matchRow, startTime, "Round 3", matchColorTable.get(matchLocation), team);
            }
            if ((matchLocation >= 2) && (matchLocation <= 3)) {
                for (int c = 0; c < matchRow.getLastCellNum(); c++) {
                    matchRow.getCell(c).setCellStyle(matchColorStyle);
                }
            }
        }
        // setup printing
        sheet.setRepeatingRows(CellRangeAddress.valueOf("1:2"));
        sheet.setPrintGridlines(true);
        for (int c = 0;c < headerRow.getLastCellNum(); c++) {
            sheet.autoSizeColumn(c);
        }
        // Set column width (50 characters * character unit size of 1/256)
        sheet.setColumnWidth(teamNameCol, 50*256);
        Footer footer = sheet.getFooter();  
        footer.setCenter( "Page " + HeaderFooter.page() + " of " + HeaderFooter.numPages() );  
    }
    private void FillMatchDataRow(XSSFRow row, LocalTime startTime, String roundType, String table, int teamNumber) {
        XSSFCell teamNumberCell = row.createCell(0);
        XSSFCell teamNameCell = row.createCell(1);
        XSSFCell roundTypeCell = row.createCell(2);
        XSSFCell startTimeCell = row.createCell(3);
        XSSFCell tableCell = row.createCell(4);

        // Cell formatting
        CreationHelper createHelper = inputWorkbook.getCreationHelper();  
        CellStyle cellStyle = inputWorkbook.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));  

        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
        String formula;
        int teamRow = teamNumber + 1;

        roundTypeCell.setCellValue(roundType);

        startTimeCell.setCellValue(startTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        formula = "_xlfn.CONCAT(" + table + ")";
        tableCell.setCellFormula(formula);
        formulaEvaluator.evaluateFormulaCell(tableCell);
        
        if (teamNumber != 999999) {
            formula = "_xlfn.CONCAT(Team List!B" + teamRow + ")";
            teamNumberCell.setCellFormula(formula);
            formulaEvaluator.evaluateFormulaCell(teamNumberCell);
            formula = "_xlfn.CONCAT(Team List!C" + teamRow + ")";
            teamNameCell.setCellFormula(formula);
            formulaEvaluator.evaluateFormulaCell(teamNameCell);
        } else {
            teamNameCell.setCellValue("Wild Card Team");
        }
    }

    private void FillJudgingDataRow(XSSFRow row, LocalTime startTime, String room, int teamNumber) {
        XSSFCell teamNumberCell = row.createCell(0);
        XSSFCell teamNameCell = row.createCell(1);
        XSSFCell startTimeCell = row.createCell(2);
        XSSFCell roomCell = row.createCell(3);

        // Cell formatting
        CreationHelper createHelper = inputWorkbook.getCreationHelper();  
        CellStyle cellStyle = inputWorkbook.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));  

        XSSFFormulaEvaluator formulaEvaluator = inputWorkbook.getCreationHelper().createFormulaEvaluator();
        String formula;
        int teamRow = teamNumber + 1;

        startTimeCell.setCellValue(startTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
        formula = "_xlfn.CONCAT(" + room + ")";
        roomCell.setCellFormula(formula);
        formulaEvaluator.evaluateFormulaCell(roomCell);
        
        formula = "_xlfn.CONCAT(Team List!B" + teamRow + ")";
        teamNumberCell.setCellFormula(formula);
        formulaEvaluator.evaluateFormulaCell(teamNumberCell);
        formula = "_xlfn.CONCAT(Team List!C" + teamRow + ")";
        teamNameCell.setCellFormula(formula);
        formulaEvaluator.evaluateFormulaCell(teamNumberCell);
    }
}
