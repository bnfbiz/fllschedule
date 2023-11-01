package wifllscheduler;

import java.time.LocalTime;

public class Schedule {
    private ScheduleSlot[][] schedule;
    private int timeSlots;
    private int teamCount;
    private LocalTime dayStartTime;
    private int scheduleMinutes = 15; // The number of minutes for each row in the schedule

    /**
     * @param schedulerInfo
     * @param teamList
     */
    public Schedule(Scheduler schedulerInfo, TeamList teamList) {

        // Need to better calculate the day but for now just make it 10hrs and skip printing the non-initialized end of day
        timeSlots = 10 * (int)Math.ceil(60/scheduleMinutes);
        teamCount = teamList.getSize();
        dayStartTime = schedulerInfo.getDayStartTime();
        schedule = new ScheduleSlot[timeSlots][teamList.getSize()+1]; // account for the wild card team that may occur

        // Create the coaches meeting
        int slot = scheduleSlot(schedulerInfo.getCoachMeetingTime1());
        System.out.println("Got slot " + slot + " for Coaches Meeting");
        for (int t=0; t < teamCount; t++) {
            Team team = teamList.getTeam(t);
            schedule[slot][t] = new ScheduleSlot(team.getTeamNumber(), ScheduleSlot.SlotType.COACHES_MEETING, ScheduleSlot.Location.COMPETITION_FIELD, schedulerInfo.getCoachMeetingTime1().getHour(), schedulerInfo.getCoachMeetingTime1().getMinute(), 15);
        }
        // Create the judging sessions
        LocalTime[] judgingTimes = schedulerInfo.getJudgingTimes();
        int judgingTimeIndex = 0;
        LocalTime nextJudgingTime = judgingTimes[judgingTimeIndex++];
        slot = scheduleSlot(nextJudgingTime);
        int judgingPanels = schedulerInfo.getJudgingPanelNum();
        int judgingRoom = 0;
        ScheduleSlot.Location location;
        for (int t=0; t < teamCount; t++) {
            location = ScheduleSlot.getJudgingLocation(judgingRoom);
            Team team = teamList.getTeam(t);
            // schedule the team for all of the timeslots
            schedule[slot][t] = new ScheduleSlot(team.getTeamNumber(), ScheduleSlot.SlotType.JUDGING, location, nextJudgingTime.getHour(), nextJudgingTime.getMinute(), schedulerInfo.getJudgingDuration().getMinute());
            schedule[slot+1][t] = new ScheduleSlot(team.getTeamNumber(), ScheduleSlot.SlotType.JUDGING, location, nextJudgingTime.getHour(), nextJudgingTime.getMinute(), schedulerInfo.getJudgingDuration().getMinute());
            if (judgingRoom < judgingPanels - 1) {
                judgingRoom++;
            } else {
                if (judgingTimeIndex < judgingTimes.length) {
                    nextJudgingTime = judgingTimes[judgingTimeIndex];
                    judgingTimeIndex++;
                    slot = scheduleSlot(nextJudgingTime);
                    judgingRoom = 0;
                } else {
                    if (t+1 < teamCount) {
                        System.out.println("Too few Judging times/rooms");
                    }
                }
            }
        }

        int gameTablePairs = schedulerInfo.getNumRobotGameTablePairs();
        int matchDuration = schedulerInfo.getMatchAlternateTIme().getMinute();
        /**
         * Schedule the practice rounds
         *
         * Start the practice rounds with the next team that is not in judging and continue to the end of the list of teams,
         * then schedule the beginning of the list through the teams that are in judging at the later time.
         */
        int roundStartTeam = judgingPanels;
        int roundEndTeam = teamCount;
        LocalTime roundStartTime = schedulerInfo.getPracticeMatchTime1();
        LocalTime matchEndTime = setPracticeSchedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);
        // The group that was in judging
        roundStartTeam = 0;     
        roundEndTeam = judgingPanels;
        roundStartTime = schedulerInfo.getPracticeMatchTime2();
        matchEndTime = setPracticeSchedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);

        /**
         * Schedule the competitions for Round1
         * 
         * Start the Round 1 with the team after the latest team not in judging and continue to the end of the list of teams,
         * then schedule the beginning of the list through the teams that are in judging at the later time.
         */
        roundEndTeam = teamCount;
        roundStartTime = schedulerInfo.getRound1MatchTime1();
        if (roundStartTime.isBefore(matchEndTime)) {
            roundStartTime = matchEndTime;
        }
        slot = scheduleSlot(roundStartTime);
        roundStartTeam = getHighestTeamPosInJudging(teamList, slot) + 1;
        System.out.println("Round1 Starts at " + roundStartTime + " with team " + roundStartTeam + " and goes through team " + roundEndTeam);
        matchEndTime = setRound1Schedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);
        // Up through the group that was in judging
        roundEndTeam = roundStartTeam;
        roundStartTime = schedulerInfo.getRound1MatchTime2();
        if (roundStartTime.isBefore(matchEndTime)) {
            roundStartTime = matchEndTime;
        }
        slot = scheduleSlot(roundStartTime);
        roundStartTeam = 0;
        System.out.println("Round1 Starts at " + roundStartTime + " with team " + roundStartTeam + " and goes through team " + roundEndTeam);
        matchEndTime = setRound1Schedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);

        /**
         * Schedule the competitions for Round2
         * 
         * Start the Round 2 with the team after the latest team not in judging and continue to the end of the list of teams,
         * then schedule the beginning of the list through the teams that are in judging at the later time.
         */
        roundEndTeam = teamCount;
        roundStartTime = schedulerInfo.getRound2MatchTime1();
        if (roundStartTime.isBefore(matchEndTime)) {
            roundStartTime = matchEndTime;
        }
        slot = scheduleSlot(roundStartTime);
        roundStartTeam = getHighestTeamPosInJudging(teamList, slot) + 1;
        System.out.println("Round2 Starts at " + roundStartTime + " with team " + roundStartTeam + " and goes through team " + roundEndTeam + " with a match end time of " + matchEndTime);
        matchEndTime = setRound2Schedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);
        // Up through the group that was in judging
        roundEndTeam = roundStartTeam;
        roundStartTime = schedulerInfo.getRound2MatchTime2();
        if (roundStartTime.isBefore(matchEndTime)) {
            roundStartTime = matchEndTime;
        }
        slot = scheduleSlot(roundStartTime);
        roundStartTeam = 0;
        System.out.println("Round2 Starts at " + roundStartTime + " with team " + roundStartTeam + " and goes through team " + roundEndTeam+ " with a match end time of " + matchEndTime);
        matchEndTime = setRound2Schedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);

        /**
         * Schedule the competitions for Round3
         * 
         * Start the Round 3 with the team after the latest team not in judging and continue to the end of the list of teams,
         * then schedule the beginning of the list through the teams that are in judging at the later time.
         */
        roundEndTeam = teamCount;
        roundStartTime = schedulerInfo.getRound3MatchTime1();
        if (roundStartTime.isBefore(matchEndTime)) {
            roundStartTime = matchEndTime;
        }
        slot = scheduleSlot(roundStartTime);
        roundStartTeam = getHighestTeamPosInJudging(teamList, slot) + 1;
        System.out.println("Round3 Starts at " + roundStartTime + " with team " + roundStartTeam + " and goes through team " + roundEndTeam + " with a match end time of " + matchEndTime);
        matchEndTime = setRound3Schedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);
        // Up through the group that was in judging
        roundEndTeam = roundStartTeam;
        roundStartTime = schedulerInfo.getRound3MatchTime2();
        if (roundStartTime.isBefore(matchEndTime)) {
            roundStartTime = matchEndTime;
        }
        slot = scheduleSlot(roundStartTime);
        roundStartTeam = 0;
        System.out.println("Round3 Starts at " + roundStartTime + " with team " + roundStartTeam + " and goes through team " + roundEndTeam+ " with a match end time of " + matchEndTime);
        matchEndTime = setRound3Schedule(teamList, roundStartTime, matchDuration, roundStartTeam, roundEndTeam, gameTablePairs);
    }

    public String toString() {
        String ret = "";
        for (int t = 0; t < timeSlots; t++) {
            for (int team = 0; team < teamCount + 1; team++) {
                try {
                    ret = ret + "For timeslot " + t + " " + schedule[t][team].toString() + "\n";
                }
                catch (NullPointerException e){
                    // timeslot doesn't exist so stop processing
                    // t = timeSlots;
                    // team = teamCount;
                }
            }
        }
        return ret;
    }

    private LocalTime addHourMinutes(LocalTime t1, LocalTime t2) {
        LocalTime newTime;
        int hours = t1.getHour() + t2.getHour();
        int minutes = t1.getMinute() + t2.getMinute();
    
        if (minutes >= 60) {
            hours++;
            minutes -= 60;
        }
        newTime = LocalTime.of(hours, minutes);

        System.out.println("addHourMinutes: t1: " + t1 + " t2: " + t2 + " is now " + newTime);
        return newTime;
    }

    private int scheduleSlot(LocalTime t) {
        int minutes = (t.getHour() * 60 + t.getMinute()) - (dayStartTime.getHour() * 60 + dayStartTime.getMinute());

        return (int) Math.floorDiv(minutes, scheduleMinutes);
    }

    /**
     * getHighestTeamPosInJudging()
     * 
     * Get the highest array position in the schedule for the team in the schedule
     * @param teamList
     * @param slot
     * @return
     */

    private int getHighestTeamPosInJudging(TeamList teamList, int slot) {
        int highTeamSLot = 0;
        boolean found = false;

        if (slot < 0) {
            return -1;
        }
        for (int t = 0; t < teamList.getSize(); t++) {
            try {                
                if (schedule[slot][t].isJudgingSlot()) {
                    System.out.println("Slot " + slot + " has team " + t + " in judging");
                    if (t >= highTeamSLot) {
                        highTeamSLot = t;
                        found = true;
                    }
                }
            } catch ( Exception e) {
                // nothing scheduled at this slot
            }
        }

        if (!found) {
            System.out.println("Didn't get a team in judging so backing up a slot " + slot);
            highTeamSLot = getHighestTeamPosInJudging(teamList, slot - 1);
        }
        return highTeamSLot;
    }

    private LocalTime setPracticeSchedule(TeamList teamList, LocalTime matchTime, int matchDuration, int roundStartTeam, int roundEndTeam, int gameTablePairs) {
        for (int t = roundStartTeam; t < roundEndTeam; t += gameTablePairs*2) {
            // determine how many wildcard teams
            int wildcardTeamCount = 0;
            if (t + (gameTablePairs * 2) > roundEndTeam) {
                wildcardTeamCount = (t + (gameTablePairs * 2) - roundEndTeam);
                System.out.println("The wildcardTeamCount is " + wildcardTeamCount);
            }
            int slot = scheduleSlot(matchTime);
            Team team1;
            Team team2;
            Team team3;
            Team team4;
            switch (wildcardTeamCount) {
                case 0:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getTeam(t+3);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute() + matchDuration, matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute() + matchDuration, matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 1:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute() + matchDuration, matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute() + matchDuration, matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 2:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                case 3:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.PRACTICE_MATCH, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                default:
                    System.out.println("Invalid wildcard team count");
            }
        }
        return matchTime;
    }

    private LocalTime setRound1Schedule(TeamList teamList, LocalTime matchTime, int matchDuration, int roundStartTeam, int roundEndTeam, int gameTablePairs) {
        for (int t = roundStartTeam; t < roundEndTeam; t += gameTablePairs*2) {
            // determine how many wildcard teams
            int wildcardTeamCount = 0;
            if (t + (gameTablePairs * 2) > roundEndTeam) {
                wildcardTeamCount = (t + (gameTablePairs * 2) - roundEndTeam);
                System.out.println("The wildcardTeamCount is " + wildcardTeamCount);
            }
            int slot = scheduleSlot(matchTime);
            Team team1;
            Team team2;
            Team team3;
            Team team4;
            switch (wildcardTeamCount) {
                case 0:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getTeam(t+3);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 1:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 2:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                case 3:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH1, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                default:
                    System.out.println("Invalid wildcard team count");
            }
        }
        return matchTime;
    }

        private LocalTime setRound2Schedule(TeamList teamList, LocalTime matchTime, int matchDuration, int roundStartTeam, int roundEndTeam, int gameTablePairs) {
        for (int t = roundStartTeam; t < roundEndTeam; t += gameTablePairs*2) {
            // determine how many wildcard teams
            int wildcardTeamCount = 0;
            if (t + (gameTablePairs * 2) > roundEndTeam) {
                wildcardTeamCount = (t + (gameTablePairs * 2) - roundEndTeam);
                System.out.println("The wildcardTeamCount is " + wildcardTeamCount);
            }
            int slot = scheduleSlot(matchTime);
            Team team1;
            Team team2;
            Team team3;
            Team team4;
            switch (wildcardTeamCount) {
                case 0:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getTeam(t+3);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 1:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 2:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                case 3:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH2, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                default:
                    System.out.println("Invalid wildcard team count");
            }
        }
        return matchTime;
    }

    private LocalTime setRound3Schedule(TeamList teamList, LocalTime matchTime, int matchDuration, int roundStartTeam, int roundEndTeam, int gameTablePairs) {
        for (int t = roundStartTeam; t < roundEndTeam; t += gameTablePairs*2) {
            // determine how many wildcard teams
            int wildcardTeamCount = 0;
            if (t + (gameTablePairs * 2) > roundEndTeam) {
                wildcardTeamCount = (t + (gameTablePairs * 2) - roundEndTeam);
                System.out.println("The wildcardTeamCount is " + wildcardTeamCount);
            }
            int slot = scheduleSlot(matchTime);
            Team team1;
            Team team2;
            Team team3;
            Team team4;
            switch (wildcardTeamCount) {
                case 0:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getTeam(t+3);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 1:
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    team3 = teamList.getTeam(t+2);
                    team4 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+2] = new ScheduleSlot(team3.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_REDB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+3] = new ScheduleSlot(team4.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_REDA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration*2));
                    break;
                case 2:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getTeam(t+1);
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                case 3:
                    /**
                     * Only need to schedule 2 teams, don't need a table pair with two wild card teams
                     */
                    team1 = teamList.getTeam(t);
                    team2 = teamList.getWildCardTeam();
                    schedule[slot][t] = new ScheduleSlot(team1.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEA, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    schedule[slot][t+1] = new ScheduleSlot(team2.getTeamNumber(), ScheduleSlot.SlotType.COMPETITION_MATCH3, ScheduleSlot.Location.TABLE_BLUEB, matchTime.getHour(), matchTime.getMinute(), matchDuration);
                    matchTime = addHourMinutes(matchTime, LocalTime.of(0, matchDuration));
                    break;
                default:
                    System.out.println("Invalid wildcard team count");
            }
        }
        return matchTime;
    }
}
