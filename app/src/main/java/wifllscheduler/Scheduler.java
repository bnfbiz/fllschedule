package wifllscheduler;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import wifllscheduler.ScheduleSlot.SlotType;

public class Scheduler {
    private int numJudgingRooms;
    private int numRobotGameTablePairs;
    private LocalTime dayStartTime;
    private LocalTime coachMeetingTime1;
    private String coachMeetingTime1CellLoc;
    private LocalTime coachMeetingTime2;
    private String coachMeetingTime2CellLoc;
    private LocalTime practiceMatchTime1;
    private String practiceMatchTime1CellLoc;
    private LocalTime practiceMatchTime2;
    private String practiceMatchTime2CellLoc;
    private LocalTime round1MatchTime1;
    private String round1MatchTime1CellLoc;
    private LocalTime round1MatchTime2;
    private String round1MatchTime2CellLoc;
    private LocalTime round2MatchTime1;
    private String round2MatchTime1CellLoc;
    private LocalTime round2MatchTime2;
    private String round2MatchTime2CellLoc;
    private LocalTime round3MatchTime1;
    private String round3MatchTime1CellLoc;
    private LocalTime round3MatchTime2;
    private String round3MatchTime2CellLoc;
    private LocalTime matchAlternateTime;
    private LocalTime minTimeBetweenActivities;
    private LocalTime lunchTime;
    private LocalTime judgingTimes[];
    private LocalTime judgingDuration = LocalTime.of(0,30);
    private LocalTime judgingMinimumDiscussionTime = LocalTime.of(0,15);

    public Scheduler(int numberOfJudgingRooms, int numberOfJudgingTimes, int numberOfRobotGameTablePairs) {
        numJudgingRooms = numberOfJudgingRooms;
        numRobotGameTablePairs = numberOfRobotGameTablePairs;
        judgingTimes = new LocalTime[numberOfJudgingTimes];
    }

    public Scheduler() {
        // the setters will be used for complete setup
    }

    public String toString() {
        return "JudgingRoomCount: " + numJudgingRooms + ", RobotGamePairs" + numRobotGameTablePairs + ", startTime " + dayStartTime 
            + ", CoachMeetingTime " + coachMeetingTime1 + "/" + coachMeetingTime2 + ", minActivityTime " + minTimeBetweenActivities 
            + ", lunchtime " + lunchTime + ", JudgingLength " + judgingDuration + " + " + judgingMinimumDiscussionTime;
    }

    public void addJudgingTime(int hour, int minute) {
        for (int c = 0; c < judgingTimes.length; c++) {
            if (judgingTimes[c] == null) {
                judgingTimes[c] = LocalTime.of(hour,  minute);
                c = judgingTimes.length;
            }
        }
    }
    
    public void addJudgingTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        for (int c = 0; c < judgingTimes.length; c++) {
            if (judgingTimes[c] == null) {
                judgingTimes[c] = LocalTime.parse(time, formatter);
                c = judgingTimes.length;
            }
        }
    }

    public void setNumberOfJudgingTimes(int numberOfJudgingTimes) {
       judgingTimes = new LocalTime[numberOfJudgingTimes];
    }

    public int getNumberOfJudgingTimes() {
        return judgingTimes.length;
    }

    public LocalTime[] getJudgingTimes() {
        return judgingTimes;
    }

    public LocalTime getDayStartTime() {
        return dayStartTime;
    }

    public void setDayStartTime(int hour, int minutes) {
        dayStartTime = LocalTime.of(hour, minutes);
    }

    public void setDayStartTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        dayStartTime = LocalTime.parse(time, formatter);
    }
 
    public LocalTime getCoachMeetingTime1() {
        return coachMeetingTime1;
    }
    
    public void setCoachMeetingTime1(int hour, int minutes) {
        coachMeetingTime1 = LocalTime.of(hour, minutes);
    }

    public void setCoachMeetingTime1(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        coachMeetingTime1 = LocalTime.parse(time, formatter);
    }

    public LocalTime getCoachMeetingTime2() {
        return coachMeetingTime2;
    }

    public void setCoachMeetingTime2(int hour, int minutes) {
        coachMeetingTime2 = LocalTime.of(hour, minutes);
    }
    
    public void setCoachMeetingTime2(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        coachMeetingTime2 = LocalTime.parse(time, formatter);
    }

    public LocalTime getPracticeMatchTime1() {
        return practiceMatchTime1;
    }
    
    public void setPracticeMatchTime1(int hour, int minutes) {
        practiceMatchTime1 = LocalTime.of(hour, minutes);
    }

    public void setPracticeMatchTime1(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        practiceMatchTime1 = LocalTime.parse(time, formatter);
    }
    
    
    public LocalTime getPracticeMatchTime2() {
        return practiceMatchTime2;
    }
    
    public void setPracticeMatchTime2(int hour, int minutes) {
        practiceMatchTime2 = LocalTime.of(hour, minutes);
    }
    
    public void setPracticeMatchTime2(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        practiceMatchTime2 = LocalTime.parse(time, formatter);
    }

    public LocalTime getRound1MatchTime1() {
        return round1MatchTime1;
    }
    
    public void setRound1MatchTime1(int hour, int minutes) {
        round1MatchTime1 = LocalTime.of(hour, minutes);
    }
    
    public void setRound1MatchTime1(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        round1MatchTime1 = LocalTime.parse(time, formatter);
    }

    public LocalTime getRound1MatchTime2() {
        return round1MatchTime2;
    }
    
    public void setRound1MatchTime2(int hour, int minutes) {
        round1MatchTime2 = LocalTime.of(hour, minutes);
    }
    
    public void setRound1MatchTime2(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        round1MatchTime2 = LocalTime.parse(time, formatter);
    }

    public LocalTime getRound2MatchTime1() {
        return round2MatchTime1;
    }
    public void setRound2MatchTime1(int hour, int minutes) {
        round2MatchTime1 = LocalTime.of(hour, minutes);
    }
    
    public void setRound2MatchTime1(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        round2MatchTime1 = LocalTime.parse(time, formatter);
    }
    
    public LocalTime getRound2MatchTime2() {
        return round2MatchTime2;
    }
    
    public void setRound2MatchTime2(int hour, int minutes) {
        round2MatchTime2 = LocalTime.of(hour, minutes);
    }
    
    public void setRound2MatchTime2(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        round2MatchTime2 = LocalTime.parse(time, formatter);
    }
    
    public LocalTime getRound3MatchTime1() {
        return round3MatchTime1;
    }
    
    public void setRound3MatchTime1(int hour, int minutes) {
        round3MatchTime1 = LocalTime.of(hour, minutes);
    }

    public void setRound3MatchTime1(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        round3MatchTime1 = LocalTime.parse(time, formatter);
    }
 
    public LocalTime getRound3MatchTime2() {
        return round3MatchTime2;
    }
    
    public void setRound3MatchTime2(int hour, int minutes) {
        round3MatchTime2 = LocalTime.of(hour, minutes);
    }
    
    public void setRound3MatchTime2(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        round3MatchTime2 = LocalTime.parse(time, formatter);
    }
 
    public LocalTime getMinTimeBetweenActivities() {
        return minTimeBetweenActivities;
    }
    
    public void setMinTimeBetweenActivities(int hour, int minutes) {
        minTimeBetweenActivities = LocalTime.of(hour, minutes);
    }
    
    public void setMinTimeBetweenActivities(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");

        minTimeBetweenActivities = LocalTime.parse(time, formatter);
    }

    public LocalTime getLunchTime() {
        return lunchTime;
    }     
    public void setLunchTime(int hour, int minutes) {
        lunchTime = LocalTime.of(hour, minutes);
    }     
    
    public void setLunchTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:m a");

        lunchTime = LocalTime.parse(time, formatter);
    }    
 
    public LocalTime getJudgingDuration() {
        return judgingDuration;
    }    

    public void setJudgingDuration(int hour, int minutes) {
        judgingDuration = LocalTime.of(hour, minutes);
    }    

    public void setJudgingDuration(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");

        judgingDuration = LocalTime.parse(time, formatter);
    }

    public LocalTime getJudgingMinimumDiscussionTime() {
        return judgingMinimumDiscussionTime;
    }
    
    public void setJudgingMinimumDiscussionTime(int hour, int minutes) {
        judgingMinimumDiscussionTime = LocalTime.of(hour, minutes);
    }
    
    public void setJudgingMinimumDiscussionTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");

        judgingMinimumDiscussionTime = LocalTime.parse(time, formatter);
    }

    public void setJudgingPanelNum(int rooms) {
        numJudgingRooms = rooms;
    }

    public int getJudgingPanelNum() {
        return numJudgingRooms;
    }

    public int getNumRobotGameTablePairs() {
        return numRobotGameTablePairs;
    }

    public LocalTime getMatchAlternateTIme() {
        return matchAlternateTime;
    }

    public void setMatchAlternateTime(int minutes) {
        matchAlternateTime = LocalTime.of(0, minutes);
    }

    public void setMatchAlternateTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");

        matchAlternateTime = LocalTime.parse(time, formatter);
    }

    public void setNumberOfGameTablePairs(int pairs) {
        numRobotGameTablePairs = pairs;
    }

    public int getNumberOfGameTablePairs() {
        return numRobotGameTablePairs;
    }

    public void setCellLocation(SlotType slotType, String location) {

        switch (slotType) {
            case COACHES_MEETING_TIME1:
                coachMeetingTime1CellLoc = location;
                break;
            case COACHES_MEETING_TIME2:
                coachMeetingTime2CellLoc = location;
                break;
            case PRACTICE_MATCH_TIME1:
                practiceMatchTime1CellLoc = location;
                break;
            case PRACTICE_MATCH_TIME2:
                practiceMatchTime2CellLoc = location;
                break;
            case COMPETITION_MATCH1_TIME1:
                round1MatchTime1CellLoc = location;
                break;
            case COMPETITION_MATCH1_TIME2:
                round1MatchTime2CellLoc = location;
                break;
            case COMPETITION_MATCH2_TIME1:
                round2MatchTime1CellLoc = location;
                break;
            case COMPETITION_MATCH2_TIME2:
                round2MatchTime2CellLoc = location;
                break;
            case COMPETITION_MATCH3_TIME1:
                round3MatchTime1CellLoc = location;
                break;
            case COMPETITION_MATCH3_TIME2:
                round3MatchTime2CellLoc = location;
                break;
            case JUDGING:
                // not currenty needed
                break;
            default:
                System.out.println("setLocation: Unknown slot type");
                break;
        }
    }

    public String getCellLocation(SlotType slotType) {
        String location;
        location="";
        switch (slotType) {
            case COACHES_MEETING_TIME1:
                location = coachMeetingTime1CellLoc;
                break;
            case COACHES_MEETING_TIME2:
                location = coachMeetingTime2CellLoc;
                break;
            case PRACTICE_MATCH_TIME1:
                location = practiceMatchTime1CellLoc;
                break;
            case PRACTICE_MATCH_TIME2:
                location = practiceMatchTime2CellLoc;
                break;
            case COMPETITION_MATCH1_TIME1:
                location = round1MatchTime1CellLoc;
                break;
            case COMPETITION_MATCH1_TIME2:
                location = round1MatchTime2CellLoc;
                break;
            case COMPETITION_MATCH2_TIME1:
                location = round2MatchTime1CellLoc;
                break;
            case COMPETITION_MATCH2_TIME2:
                location = round2MatchTime2CellLoc;
                break;
            case COMPETITION_MATCH3_TIME1:
                location = round3MatchTime1CellLoc;
                break;
            case COMPETITION_MATCH3_TIME2:
                location = round3MatchTime2CellLoc;
                break;
            case JUDGING:
                // not currenty needed
                break;
            default:
                System.out.println("getLocation: Unknown slot type");
                location = "Cell location unknown";
                break;
        }
        return location;
    }
}