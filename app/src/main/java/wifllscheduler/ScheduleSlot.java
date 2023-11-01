package wifllscheduler;

import java.time.LocalTime;

public class ScheduleSlot {
    private Integer teamNumber;
    private LocalTime start;
    private LocalTime end;
    private LocalTime offset;
    private SlotType type;
    private Location location;
    
    public enum SlotType {
        COACHES_MEETING,
        PRACTICE_MATCH,
        COMPETITION_MATCH1,
        COMPETITION_MATCH2,
        COMPETITION_MATCH3,
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
        offset = timeDelta(start, blockStartTime);
        this.type = type;
    }

    public String toString() {
        return "Slot for team " + teamNumber + " is of type " + type + " it is in " + location + " with a start Time: " + start + " ends at " + end + " startng " + offset + " in the block";
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

    public boolean isJudgingSlot() {
        return type == SlotType.JUDGING;
    }

    public int getTeamNumber() {
        return teamNumber;
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
