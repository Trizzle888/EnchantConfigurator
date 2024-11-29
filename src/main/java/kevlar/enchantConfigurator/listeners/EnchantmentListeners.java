package kevlar.enchantConfigurator.listeners;

import kevlar.enchantConfigurator.EnchantConfigurator;
import kevlar.enchantConfigurator.config.VanillaEnchantSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class EnchantmentListeners implements Listener {
    private final EnchantConfigurator plugin;

    public EnchantmentListeners(EnchantConfigurator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        
        if (first == null || second == null) return;

        // Handle custom enchant books
        ItemMeta secondMeta = second.getItemMeta();
        if (secondMeta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "custom_enchant");
        String enchantData = secondMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        
        if (enchantData != null) {
            String[] parts = enchantData.split(":");
            String enchantName = parts[0];
            int level = Integer.parseInt(parts[1]);

            ItemStack result = first.clone();
            if (plugin.getEnchantManager().applyCustomEnchant(result, enchantName, level)) {
                event.setResult(result);
                // Set a reasonable repair cost
                AnvilInventory anvilInv = event.getInventory();
                anvilInv.setRepairCost(level * 2); // Cost scales with enchantment level
            }
            return; // Return here to prevent vanilla enchant handling
        }

        // Handle vanilla enchant modifications
        if (plugin.getConfigManager().isAllowCombiningAboveMax()) {
            handleVanillaEnchantCombining(event);
        }
    }

    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        // Instead of modifying offers directly, we'll modify the exp level requirements
        for (Enchantment enchant : Enchantment.values()) {
            VanillaEnchantSettings settings = plugin.getConfigManager().getVanillaEnchantSettings(enchant);
            if (!settings.isEnabled()) {
                // If enchant is disabled, set required level very high to prevent it
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        // Modify enchantment levels based on config
        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        
        for (Map.Entry<Enchantment, Integer> entry : enchantsToAdd.entrySet()) {
            VanillaEnchantSettings settings = plugin.getConfigManager().getVanillaEnchantSettings(entry.getKey());
            if (!settings.isEnabled()) {
                event.setCancelled(true);
                return;
            }
            if (entry.getValue() > settings.getMaxLevel()) {
                entry.setValue(settings.getMaxLevel());
            }
        }
    }

    private void handleVanillaEnchantCombining(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        if (first == null || second == null) return;

        ItemStack result = event.getResult();
        if (result == null) {
            result = first.clone();
        }

        boolean modified = false;
        for (Enchantment enchant : Enchantment.values()) {
            VanillaEnchantSettings settings = plugin.getConfigManager().getVanillaEnchantSettings(enchant);
            
            int firstLevel = first.getEnchantmentLevel(enchant);
            int secondLevel = second.getEnchantmentLevel(enchant);
            
            if (firstLevel > 0 || secondLevel > 0) {
                if (firstLevel == secondLevel && firstLevel > 0) {
                    int newLevel = Math.min(firstLevel + 1, settings.getMaxLevel());
                    if (newLevel > firstLevel) {
                        result.addUnsafeEnchantment(enchant, newLevel);
                        modified = true;
                    }
                } else {
                    int maxLevel = Math.max(firstLevel, secondLevel);
                    if (maxLevel > 0) {
                        result.addUnsafeEnchantment(enchant, Math.min(maxLevel, settings.getMaxLevel()));
                        modified = true;
                    }
                }
            }
        }

        if (modified) {
            event.setResult(result);
            // Set repair cost based on number of enchantments
            AnvilInventory anvilInv = event.getInventory();
            anvilInv.setRepairCost(result.getEnchantments().size() * 2);
        }
    }
} 