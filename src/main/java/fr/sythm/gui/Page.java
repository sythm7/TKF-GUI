package fr.sythm.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used as a UI manager. Its function is to create an {@link Inventory}, add some {@link Button} into it,
 * and make it act as a real interface by blocking some user interactions and adding new user interactions.
 */
public class Page {

    private static Plugin plugin;

    private final String title;

    private final int rowCount;

    private final Inventory inventory;

    /**
     * 9 is the number of columns contained into an inventory, it cannot be changed.
     */
    private final int COLUMN_COUNT = 9;

    /**
     * Key-value association of a {@link Button} and its position in an inventory.
     */
    private final Map<Button, Integer> buttonMap;


    /**
     * Initialize an empty {@link Page} with the specified title and row count
     * @param title The title that will be displayed on top of the opened {@link Inventory}
     * @param rowCount The number of rows that will be displayed in the {@link Inventory}
     */
    public Page(String title, int rowCount) {

        /*
         * Retrieve the plugin instance by finding from which plugin this class is executed, then register all the Page events.
         * It's a mandatory operation as it's impossible to register an event without access to the corresponding plugin.
         */
        if(plugin == null) {
            plugin = JavaPlugin.getProvidingPlugin(Page.class);
            plugin.getServer().getPluginManager().registerEvents(new TKFInventoryListener(), plugin);
        }
        this.title = title;
        this.rowCount = rowCount;
        // Inventory size isn't counted as rows here but as number of Inventory slots, so it's rows * columns.
        this.inventory = Bukkit.createInventory(new PageHolder(), rowCount * COLUMN_COUNT, Component.text(this.title));
        this.buttonMap = new HashMap<>();
    }

    /**
     * Allows the user to add a {@link Button} to this {@link Page}.
     * This method also registers the {@link Button} event and its corresponding action, if one was specified.
     * @param button The {@link Button} to add to the {@link Page}
     * @param pos The position of the {@link Button} on the {@link Page}.
     * @throws IndexOutOfBoundsException This exception is thrown if the specified {@param pos} is higher than the number of available slots.
     */
    public void addButton(Button button, int pos) throws IndexOutOfBoundsException    {
        if(pos >= rowCount * COLUMN_COUNT) {
            throw new IndexOutOfBoundsException("Button position is out of range. " + pos + " >= " + rowCount * COLUMN_COUNT);
        }
        this.buttonMap.put(button, pos);

        // Add the item contained in the Button into the Inventory
        this.inventory.setItem(pos, button.getItemStack());
    }

    public void updateButton(Button button) {

        Integer pos = buttonMap.get(button);

        if(pos == null) {
            plugin.getSLF4JLogger().warn("The button '{}' is not in the page '{}'.", button.getDisplayName(), this.title);
            return;
        }

        this.inventory.setItem(pos, button.getItemStack());
    }

    /**
     * Get the {@link Inventory} contained in the {@link Page}.
     * For example, allows the user to open an {@link Inventory} after having built a {@link Page}.
     * @return The {@link Inventory} contained in this {@link Page}
     */
    public Inventory getInventory() {
        return inventory;
    }


    /*
    -------------------------------------------
    Private operations, not useful for the user
     */

    /**
     * This class adds all the necessary events for blocking non-desirable user interactions.
     */
    private static class TKFInventoryListener implements Listener {

        /**
         * Blocks every user action consisting of moving an item out of the {@link Inventory}, or moving an item inside the {@link Inventory}.
         * @param event The {@link InventoryClickEvent} that was triggered
         */
        @EventHandler
        private void onInventoryClickEvent(InventoryClickEvent event){

            /*
            If the holder is an instance of PageHolder, that means we're dealing with a custom inventory
            built with this library.
             */
            if(! (event.getInventory().getHolder() instanceof PageHolder)) {
                return;
            }

            // If no inventory was clicked, nothing happened, we do nothing.
            if(event.getClickedInventory() == null) {
                return;
            }

            // If the clicked inventory is not the Player's inventory, that means the Page was clicked on.
            // We cancel that event to prevent items from being moved in or out the Page. Shift click is also cancelled.
            if(! event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
                event.setCancelled(true);
            } else {
                if(event.isShiftClick()) {
                    event.setCancelled(true);
                }
            }
        }

        /**
         * It's a specific case where spam-clicking is considered drag-clicking somehow
         * @param event The {@link InventoryDragEvent} that was triggered
         */
        @EventHandler
        private void onInventoryDragEvent(InventoryDragEvent event){

            /*
            If the holder is an instance of PageHolder, that means we're dealing with a custom inventory
            built with this library, so we cancel the Drag event.
             */
            if(! (event.getInventory().getHolder() instanceof PageHolder)) {
                return;
            }
            event.setCancelled(true);
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

    /**
     * This class is used as a better way to recognize when a {@link Page} event is triggered
     * by checking if the opened inventory has an instance of {@link PageHolder} instead of comparing inventories title names.
     */
    private static class PageHolder implements InventoryHolder {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
