package fr.sythm.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This class makes a {@link Button} being a usable Minecraft item, which means the item can be given into a player's inventory
 * and can be used from the player's hand.
 */
public class UsableItem extends Button {

    /**
     * Adapted for building a {@link UsableItem}
     */
    public static final class Builder extends GenericBuilder<Builder> {

        /**
         * Initializes a {@link Builder} with the {@link Material} as the only mandatory feature that a {@link UsableItem} will use.
         * Other features are optional, and depends on what usage the user wants to create for his {@link UsableItem}.
         *
         * @param material The in game item ID
         */
        public Builder(Material material) {
            super(material);
        }

        @Override
        public UsableItem build() {
            return new UsableItem(this.material, this.displayName, this.color, this.lore, this.isEnchanted, this.action);
        }
    }

    /**
     * Give the {@link UsableItem} to the selected {@link Player}
     * @param player The selected {@link Player}
     * @param inventoryPos The position which the item will be put in the player's inventory
     */
    public void give(Player player, int inventoryPos) {
        this.registerEvent(new ButtonListener(this));
        player.getInventory().setItem(inventoryPos, this.getItemStack());
    }

    /**
     * Private {@link UsableItem} constructor as it would be nonsense to allow the user to use this publicly, because we don't need to initialize
     * every attribute of this class. Instead, the user has to use the {@link Builder} class because it allows him to decide whenever he
     * wants to use an option for his {@link UsableItem}.
     * @param material The in game item ID
     * @param displayName Custom name that will be displayed for an {@link ItemStack}
     * @param color The color that will be applied on the displayed name
     * @param lore The description that will be shown for an {@link ItemStack}
     * @param isEnchanted Specifies if the {@link ItemStack} will have an enchantment effect displayed on it
     * @param action Specifies the action to be triggered on a {@link UsableItem} click
     */
    private UsableItem(Material material, String displayName, Color color, List<String> lore, boolean isEnchanted, Action action) {
        super(material, displayName, color, lore, isEnchanted, action);
    }

    /**
     * private class, used for creating the {@link UsableItem} event
     * and automatize the execution of its associated action.
     * When an event is triggered, it checks if the clicked item was indeed the specified {@link UsableItem}
     * and if yes, executes the action specified by the user.
     */
    protected static class ButtonListener extends Button.ButtonListener {

        /**
         * Initializes a {@link ButtonListener}
         * @param usableItem The button linked to this {@link Listener}
         */
        public ButtonListener(UsableItem usableItem) {
            super(usableItem);
        }

        /**
         * Handles the events related to item utilisation (left-click and right-click) by a {@link Player}
         * @param event The {@link PlayerInteractEvent} which is triggered by a {@link Player}
         */
        @EventHandler
        public void onHandItemClick(PlayerInteractEvent event) {
            Player player = event.getPlayer();

            ItemStack itemStack = event.getItem();

            if(isButtonClicked(itemStack)) {
                event.setCancelled(true);
                this.executeAction(player, itemStack);
            }
        }
    }

    @EventHandler
    public void onItemDrag(InventoryDragEvent event) {

        if(! (event.getWhoClicked() instanceof Player player)) return;

    }

}
