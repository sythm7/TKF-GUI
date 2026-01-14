package fr.sythm.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;

/** A simple scoreboard manager that allows to create a sidebar scoreboard with multiple lines
 * and an optional below name objective
 */
public class SimpleScoreboard {

    /** The Bukkit scoreboard instance */
    private final Scoreboard scoreboard;

    /** The criteria of the sidebar objective */
    private final Criteria criteria;

    /** The title of the sidebar objective */
    private final Line title;

    /** The sidebar objective */
    private Objective sidebar;

    /** The below name objective */
    private Objective belowName;

    /** The lines of the scoreboard */
    private final List<Line> lines;

    /** Creates a simple scoreboard with a sidebar objective using dummy criteria
     * @param title The display name of the sidebar objective
     * @param lines The lines to display on the scoreboard
     */
    public SimpleScoreboard(Line title, List<Line> lines) {
        this(Criteria.DUMMY, title, lines);
    }

    /** Creates a simple scoreboard with a sidebar objective
     * @param criteria The criteria of the sidebar objective
     * @param title The display name of the sidebar objective
     * @param lines The lines to display on the scoreboard
     */
    public SimpleScoreboard(Criteria criteria, Line title, List<Line> lines) {
        this.criteria = criteria;
        this.title = title;
        this.lines = lines;
        // Create a new scoreboard
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.setLines(lines);
    }

    /** Sets the lines of the scoreboard, replacing the previous ones if any
     * @param lines The lines to set on the scoreboard
     */
    public void setLines(List<Line> lines) {

        // Unregister the previous sidebar objective if it exists
        if(this.sidebar != null) {
            this.sidebar.unregister();
        }

        // Create a new sidebar objective
        this.sidebar = this.scoreboard.registerNewObjective("_tkf_sidebar", criteria, title.asComponent());
        int scoreID = lines.size() - 1;
        // Set each line with a decreasing score to have them in the correct order
        for(Line line : lines) {

            Score score = this.sidebar.getScore(String.valueOf(scoreID));
            score.setScore(scoreID--);
            score.customName(line.asComponent());
        }
        // Make the numbers disappear from the scoreboard
        this.sidebar.numberFormat(NumberFormat.blank());
        // Set the scoreboard to display in the sidebar
        this.sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /** Assigns the scoreboard to a player
     * @param player The player to whom we assign the scoreboard
     */
    public void assign(Player player) {
        player.setScoreboard(this.scoreboard);
    }

    /** Replaces a specific line of the scoreboard
     * @param lineID The ID of the line to replace
     * @param newLine The new line to set
     */
    public void replaceLine(int lineID, Line newLine) {
        this.sidebar.getScore(String.valueOf(this.lines.size() - 1 - lineID)).customName(newLine.asComponent());
    }

    /** Sets the below name objective of the scoreboard, replacing the previous one if any
     * @param criteria The criteria of the below name objective
     * @param information The display name of the below name objective
     */
    public void setBelowName(Criteria criteria, Line information) {
        if(this.belowName != null) {
            this.belowName.unregister();
        }
        this.belowName = this.scoreboard.registerNewObjective("_tkf_below_name", criteria, information.asComponent());
        this.belowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    /** Sets the below name objective of the scoreboard to display the player's health with a red heart symbol,
     * replacing the previous below name objective if any
     */
    public void setBelowNameHealth() {
        this.setBelowName(Criteria.HEALTH, new Line("❤", Color.RED));
    }

    /** Gets the lines of the scoreboard
     * @return The lines of the scoreboard
     */
    public List<Line> getLines() {
        return lines;
    }
}
