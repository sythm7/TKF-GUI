package fr.sythm.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class SimpleScoreboard {

    private final Scoreboard scoreboard;

    private final Criteria criteria;

    private final Component title;

    private Objective sidebar;

    private Objective belowName;

    private final List<String> lines;

    /** Creates a simple scoreboard with a sidebar objective using dummy criteria
     * @param title The display name of the sidebar objective
     * @param lines The lines to display on the scoreboard
     */
    public SimpleScoreboard(Component title, List<String> lines) {
        this(Criteria.DUMMY, title, lines);
    }

    /** Creates a simple scoreboard with a sidebar objective
     * @param criteria The criteria of the sidebar objective
     * @param title The display name of the sidebar objective
     * @param lines The lines to display on the scoreboard
     */
    public SimpleScoreboard(Criteria criteria, Component title, List<String> lines) {
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
    public void setLines(List<String> lines) {

        // Unregister the previous sidebar objective if it exists
        if(this.sidebar != null) {
            this.sidebar.unregister();
        }

        // Create a new sidebar objective
        this.sidebar = this.scoreboard.registerNewObjective("_tkf_sidebar", criteria, title);
        int scoreID = lines.size() - 1;
        // Set each line with a decreasing score to have them in the correct order
        for(String line : lines) {
            this.sidebar.getScore(line).setScore(scoreID--);
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
     */
    public void replaceLine(int lineID) {
        this.sidebar.getScore(this.lines.get(lines.size() - 1 - lineID));
    }

    /** Sets the below name objective of the scoreboard, replacing the previous one if any
     * @param criteria The criteria of the below name objective
     * @param information The display name of the below name objective
     */
    public void setBelowName(Criteria criteria, Component information) {
        if(this.belowName != null) {
            this.belowName.unregister();
        }
        this.belowName = this.scoreboard.registerNewObjective("_tkf_below_name", criteria, information);
        this.belowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    /** Sets the below name objective of the scoreboard to display the player's health with a red heart symbol,
     * replacing the previous below name objective if any
     */
    public void setBelowNameHealth() {
        this.setBelowName(Criteria.HEALTH, Component.text("❤").color(TextColor.color(Color.RED.asRGB())));
    }
}
