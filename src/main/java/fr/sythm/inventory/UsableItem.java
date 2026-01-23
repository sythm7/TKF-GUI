package fr.sythm.inventory;

import fr.sythm.inventory.action.Action;
import fr.sythm.inventory.action.NavigationAction;
import net.kyori.adventure.text.TextComponent;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        /** Map of attribute modifiers to be applied to the item */
        private Map<Attribute, AttributeModifier> attributeModifierMap;

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
         * @param isUnbreakable {@code true} if the item is unbreakable, {@code false} otherwise
         * @return The updated {@link Builder}
         */
        public Builder setUnbreakable(boolean isUnbreakable) {
            this.isUnbreakable = isUnbreakable;
            return this;
        }

        /** Adds an attribute modifier to the item
         *
         * @param attribute The attribute to modify
         * @param attributeModifier The modifier to apply
         * @return The updated {@link Builder}
         */
        public Builder addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
            if(this.attributeModifierMap == null) {
                this.attributeModifierMap = new HashMap<>();
            }
            this.attributeModifierMap.put(attribute, attributeModifier);
            return this;
        }

        /**
         * Builds the final {@link UsableItem} instance from the configuration defined by the user in this {@link Builder}.
         * @return The {@link UsableItem} instance built with its proper configuration
         */
        @Override
        public UsableItem build() {
            return new UsableItem(this.material, this.displayName, this.lore, this.isEnchanted, this.action,
                    this.clickableOnInventory, this.showItemAttributes, this.itemArmor, this.itemAttackDamage, this.itemAttackSpeed, this.isUnbreakable, this.attributeModifierMap);
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
     * @param lore                 The description that will be shown for an {@link ItemStack}
     * @param isEnchanted          Specifies if the {@link ItemStack} will have an enchantment effect displayed on it
     * @param action               Specifies the action to be triggered on a {@link UsableItem} click
     * @param clickableOnInventory Specifies if the {@link UsableItem} can be clicked through the inventory
     * @param showItemAttributes   Specifies if the item attributes (attack damage, armor, enchantments, etc.) will be shown in the item lore
     * @param itemArmor            The armor points that will be added to the item
     * @param itemAttackDamage     The attack damage that will be added to the item
     * @param itemAttackSpeed      The attack speed that will be added to the item
     * @param isUnbreakable        Specifies if the item is unbreakable
     * @param attributeModifierMap Map of attribute modifiers to be applied to the item
     */
    private UsableItem(Material material, TextComponent displayName, List<TextComponent> lore, boolean isEnchanted, Action action,
                       boolean clickableOnInventory, boolean showItemAttributes, Double itemArmor, Double itemAttackDamage, Double itemAttackSpeed, boolean isUnbreakable, Map<Attribute, AttributeModifier> attributeModifierMap) {

        super(material, displayName, lore, isEnchanted, action, showItemAttributes);

        if(! initialized) {
            // Register the Listener common to every UsableItem. This operation is called only once.
            plugin.getServer().getPluginManager().registerEvents(new UsableItemListener(), plugin);
            initialized = true;
        }

        if(! clickableOnInventory) {
            // Disable the Action being executed in the InventoryClickEvent for this UsableItem
            this.disableInventoryClick();
        }
        /* Set the custom item attributes if any are specified */
        if(attributeModifierMap != null) {
            for(Map.Entry<Attribute, AttributeModifier> entry : attributeModifierMap.entrySet()) {
                this.itemMeta.addAttributeModifier(entry.getKey(), entry.getValue());
            }
        }
        /* Add the attack damage attribute if specified */
        if(itemAttackDamage != null) {
            this.itemMeta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.attack_damage")), itemAttackDamage, AttributeModifier.Operation.ADD_NUMBER));
        }
        /* Add the armor attribute if specified */
        if(itemArmor != null) {
            this.itemMeta.addAttributeModifier(Attribute.ARMOR,
                    new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.armor")), itemArmor, AttributeModifier.Operation.ADD_NUMBER));
        }
        /* Add the attack speed attribute if specified */
        if(itemAttackSpeed != null) {
            this.itemMeta.addAttributeModifier(Attribute.ATTACK_SPEED,
                    new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("generic.armor")), itemAttackSpeed, AttributeModifier.Operation.ADD_NUMBER));
        }
        /* Make the item unbreakable if specified */
        if(isUnbreakable) {
            this.itemMeta.setUnbreakable(true);
        }

        /* Update the ItemStack's ItemMeta */
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

    /** Set the {@link UsableItem} in the specified equipment slot of the selected {@link Player}
     *
     * @param player The selected {@link Player}
     * @param equipmentSlot The equipment slot which the item will be put in
     */
    public void setEquipmentSlot(Player player, EquipmentSlot equipmentSlot) {
        player.getInventory().setItem(equipmentSlot, this.itemStack);
    }

    /** Set the {@link UsableItem} in the hand of the selected {@link Player}
     *
     * @param player The selected {@link Player}
     */
    public void setHand(Player player) {
        player.getInventory().setItem(EquipmentSlot.HAND, this.itemStack);
    }

    /** Set the {@link UsableItem} in the off-hand of the selected {@link Player}
     *
     * @param player The selected {@link Player}
     */
    public void setOffHand(Player player) {
        player.getInventory().setItem(EquipmentSlot.OFF_HAND, this.itemStack);
    }

    /**
     * private class, used for creating the {@link UsableItem} event
     * and automatize the execution of its associated action.
     * When an event is triggered, it checks if the clicked item was indeed the specified {@link UsableItem}
     * and if yes, executes the action specified by the user.
     */
    private static class UsableItemListener implements Listener {

        /**
         * Handles the events related to item utilization (left-click and right-click) by a {@link Player}
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
