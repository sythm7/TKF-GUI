package fr.sythm.gui;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This class makes a {@link Button} being a usable Minecraft item, which means the item can be given into a player's inventory
 * and can be used from the player's hand.
 */
public class UsableItem extends Button {

    private static boolean initialized = false;

    /**
     * Adapted for building a {@link UsableItem}
     */
    public static final class Builder extends GenericBuilder<Builder> {

        // Specifies if the UsableItem can be clicked through an inventory
        private boolean clickableOnInventory;

        /**
         * Initializes a {@link Builder} with the {@link Material} as the only mandatory feature that a {@link UsableItem} will use.
         * Other features are optional, and depends on what usage the user wants to create for his {@link UsableItem}.
         *
         * @param material The in game item ID
         */
        public Builder(Material material) {
            super(material);
            this.clickableOnInventory = true;
        }

        /**
         * Adds an {@link Action} specified by the user. It can be either a {@link NavigationAction} or any other custom {@link Action} implemented by the user.
         * @param action Specifies the {@link Action} to be triggered when a {@link UsableItem} is used.
         * @param clickableOnInventory Specifies if the {@link Action} can be triggered through an {@link org.bukkit.event.inventory.InventoryClickEvent}
         * @return The updated {@link Builder}
         */
        public Builder withAction(Action action, boolean clickableOnInventory) {
            this.action = action;
            this.clickableOnInventory = clickableOnInventory;
            return this;
        }

        /**
         * Builds the final {@link UsableItem} instance from the configuration defined by the user in this {@link Builder}.
         * @return The {@link UsableItem} instance built with its proper configuration
         */
        @Override
        public UsableItem build() {
            return new UsableItem(this.material, this.displayName, this.color, this.lore, this.isEnchanted, this.action, this.clickableOnInventory);
        }
    }

    /**
     * Give the {@link UsableItem} to the selected {@link Player}
     * @param player The selected {@link Player}
     * @param inventoryPos The position which the item will be put in the player's inventory
     */
    public void give(Player player, int inventoryPos) {
        player.getInventory().setItem(inventoryPos, this.getItemStack());
    }

    /**
     * Private {@link UsableItem} constructor as it would be nonsense to allow the user to use this publicly, because we don't need to initialize
     * every attribute of this class. Instead, the user has to use the {@link Builder} class because it allows him to decide whenever he
     * wants to use an option for his {@link UsableItem}.
     *
     * @param material             The in game item ID
     * @param displayName          Custom name that will be displayed for an {@link ItemStack}
     * @param color                The color that will be applied on the displayed name
     * @param lore                 The description that will be shown for an {@link ItemStack}
     * @param isEnchanted          Specifies if the {@link ItemStack} will have an enchantment effect displayed on it
     * @param action               Specifies the action to be triggered on a {@link UsableItem} click
     * @param clickableOnInventory Specifies if the {@link UsableItem} can be clicked through the inventory
     */
    private UsableItem(Material material, String displayName, Color color, List<String> lore, boolean isEnchanted, Action action, boolean clickableOnInventory) {
        super(material, displayName, color, lore, isEnchanted, action);

        if(! initialized) {
            // Register the Listener common to every UsableItem. This operation is called only once.
            plugin.getServer().getPluginManager().registerEvents(new UsableItemListener(), plugin);
            initialized = true;
        }

        if(! clickableOnInventory) {
            // Disable the Action being executed in the InventoryClickEvent for this UsableItem
            this.disableInventoryClick();
        }
    }

    /**
     * private class, used for creating the {@link UsableItem} event
     * and automatize the execution of its associated action.
     * When an event is triggered, it checks if the clicked item was indeed the specified {@link UsableItem}
     * and if yes, executes the action specified by the user.
     */
    private static class UsableItemListener implements Listener {

        /**
         * Handles the events related to item utilisation (left-click and right-click) by a {@link Player}
         * @param event The {@link PlayerInteractEvent} which is triggered by a {@link Player}
         */
        @EventHandler
        public void onHandItemClick(PlayerInteractEvent event) {
            Player player = event.getPlayer();

            ItemStack itemStack = event.getItem();

            Action action = getAction(itemStack);

            if(action != null) {
                action.execute(player, itemStack);
            }
        }

        /**
         * Correctly reset things when the plugin including this library disables itself
         * and avoid problems when reloading plugins
         * @param event The {@link PluginDisableEvent} which was triggered
         */
        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            // Unregister all events related to this Listener
            HandlerList.unregisterAll(this);
        }
    }
}
