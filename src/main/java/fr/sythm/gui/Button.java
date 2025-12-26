package fr.sythm.gui;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

/**
 * This class transforms a simple {@link ItemStack} in a clickable {@link Button} with its own customized actions.
 */
public class Button {

    // Makes a unique ID everytime a Button is created.
    private static int buttonIndexing = 0;
    // Used for creating a custom attribute where we can put the Button ID in.
    private static NamespacedKey KEY;

    /* We initialize a Map and instead of registering events for each button created, we only register 1 event,
    and when the event is triggered we retrieve the Action corresponding to the id of the clicked Button
     */
    private static Map<Integer, Action> buttonIdActionMap;

    /**
     * This generic class is used to create a {@link Button} in the simplest way, using a Builder Pattern.
     * The purpose of this generic class is to be extended for a specific use case (see {@link Button.Builder} & {@link UsableItem.Builder})
     */
    protected static class GenericBuilder<T extends  GenericBuilder<T>> {

        // In game item name/ID
        protected final Material material;
        // Custom item name that will be displayed
        protected String displayName;
        // Display name color
        protected Color color;
        // Item description
        protected List<String> lore;
        // Enchantment glint effect ON or OFF
        protected boolean isEnchanted;
        // Custom action on a Button click
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

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        /**
         * Adds a display name to the {@link GenericBuilder} configuration.
         * @param displayName Custom name that will be displayed for an {@link ItemStack}
         * @return The updated {@link GenericBuilder}
         */
        public T withDisplayName(String displayName) {
            this.displayName = displayName;
            return this.self();
        }

        /**
         * Adds a colored display name to the {@link GenericBuilder} configuration.
         * @param displayName Custom name that will be displayed for an {@link ItemStack}
         * @param color The colored that will be applied on the displayed name
         * @return The updated {@link GenericBuilder}
         */
        public T withDisplayName(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
            return this.self();
        }

        /**
         * Adds a description to the {@link GenericBuilder} configuration.
         * @param lore The description that will be shown for an {@link ItemStack}
         * @return The updated {@link GenericBuilder}
         */
        public T withLore(List<String> lore) {
            this.lore = lore;
            return this.self();
        }

        /**
         * Specifies the usage of the enchanted effect into the {@link GenericBuilder} configuration.
         * @param isEnchanted Specifies if the {@link ItemStack} will have an enchantment effect displayed on it.
         * @return The updated {@link GenericBuilder}
         */
        public T withEnchantedEffect(boolean isEnchanted) {
            this.isEnchanted = isEnchanted;
            return this.self();
        }

        /**
         * Adds an {@link Action} specified by the user. It can be either a {@link NavigationAction} or any other custom {@link Action} implemented by the user.
         * @param action Specifies the action to be triggered on a {@link Button} click
         * @return The updated {@link GenericBuilder}
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
    private final Material material;
    // Custom item name that will be displayed
    private final String displayName;
    // Display name color
    private final Color color;
    // Item description
    private final List<String> lore;
    // Enchantment glint effect ON or OFF
    private final boolean isEnchanted;
    // Custom action on a Button click
    private final Action action;
    // Unique ID assigned to the Button
    private final int buttonID;

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    private static Plugin plugin;

    /**
     * Protected {@link Button} constructor as it would be nonsense to allow the user to use this publicly, because we don't need to initialize
     * every attribute of this class. Instead, the user has to use the {@link GenericBuilder} class because it allows him to decide whenever he
     * wants to use an option for his {@link Button}.
     * @param material The in game item ID
     * @param displayName Custom name that will be displayed for an {@link ItemStack}
     * @param color The color that will be applied on the displayed name
     * @param lore The description that will be shown for an {@link ItemStack}
     * @param isEnchanted Specifies if the {@link ItemStack} will have an enchantment effect displayed on it
     * @param action Specifies the action to be triggered on a {@link Button} click
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

        if(plugin == null) {
            plugin = JavaPlugin.getProvidingPlugin(Button.class);
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
     * Registers the {@link Button} event and its corresponding action, if one was specified.
     * @param listener The {@link Listener} that will be registered
     */
    protected void registerEvent(ButtonListener listener) {
        /*
         * Retrieve the plugin instance by finding from which plugin this class is executed, then register the Button event.
         * It's a mandatory operation as it's impossible to register an event without access to the corresponding plugin.
         */
        if(this.action != null) {
            // Create a new property that will contain our unique buttonID.
            if(KEY == null) {
                KEY = new NamespacedKey(plugin, "button_id");
            }

            // Inject the newly created property into the ItemMeta
            this.itemMeta.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, this.buttonID);
            // Update the ItemMeta contained in this ItemStack
            this.itemStack.setItemMeta(this.itemMeta);
            // Finally, register our event and its associated Action into spigot
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    /**
     * Protected class, used for creating the {@link Button} event
     * and automatize the execution of its associated action.
     * When an event is triggered, it checks if the clicked item was indeed the specified {@link Button}
     * and if yes, executes the action specified by the user.
     */
    protected static class ButtonListener implements Listener {

        private final Button button;

        /**
         * Initializes a {@link ButtonListener}
         * @param button The button linked to this {@link Listener}
         */
        public ButtonListener(Button button) {
            this.button = button;
        }

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

            if(this.isButtonClicked(itemStack)) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    this.executeAction(player, itemStack);
                }, 5);
            }
        }

        /**
         * Checks if the clicked {@link ItemStack} is a {@link Button}
         * @param itemStack The {@link ItemStack} that was clicked
         * @return {@literal true} true if the right {@link ItemStack} was clicked, otherwise false
         */
        protected boolean isButtonClicked(ItemStack itemStack) {
            // itemStack can be null, we have to be careful
            if (itemStack == null) return false;

            ItemMeta itemMeta = itemStack.getItemMeta();

            // Retrieve the buttonID that was previously injected into the ItemMeta via the property we created before
            Integer buttonID = itemMeta.getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);

            /*
            If it's null, that means it doesn't contain a buttonID, so it doesn't correspond to a Button
            Otherwise, check, if the retrieved buttonID/object and the instance's buttonID/object are the same (that means we got the right Button clicked ! :D)
             */
            if (buttonID != null && buttonID == this.button.buttonID && itemStack.equals(this.button.itemStack)) {
                return true;
            }
            return false;
        }

        /**
         * Makes the {@link Player} executes the {@link Action} related to the clicked {@link ItemStack}
         * @param player The {@link Player} related to the {@link ItemStack}
         * @param itemStack The {@link ItemStack} that was clicked
         */
        protected void executeAction(Player player, ItemStack itemStack) {
            this.button.action.execute(player, itemStack);
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
        return 31 * (this.material.hashCode()
                + (this.lore != null ? this.lore.hashCode() : 0)
                + (this.displayName != null ? this.displayName.hashCode() : 0)
                + this.itemStack.hashCode()
                + Boolean.hashCode(this.isEnchanted));
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
