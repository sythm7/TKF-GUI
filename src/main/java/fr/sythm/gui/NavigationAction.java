package fr.sythm.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Allows the user to add an action for a {@link Button} that makes the player navigate to another inventory when the button is clicked.
 */
public class NavigationAction implements Action {

    private final Page nextPage;

    /**
     * Initialize the navigation action by specifying its associated page.
     * @param nextPage The page that will be opened on a button click
     */
    public NavigationAction(Page nextPage) {
        this.nextPage = nextPage;
    }

    /**
     * This method will automatically be executed as the button click event occurs
     * @param player The {@link Player} to perform the button click
     * @param itemStack The {@link ItemStack} which was clicked on
     */
    @Override
    public void execute(Player player, ItemStack itemStack) {
        player.openInventory(this.nextPage.getInventory());
    }
}
