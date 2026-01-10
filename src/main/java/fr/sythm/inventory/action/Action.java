package fr.sythm.inventory.action;

import fr.sythm.inventory.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Allows the user to implement a custom action for a {@link Button}.
 */
public interface Action {

    /**
     * Executes the custom action that will be specified inside
     * @param player The {@link Player} who triggered the button click event
     * @param event The {@link Event} that triggered the action,
     * which can be an {@link org.bukkit.event.inventory.InventoryClickEvent} (see {@link Button})
     * or an {@link org.bukkit.event.player.PlayerInteractEvent} (see {@link fr.sythm.inventory.UsableItem})
     */
    void execute(Player player, Event event);
}