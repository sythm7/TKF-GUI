package fr.sythm.gui;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

/**
 * This class transforms a simple {@link ItemStack} in a clickable {@link Button} with its own customized actions.
 */
public class Button {

    // Makes a unique ID everytime a Button is created.
    private static int buttonIndexing = 0;
    // Used for creating a custom attribute where we can put the Button ID in.
    private static NamespacedKey KEY;

    /**
     * This class is used to create a {@link Button} in the simplest way, using a Builder Pattern.
     */
    public static class Builder {

        // In game item name/ID
        private final Material material;
        // Custom item name that will be displayed
        private String displayName;
        // Display name color
        private Color color;
        // Item description
        private List<String> lore;
        // Enchantment glint effect ON or OFF
        private boolean isEnchanted;
        // Custom action on a Button click
        private Action action;

        /**
         * Initializes a {@link Builder} with the {@link Material} as the only mandatory feature that a {@link Button} will use.
         * Other features are optional, and depends on what usage the user wants to create for his {@link Button}.
         * @param material The in game item ID
         */
        public Builder(Material material) {
            this.material = material;
            this.isEnchanted = false;
        }

        /**
         * Adds a display name to the {@link Builder} configuration.
         * @param displayName Custom name that will be displayed for an {@link ItemStack}
         * @return The updated {@link Builder}
         */
        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Adds a colored display name to the {@link Builder} configuration.
         * @param displayName Custom name that will be displayed for an {@link ItemStack}
         * @param color The colored that will be applied on the displayed name
         * @return The updated {@link Builder}
         */
        public Builder withDisplayName(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
            return this;
        }

        /**
         * Adds a description to the {@link Builder} configuration.
         * @param lore The description that will be shown for an {@link ItemStack}
         * @return The updated {@link Builder}
         */
        public Builder withLore(List<String> lore) {
            this.lore = lore;
            return this;
        }

        /**
         * Specifies the usage of the enchanted effect into the {@link Builder} configuration.
         * @param isEnchanted Specifies if the {@link ItemStack} will have an enchantment effect displayed on it.
         * @return The updated {@link Builder}
         */
        public Builder withEnchantedEffect(boolean isEnchanted) {
            this.isEnchanted = isEnchanted;
            return this;
        }

        /**
         * Adds an {@link Action} specified by the user. It can be either a {@link NavigationAction} or any other custom {@link Action} implemented by the user.
         * @param action Specifies the action to be triggered on a {@link Button} click
         * @return The updated {@link Builder}
         */
        public Builder withAction(Action action) {
            this.action = action;
            return this;
        }

        /**
         * Builds the final {@link Button} instance from the configuration defined by the user in this {@link Builder}.
         * @return The {@link Button} instance built with its proper configuration
         */
        public Button build() {
            return new Button(this.material, this.displayName, this.color, this.lore, this.isEnchanted, this.action);
        }
    }

    // In game item name/ID
    private final Material material;
    // Custom item name that will be displayed
    private final String displayName;
    // Display name color
    private Color color;
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
     * Private {@link Button} constructor as it would be nonsense to allow the user to use this publicly, because we don't need to initialize
     * every attribute of this class. Instead, the user has to use the {@link Builder} class because it allows him to decide whenever he
     * wants to use an option for his {@link Button}.
     * @param material The in game item ID
     * @param displayName Custom name that will be displayed for an {@link ItemStack}
     * @param color The color that will be applied on the displayed name
     * @param lore The description that will be shown for an {@link ItemStack}
     * @param isEnchanted Specifies if the {@link ItemStack} will have an enchantment effect displayed on it
     * @param action Specifies the action to be triggered on a {@link Button} click
     */
    private Button(Material material, String displayName, Color color, List<String> lore, boolean isEnchanted, Action action) {

        this.material = material;
        this.itemStack = new ItemStack(material);
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

        // We have to be careful, as the getItemMeta() method is @Nullable
        if(this.itemMeta == null) {
            plugin.getLogger().warning("Material " + material + " has null ItemMeta.");
            return;
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
     */
    protected void registerEvent() {
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
            plugin.getServer().getPluginManager().registerEvents(new ButtonListener(this), plugin);
        }
    }

    /**
     * Private record (a record is a simple class), used for creating the {@link Button} event
     * and automatize the execution of its associated action.
     * When an event is triggered, it checks if the clicked item was indeed the specified {@link Button}
     * and if yes, executes the action specified by the user.
     * @param button The button linked to this {@link Listener}
     */
    private record ButtonListener(Button button) implements Listener {
            /**
             * Listens to every {@link InventoryClickEvent} and checks if a {@link Button} was clicked, then if the right {@link Button was clicked},
             * and automatizes the execution of its associated {@link Action}.
             *
             * @param event The {@link InventoryClickEvent} that was triggered by a {@link org.bukkit.entity.HumanEntity}
             */
            @EventHandler
            private void onButtonClick(InventoryClickEvent event) {

                // If it's not a Player who clicked, we do nothing
                if (!(event.getWhoClicked() instanceof Player player)) {
                    return;
                }

                ItemStack itemStack = event.getCurrentItem();

                // getCurrentItem() is @Nullable, we have to be careful
                if (itemStack == null) {
                    return;
                }

                ItemMeta itemMeta = itemStack.getItemMeta();

                // getItemMeta() is @Nullable, we have to be careful
                if (itemMeta == null) {
                    return;
                }

                // Retrieve the buttonID that was previously injected into the ItemMeta via the property we created before
                Integer buttonID = itemMeta.getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);

                /*
                If it's null, that means it doesn't contain a buttonID, so it doesn't correspond to a Button built in this class
                Otherwise, check, if the retrieved buttonID/object and the instance's buttonID/object are the same (that means we got the right Button clicked ! :D)
                 */
                if (buttonID != null && buttonID == this.button.buttonID && itemStack.equals(this.button.itemStack)) {
                    this.button.action.execute(player, itemStack);
                }
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
