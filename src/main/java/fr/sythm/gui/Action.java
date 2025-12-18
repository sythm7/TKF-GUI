package fr.sythm.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Allows the user to implement a custom action for a {@link Button}.
 */
public interface Action {

    /**
     * Executes the custom action that will be specified inside
     * @param player The {@link Player} who triggered the button click event
     * @param itemStack The {@link ItemStack} that was clicked on by the {@link Player}
     */
    void execute(Player player, ItemStack itemStack);
}
