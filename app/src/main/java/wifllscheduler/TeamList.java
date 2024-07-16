package wifllscheduler;

import java.util.ArrayList;

public class TeamList {
    private ArrayList<Team> teams;
    private Team wildCardTeam;

    /**
     * Contruct an team list from an excel file
     */
    public TeamList(String filename) {
   
        ExcelFileReader excelFile;
        try {
            excelFile = new ExcelFileReader(filename);
            teams = excelFile.getTeams();
        } catch ( Exception e) {
            System.out.println("Got exception" + e);
            System.exit(-1);
        }
        addWildCardTeam();
    }

    public TeamList(ExcelFileReader excelFile) {
   
        teams = excelFile.getTeams();
        addWildCardTeam();
    }


    /**
     * Construct a generic team list based on team count
     */
    public TeamList(int numberOfTeams) {
        teams = new ArrayList<Team>();

        for (int t = 0; t < numberOfTeams; t++){
            teams.add(new Team(t, String.format("Team %02d", t)));
        }

        addWildCardTeam();
    }


    /**
     * Returns a string representing the team
     */
    public String toString() {
        return teams.toString();
    }

    /**
     * Add the team to the list
     */
    public void addTeam(Team team) {
        teams.add(team);
    }

    /**
     * Get the number of teams
     */
    public int getSize() {
        return teams.size();
    }

    /**
     * Get a team
     */
    public Team getTeam(int team){
        return teams.get(team);
    }

    /**
     * Get the list of teams
     */
    public ArrayList<Team> getTeamList() {
        return teams;
    }

    /**
     * handle the wildcard team
     */
    public Team getWildCardTeam() {
        return wildCardTeam;
    }
    
    public void addWildCardTeam() {
        wildCardTeam = new Team(999999,"Wild CardTeam");
    }
}
