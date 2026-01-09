package fr.sythm.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Allows the user to implement a custom action for a {@link Button}.
 */
public interface Action {

    /**
     * Executes the custom action that will be specified inside
     * @param player The {@link Player} who triggered the button click event
     */
    void execute(Player player, Event event);
}