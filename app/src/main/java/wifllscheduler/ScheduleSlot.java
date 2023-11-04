package wifllscheduler;

import java.time.LocalTime;

public class ScheduleSlot {
    private Integer teamNumber;
    private LocalTime start;
    private LocalTime end;
    private LocalTime blockStart;
    private LocalTime offsetTime;
    private SlotType type;
    private Location location;
    
    public enum SlotType {
        COACHES_MEETING_TIME1,
        COACHES_MEETING_TIME2,
        PRACTICE_MATCH_TIME1,
        PRACTICE_MATCH_TIME2,
        COMPETITION_MATCH1_TIME1,
        COMPETITION_MATCH1_TIME2,
        COMPETITION_MATCH2_TIME1,
        COMPETITION_MATCH2_TIME2,
        COMPETITION_MATCH3_TIME1,
        COMPETITION_MATCH3_TIME2,
        JUDGING
    }

    public enum Location {
        COMPETITION_FIELD,
        TABLE_REDA,
        TABLE_REDB,
        TABLE_BLUEA,
        TABLE_BLUEB,
        TABLE_COLOR3A,
        TABLE_COLOR3B,
        TABLE_COLOR4A,
        TABLE_COLOR4B,
        JUDGING_ROOM1,
        JUDGING_ROOM2,
        JUDGING_ROOM3,
        JUDGING_ROOM4,
        JUDGING_ROOM5,
        JUDGING_ROOM6,
        JUDGING_ROOM7,
        JUDGING_ROOM8,
        UNKNOWN
    }

    public ScheduleSlot(Integer teamNumber, SlotType type, Location location, int startHour, int startMinute, LocalTime blockStartTime, int lengthMinutes) {
        this.teamNumber = teamNumber;
        this.location = location;
        while (startMinute >= 60) {
            startHour++;
            startMinute = startMinute - 60;
        }
        start = LocalTime.of(startHour, startMinute);
        end = start.plusMinutes(lengthMinutes);
        blockStart = blockStartTime;
        offsetTime = timeDelta(start, blockStartTime);
        this.type = type;
    }

    public String toString() {
        return "Slot for team " + teamNumber + " is of type " + type + " it is in " + location + " with a start Time: " + start + " ends at " + end + " startng " + offsetTime + " in the block start time of " + blockStart;
    }

    public static Location getJudgingLocation(int n) {
        Location location;
        switch (n) {
            case 0:
                location = Location.JUDGING_ROOM1;
                break;
            case 1:
                location = Location.JUDGING_ROOM2;
                break;
            case 2:
                location = Location.JUDGING_ROOM3;
                break;
            case 3:
                location = Location.JUDGING_ROOM4;
                break;
            case 4:
                location = Location.JUDGING_ROOM5;
                break;
            case 5:
                location = Location.JUDGING_ROOM6;
                break;
            case 6:
                location = Location.JUDGING_ROOM7;
                break;
            case 7:
                location = Location.JUDGING_ROOM8;
                break;
            default:
                location = Location.UNKNOWN;
            }
        return location;
    }

    public int getTableIndex() {
        int index = 0;
        switch (location) {
            case TABLE_REDA:
                index = 0;
                break;
            case TABLE_REDB:
                index = 1;
                break;
            case TABLE_BLUEA:
                index = 2;
                break;
            case TABLE_BLUEB:
                index = 3;
                break;
            case TABLE_COLOR3A:
                index = 4;
                break;
            case TABLE_COLOR3B:
                index = 5;
                break;
            case TABLE_COLOR4A:
                index = 6;
                break;
            case TABLE_COLOR4B:
                index = 6;
                break;
            default:
                index = -1;
        }
        return index;
    }

    public int getJudgingIndex() {
        int index = 0;
        switch (location) {
            case JUDGING_ROOM1:
                index = 0;
                break;
            case JUDGING_ROOM2:
                index = 1;
                break;
            case JUDGING_ROOM3:
                index = 2;
                break;
            case JUDGING_ROOM4:
                index = 3;
                break;
            case JUDGING_ROOM5:
                index = 4;
                break;
            case JUDGING_ROOM6:
                index = 5;
                break;
            case JUDGING_ROOM7:
                index = 6;
                break;
            case JUDGING_ROOM8:
                index = 7;
                break;
            default:
                index = -1;
            }
        return index;
    }

    public boolean isJudgingSlot() {
        return type == SlotType.JUDGING;
    }

    public boolean isCoachMeetingSlot() {
        return type == SlotType.COACHES_MEETING_TIME1 || type == SlotType.COACHES_MEETING_TIME2;
    }

    public boolean isPracticeMatch() {
        return type == SlotType.PRACTICE_MATCH_TIME1 || type == SlotType.PRACTICE_MATCH_TIME2;
    }

    public boolean isMatch1() {
        return type == SlotType.COMPETITION_MATCH1_TIME1 || type == SlotType.COMPETITION_MATCH1_TIME2;
    }
    
    public boolean isMatch2() {
        return type == SlotType.COMPETITION_MATCH2_TIME1 || type == SlotType.COMPETITION_MATCH2_TIME2;
    }
    
    public boolean isMatch3() {
        return type == SlotType.COMPETITION_MATCH3_TIME1 || type == SlotType.COMPETITION_MATCH3_TIME2;
    }
    
    public int getTeamNumber() {
        return teamNumber;
    }

    public LocalTime getStartTime() {
        return start;
    }

    private LocalTime timeDelta(LocalTime t1, LocalTime t2) {
        LocalTime delta;
        int hours;
        int minutes;
        int time2TotalMinutes = t2.getHour() * 60 + t2.getMinute();
        int time1TotalMinutes = t1.getHour() * 60 + t1.getMinute();
        int deltaMinutes = Math.abs(time2TotalMinutes - time1TotalMinutes);

        minutes = deltaMinutes % 60;
        hours = (deltaMinutes - minutes) / 60;
        delta = LocalTime.of(hours, minutes);

        return delta;
    }
}
