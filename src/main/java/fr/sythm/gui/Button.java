package fr.sythm.gui;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class transforms a simple {@link ItemStack} in a clickable {@link Button} with its own customized actions.
 */
public class Button {

    // Makes a unique ID everytime a Button is created.
    private static int buttonIndexing = 0;
    // Used for creating a custom attribute where we can put the Button ID in.
    private static NamespacedKey BUTTON_ID_KEY;

    // Used for specific use case (for example, a UsableItem can disable clicks from inventory)
    private static NamespacedKey CLICKABLE_ON_INVENTORY_KEY;

    /**
     * We initialize a Map and instead of registering events for each button created, we only register 1 event,
     * and when the event is triggered we retrieve the Action corresponding to the id of the clicked Button
     */
    protected static Map<Integer, Action> buttonIdActionMap = new HashMap<>();

    /**
     * This generic class is used to create a {@link Button} in the simplest way, using a Builder Pattern.
     * The purpose of this generic class is to be extended for a specific use case (see {@link Button.Builder} and {@link UsableItem.Builder})
     * @param <T> The Builder inheriting {@link GenericBuilder}
     */
    protected static abstract class GenericBuilder<T extends  GenericBuilder<T>> {

        /**
         * In game item name/ID
         */
        protected final Material material;
        /**
         * Custom item name that will be displayed
         */
        protected String displayName;
        /**
         * Display name color
         */
        protected Color color;
        /**
         * Item description
         */
        protected List<String> lore;
        /**
         * Enchantment glint effect ON or OFF
         */
        protected boolean isEnchanted;
        /**
         * Custom action on a Button click
         */
        protected Action action;

        /**
         * Initializes a {@link GenericBuilder} with the {@link Material} as the only mandatory feature that a {@link Button} will use.
         * Other features are optional, and depends on what usage the user wants to create for his {@link Button}.
         * @param material The in game item ID
         */
        public GenericBuilder(Material material) {
            this.material = material;
            this.isEnchanted = false;
        }

        /**
         * Cast the {@link GenericBuilder} instance into the type {@link T} of the calling subclass
         * @return The instance {@link T} inheriting {@link GenericBuilder}
         */
        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        /**
         * Adds a display name to the {@link T} Builder configuration.
         * @param displayName Custom name that will be displayed for an {@link ItemStack}
         * @return The updated {@link T} Builder
         */
        public T withDisplayName(String displayName) {
            this.displayName = displayName;
            return this.self();
        }

        /**
         * Adds a colored display name to the {@link T} Builder configuration.
         * @param displayName Custom name that will be displayed for an {@link ItemStack}
         * @param color The color that will be applied on the displayed name
         * @return The updated {@link T} Builder
         */
        public T withDisplayName(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
            return this.self();
        }

        /**
         * Adds a description to the {@link T} Builder configuration.
         * @param lore The description that will be shown for an {@link ItemStack}
         * @return The updated {@link T} Builder
         */
        public T withLore(List<String> lore) {
            this.lore = lore;
            return this.self();
        }

        /**
         * Specifies the usage of the enchanted effect into the {@link T} Builder configuration.
         * @param isEnchanted Specifies if the {@link ItemStack} will have an enchantment effect displayed on it.
         * @return The updated {@link T} Builder
         */
        public T withEnchantedEffect(boolean isEnchanted) {
            this.isEnchanted = isEnchanted;
            return this.self();
        }

        /**
         * Adds an {@link Action} specified by the user. It can be either a {@link NavigationAction} or any other custom {@link Action} implemented by the user.
         * @param action Specifies the action to be triggered on a {@link Button} click
         * @return The updated {@link T} Builder
         */
        public T withAction(Action action) {
            this.action = action;
            return this.self();
        }

        /**
         * Builds the final {@link Button} instance from the configuration defined by the user in this {@link GenericBuilder}.
         * @return The {@link Button} instance built with its proper configuration
         */
        public Button build() {
            return new Button(this.material, this.displayName, this.color, this.lore, this.isEnchanted, this.action);
        }
    }

    /**
     * Creates a {@link Button} in the simplest way, using a Builder Pattern.
     */
    public static final class Builder extends GenericBuilder<Builder> {

        /**
         * Initializes a {@link Builder} with the {@link Material} as the only mandatory feature that a {@link Button} will use.
         * Other features are optional, and depends on what usage the user wants to create for his {@link Button}.
         *
         * @param material The in game item ID
         */
        public Builder(Material material) {
            super(material);
        }
    }

    // In game item name/ID
    private Material material;
    // Custom item name that will be displayed
    private String displayName;
    // Display name color
    private Color color;
    // Item description
    private List<String> lore;
    // Enchantment glint effect ON or OFF
    private boolean isEnchanted;
    // Custom action on a Button click
    private final Action action;
    // Unique ID assigned to the Button
    private final int buttonID;
    // The ItemStack contained in that Button
    private ItemStack itemStack;
    // The ItemMeta contained in that Button
    private ItemMeta itemMeta;

    /**
     * The plugin instance, used to register events
     */
    protected static Plugin plugin;

    /**
     * Protected {@link Button} constructor as it would be nonsense to allow the user to use this publicly, because we don't need to initialize
     * every attribute of this class. Instead, the user has to use the {@link GenericBuilder} class because it allows him to decide whenever he
     * wants to use an option for his {@link Button}.
     *
     * @param material    The in game item ID
     * @param displayName Custom name that will be displayed for an {@link ItemStack}
     * @param color       The color that will be applied on the displayed name
     * @param lore        The description that will be shown for an {@link ItemStack}
     * @param isEnchanted Specifies if the {@link ItemStack} will have an enchantment effect displayed on it
     * @param action      Specifies the action to be triggered on a {@link Button} click
     */
    protected Button(Material material, String displayName, Color color, List<String> lore, boolean isEnchanted, Action action) {

        this.material = material;
        this.itemStack = ItemStack.of(material);
        // Retrieve the ItemMeta (the properties) of an ItemStack, allows us to redefine various options of the ItemStack.
        this.itemMeta = this.itemStack.getItemMeta();

        // Get the custom item name if specified by the user, otherwise retrieve the default item name
        this.displayName = displayName;

        this.color = color;

        this.lore = lore;
        this.isEnchanted = isEnchanted;
        this.action = action;

        // Assign a unique ID to this Button.
        this.buttonID = buttonIndexing;
        buttonIndexing++;

        /*
         * Retrieve the plugin instance by finding from which plugin this class is executed, then register the Button event.
         * It's a mandatory operation as it's impossible to register an event without access to the corresponding plugin.
         */
        if(plugin == null) {
            plugin = JavaPlugin.getProvidingPlugin(Button.class);
            // Register the Listener common to every Button. This operation is called only once.
            plugin.getServer().getPluginManager().registerEvents(new ButtonListener(), plugin);
            // Create a new property that will contain our uniques buttonIDs.
            BUTTON_ID_KEY = new NamespacedKey(plugin, "button_id");
            // Create a new property that will specify if we want to enable clicks from inventory or not
            CLICKABLE_ON_INVENTORY_KEY = new NamespacedKey(plugin, "clickable_on_inventory");
        }

        // Inject the BUTTON_ID_KEY and CLICKABLE_ON_INVENTORY_KEY into the ItemMeta
        this.itemMeta.getPersistentDataContainer().set(BUTTON_ID_KEY, PersistentDataType.INTEGER, this.buttonID);
        // 'true' means we want to enable clicks from inventory by default
        this.itemMeta.getPersistentDataContainer().set(CLICKABLE_ON_INVENTORY_KEY, PersistentDataType.BOOLEAN, true);

        // Update the ItemMeta contained in this ItemStack
        this.itemStack.setItemMeta(this.itemMeta);

        if(this.action != null) {
            buttonIdActionMap.put(this.buttonID, this.action);
        }

        // Apply the displayName if specified by the user, then apply the color if specified by the user.
        this.itemMeta.customName(this.displayName != null ? Component.text(this.displayName).color(this.color != null ? TextColor.color(this.color.asRGB()) : null) : null);
        // If the lore is specified by the user, transform this.lore (List<String>) into a List of Component (List<Component)
        List<Component> componentList = this.lore != null ? Lists.transform(this.lore, Component::text) : null;

        // Pass the newly created List<Component> as an argument.
        this.itemMeta.lore(componentList);
        // Enable the enchanted effect on the item if this.isEnchanted is true, otherwise it won't change anything.
        this.itemMeta.setEnchantmentGlintOverride(this.isEnchanted);
        /*
        We need to put the updated ItemMeta back to its corresponding ItemStack, because getItemMeta() gave us a clone ItemMeta,
        which means it was not assigned to the ItemStack anymore.
        */
        // Set the properties of the ItemStack using its corresponding ItemMeta
        this.itemStack.setItemMeta(this.itemMeta);
    }

    /**
     * Changes the {@link Material} displayed for this {@link Button}.
     * You have to call {@link Page#updateButton(Button)} after calling this method,
     * otherwise you won't see any change.
     * @param material The {@link Material} to set
     */
    public void setIcon(Material material) {
        this.itemStack = this.itemStack.withType(material);
        this.material = material;
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Updates the display name of this {@link Button}
     * You have to call {@link Page#updateButton(Button)} after calling this method,
     * otherwise you won't see any change.
     * @param displayName The display name to set
     */
    public void setDisplayName(String displayName) {
        this.setDisplayName(displayName, null);
    }

    /**
     * Updates the display name of this {@link Button}, with a chosen {@link Color}
     * You have to call {@link Page#updateButton(Button)} after calling this method,
     * otherwise you won't see any change.
     * @param displayName The display name to set
     * @param color The {@link Color} to set
     */
    public void setDisplayName(String displayName, Color color) {
        this.itemMeta.customName(color != null ? Component.text(displayName).color(TextColor.color(color.asRGB())) : Component.text(displayName));
        this.itemStack.setItemMeta(this.itemMeta);
        this.displayName = displayName;
        this.color = color;
    }

    /**
     * Updates the lore of this {@link Button}
     * You have to call {@link Page#updateButton(Button)} after calling this method,
     * otherwise you won't see any change.
     * @param lore The lore to be set
     */
    public void setLore(List<String> lore) {

        List<Component> componentList = Lists.transform(lore, Component::text);
        this.itemMeta.lore(componentList);
        this.itemStack.setItemMeta(this.itemMeta);
        this.lore = lore;
    }

    /**
     * Enables or disables the enchantment effect on this {@link Button}
     * You have to call {@link Page#updateButton(Button)} after calling this method,
     * otherwise you won't see any change.
     * @param isEnchanted true for showing the enchantment effect, false otherwise
     */
    public void setEnchanted(boolean isEnchanted) {
        this.itemMeta.setEnchantmentGlintOverride(isEnchanted);
        this.itemStack.setItemMeta(this.itemMeta);
        this.isEnchanted = isEnchanted;
    }

    /**
     * Gets the displayed name
     * @return displayed name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the {@link Button} material
     * @return The {@link Material}
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Gets the displayed name color
     * @return The {@link Color}
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the displayed lore
     * @return The lore
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * Gets the enchanted effect on the {@link Button}
     * @return true if the {@link Button} has an enchanted effect, false otherwise
     */
    public boolean isEnchanted() {
        return isEnchanted;
    }

    /**
     * Gets the {@link Action} assigned to the {@link Button}
     * @return The {@link Action}
     */
    public Action getAction() {
        return action;
    }

    /**
     * Denies the {@link Button} from being clicked through the inventory
     * Most useful for the subclasses (ref {@link UsableItem})
     */
    public void disableInventoryClick() {
        // 'false' means we want to disable clicks from inventory
        this.itemMeta.getPersistentDataContainer().set(CLICKABLE_ON_INVENTORY_KEY, PersistentDataType.BOOLEAN, false);
        this.itemStack.setItemMeta(this.itemMeta);
    }

    /**
     * Allows the {@link Button} to be clicked through the inventory
     */
    public void enableInventoryClick() {
        // 'true' means we want to enable clicks from inventory
        this.itemMeta.getPersistentDataContainer().set(CLICKABLE_ON_INVENTORY_KEY, PersistentDataType.BOOLEAN, true);
        this.itemStack.setItemMeta(this.itemMeta);
    }

    /**
     * Gets the {@link Action} related to an {@link ItemStack}, if one exists
     * @param itemStack The {@link ItemStack} for which an {@link Action} can be related
     * @return An {@link Action} if one is found, otherwise null
     */
    @Nullable
    protected static Action getAction(ItemStack itemStack) {

        Integer buttonID = getButtonID(itemStack);

        if(buttonID == null) return null;

        // Return the Action corresponding to the buttonID if one exists, otherwise return null
        return buttonIdActionMap.get(buttonID);
    }

    /**
     * Get the ID of a {@link Button} contained into an {@link ItemStack} if it contains one
     * @param itemStack The {@link ItemStack} in which an ID will be searched
     * @return An ID, if one is found, otherwise null
     */
    @Nullable
    protected static Integer getButtonID(ItemStack itemStack) {
        // itemStack can be null, we have to be careful
        if (itemStack == null) return null;

        ItemMeta itemMeta = itemStack.getItemMeta();

        // There can be some cases where an itemMeta is null
        if(itemMeta == null) {
            return null;
        }

        /* Return the buttonID that was previously injected into the ItemMeta via the property we created before.
        Will return null if the specified ItemStack is not a Button, because it won't have the BUTTON_ID_KEY.
         */
        return itemMeta.getPersistentDataContainer().get(BUTTON_ID_KEY, PersistentDataType.INTEGER);
    }

    /**
     * Protected class, used for creating the {@link Button} event
     * and automatize the execution of its associated action.
     * When an event is triggered, it checks if the clicked item was indeed the specified {@link Button}
     * and if yes, executes the action specified by the user.
     */
    public static class ButtonListener implements Listener {

        /**
         * Creates a new {@link ButtonListener}.
         */
        public ButtonListener() {}

        /**
         * Listens to every {@link InventoryClickEvent} and checks if a {@link Button} was clicked, then if the right {@link Button was clicked},
         * and automatizes the execution of its associated {@link Action}.
         *
         * @param event The {@link InventoryClickEvent} that was triggered by a {@link org.bukkit.entity.HumanEntity}
         */
        @EventHandler
        public void onButtonClick(InventoryClickEvent event) {

            // If it's not a Player who clicked, we do nothing
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            ItemStack itemStack = event.getCurrentItem();

            Integer buttonID = getButtonID(itemStack);

            // If the ItemStack is not a Button we leave
            if(buttonID == null) {
                return;
            }

            Boolean clickableOnInventory = itemStack.getItemMeta()
                    .getPersistentDataContainer()
                            .get(CLICKABLE_ON_INVENTORY_KEY, PersistentDataType.BOOLEAN);

            // That means this button has disabled clicks from inventory
            if(Boolean.FALSE.equals(clickableOnInventory)) {
                return;
            }

            // We cancel the event and deny any item duplication
            event.setCancelled(true);
            player.updateInventory();

            Action action = getAction(itemStack);

            // getAction() is @Nullable
            if(action != null) {

                switch(event.getClick()) {

                    /* If we left-click or right-click, we execute the action
                    There is a special case where in creative game mode, event.getClick() will
                    always return 'CREATIVE'
                     */
                    case ClickType.LEFT, ClickType.RIGHT, ClickType.CREATIVE, ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> action.execute(player, itemStack);
                }
            }
        }

        /**
         * Denies the {@link Player} from swapping a {@link Button} to his offhand
         * @param event The {@link PlayerSwapHandItemsEvent} which was triggered
         */
        @EventHandler
        public void onButtonSwapHand(PlayerSwapHandItemsEvent event) {
            ItemStack mainHandItem = event.getMainHandItem();

            ItemStack offHandItem = event.getOffHandItem();

            if(getButtonID(mainHandItem) != null || getButtonID(offHandItem) != null) {
                event.setCancelled(true);
            }
        }

        /**
         * Denies the {@link Player} from dropping a {@link Button} from an inventory
         * @param event The {@link PlayerDropItemEvent} which was triggered
         */
        @EventHandler
        public void onButtonDrop(PlayerDropItemEvent event) {

            Player player = event.getPlayer();

            ItemStack itemStack = event.getItemDrop().getItemStack();

            if(getButtonID(itemStack) != null) {
                event.setCancelled(true);
            }
        }

        /**
         * Denies the {@link Player} from having unexpected interactions with a {@link Button}
         * while being in creative mode
         * @param event The {@link InventoryCreativeEvent} which was triggered
         */
        @EventHandler
        public void onCreativeButtonInteract(InventoryCreativeEvent event) {

            if(! (event.getWhoClicked() instanceof Player player)) {
                return;
            }

            ItemStack current = event.getCurrentItem();
            ItemStack cursor  = event.getCursor();

            Integer id = getButtonID(cursor);
            if (id == null) id = getButtonID(current);

            if (id != null) {
                event.setCancelled(true);
                player.updateInventory();
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
            // Clear the <ButtonId, Action> HashMap
            buttonIdActionMap.clear();
        }
    }


    /**
     * Redefined in case {@link java.util.Map} is used later, to prevent comparison problems inside the {@link java.util.Map}
     * @param o Object to be compared with the {@link Button}
     * @return true if the two objects are the same, otherwise false
     */
    @Override
    public boolean equals(Object o) {

        if(! (o instanceof Button button)) {
            return false;
        }
        return this.material == button.material
                && this.isEnchanted == button.isEnchanted
                && (this.lore == null || this.lore.equals(button.lore))
                && this.itemStack.equals(button.itemStack)
                && (this.displayName == null || this.displayName.equals(button.displayName));
    }

    /**
     * Redefined in case {@link java.util.Map} is used later, to prevent comparison problems inside the {@link java.util.Map}
     * @return The corresponding hashCode
     */
    @Override
    public int hashCode() {
        return 31 * Integer.hashCode(this.buttonID);
    }

    /**
     * Get the {@link ItemStack} contained in this {@link Button}
     * @return The associated {@link ItemStack}
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get the {@link ItemMeta} contained in this {@link Button}
     * @return The associated {@link ItemMeta}
     */
    public ItemMeta getItemMeta() {
        return itemMeta;
    }
}
