package wifllscheduler;

public class Team {
    private final Integer teamNumber;
    private final String teamName;
    
    public Team(Integer teamNumber, String teamName) {
        this.teamNumber = teamNumber;
        this.teamName = teamName;
    }

    /**
     * Return the team name
     * @return
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Return the team number
     * @return
     */
    public Integer getTeamNumber() {
        return teamNumber;
    }

    /**
     * Return a string representing the team
     */
    public String toString() {
        return teamNumber + " - " + teamName;
    }
}
