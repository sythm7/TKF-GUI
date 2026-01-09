package fr.sythm.gui;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Objects;

/**
 * This class makes a {@link Button} being a usable Minecraft item, which means the item can be given into a player's inventory
 * and can be used from the player's hand.
 */
public class UsableItem extends Button {

    /**
     * Adapted for building a {@link UsableItem}
     */
    public static final class Builder extends GenericBuilder<Builder> {

        /** Specifies if the UsableItem can be clicked through an inventory */
        private boolean clickableOnInventory;

        /** The damage that will be applied to the item */
        private Double itemAttackDamage;

        /** The armor points that will be applied to the item */
        private Double itemArmor;

        /** The attack speed that will be applied to the item */
        private Double itemAttackSpeed;

        /** Specifies if the item is unbreakable */
        private boolean isUnbreakable = true;

        /**
         * Initializes a {@link Builder} with the {@link Material} as the only mandatory feature that a {@link UsableItem} will use.
         * Other features are optional, and depends on what usage the user wants to create for his {@link UsableItem}.
         *
         * @param material The in game item ID
         */
        public Builder(Material material) {
            super(material);
            this.showItemAttributes = true;
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

        /** Adds attack damage to the item
         *
         * @param attackDamage The amount of attack damage to add
         * @return The updated {@link Builder}
         */
        public Builder addAttackDamage(Double attackDamage) {
            this.itemAttackDamage = attackDamage;
            return this;
        }

        /** Adds attack speed to the item
         *
         * @param attackSpeed The amount of attack speed to add
         * @return The updated {@link Builder}
         */
        public Builder addAttackSpeed(Double attackSpeed) {
            this.itemAttackSpeed = attackSpeed;
            return this;
        }

        /** Adds armor points to the item
         *
         * @param armor The amount of armor points to add
         * @return The updated {@link Builder}
         */
        public Builder addArmor(Double armor) {
            this.itemArmor = armor;
            return this;
        }

        /** Specifies if the item is unbreakable
         *
         * @param isUnbreakable true if the item is unbreakable, false otherwise
         * @return The updated {@link Builder}
         */
        public Builder isUnbreakable(boolean isUnbreakable) {
            this.isUnbreakable = isUnbreakable;
            return this;
        }

        /**
         * Builds the final {@link UsableItem} instance from the configuration defined by the user in this {@link Builder}.
         * @return The {@link UsableItem} instance built with its proper configuration
         */
        @Override
        public UsableItem build() {
            return new UsableItem(this.material, this.displayName, this.color, this.loreColor, this.lore, this.isEnchanted, this.action,
                    this.clickableOnInventory, this.showItemAttributes, this.itemArmor, this.itemAttackDamage, this.itemAttackSpeed, this.isUnbreakable);
        }
    }

    /** Used to register the Listener only once */
    private static boolean initialized = false;

    /** The damage that will be applied to the item */
    private Double itemAttackDamage;

    /** The armor points that will be applied to the item */
    private Double itemArmor;

    /** The attack speed that will be applied to the item */
    private Double itemAttackSpeed;

    /** Specifies if the item is unbreakable */
    private boolean isUnbreakable;

    /**
     * Private {@link UsableItem} constructor as it would be nonsense to allow the user to use this publicly, because we don't need to initialize
     * every attribute of this class. Instead, the user has to use the {@link Builder} class because it allows him to decide whenever he
     * wants to use an option for his {@link UsableItem}.
     *
     * @param material             The in game item ID
     * @param displayName          Custom name that will be displayed for an {@link ItemStack}
     * @param color                The color that will be applied on the displayed name
     * @param loreColor            The color that will be applied on the lore
     * @param lore                 The description that will be shown for an {@link ItemStack}
     * @param isEnchanted          Specifies if the {@link ItemStack} will have an enchantment effect displayed on it
     * @param action               Specifies the action to be triggered on a {@link UsableItem} click
     * @param clickableOnInventory Specifies if the {@link UsableItem} can be clicked through the inventory
     * @param showItemAttributes   Specifies if the item attributes (attack damage, armor, enchantments, etc.) will be shown in the item lore
     * @param itemArmor           The armor points that will be added to the item
     * @param itemAttackDamage    The attack damage that will be added to the item
     * @param itemAttackSpeed     The attack speed that will be added to the item
     * @param isUnbreakable       Specifies if the item is unbreakable
     */
    private UsableItem(Material material, String displayName, Color color, Color loreColor, List<String> lore, boolean isEnchanted, Action action,
                       boolean clickableOnInventory, boolean showItemAttributes, Double itemArmor, Double itemAttackDamage, Double itemAttackSpeed, boolean isUnbreakable) {

        super(material, displayName, color, loreColor, lore, isEnchanted, action, showItemAttributes);

        if(! initialized) {
            // Register the Listener common to every UsableItem. This operation is called only once.
            plugin.getServer().getPluginManager().registerEvents(new UsableItemListener(), plugin);
            initialized = true;
        }

        if(! clickableOnInventory) {
            // Disable the Action being executed in the InventoryClickEvent for this UsableItem
            this.disableInventoryClick();
        }

        if(itemAttackSpeed != null) {
            this.itemMeta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.attack_damage")), itemAttackDamage, AttributeModifier.Operation.ADD_NUMBER));
        }
        if(itemArmor != null) {
            this.itemMeta.addAttributeModifier(Attribute.ARMOR,
                    new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.armor")), itemArmor, AttributeModifier.Operation.ADD_NUMBER));
        }
        if(itemAttackSpeed != null) {
            this.itemMeta.addAttributeModifier(Attribute.ATTACK_SPEED,
                    new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.armor")), itemAttackSpeed, AttributeModifier.Operation.ADD_NUMBER));
        }
        if(isUnbreakable) {
            this.itemMeta.setUnbreakable(true);
        }

        this.itemStack.setItemMeta(this.itemMeta);
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
                action.execute(player, event);
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
