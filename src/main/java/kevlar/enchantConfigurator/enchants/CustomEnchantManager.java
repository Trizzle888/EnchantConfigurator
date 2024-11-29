package kevlar.enchantConfigurator.enchants;

import kevlar.enchantConfigurator.EnchantConfigurator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEnchantManager {
    private final EnchantConfigurator plugin;
    private final Map<String, CustomEnchant> customEnchants;

    public CustomEnchantManager(EnchantConfigurator plugin) {
        this.plugin = plugin;
        this.customEnchants = new HashMap<>();
    }

    public void loadCustomEnchants() {
        customEnchants.clear();
        
        ConfigurationSection config = plugin.getConfigManager().getCustomEnchants();
        if (config == null) {
            plugin.getLogger().warning("Custom enchants configuration is empty or invalid!");
            return;
        }

        for (String key : config.getKeys(false)) {
            try {
                String displayName = config.getString(key + ".displayName", key);
                int maxLevel = config.getInt(key + ".maxLevel", 1);
                List<String> allowedItems = config.getStringList(key + ".allowedItems");
                List<String> effects = config.getStringList(key + ".effects");
                double levelMultiplier = config.getDouble(key + ".levelMultiplier", 1.0);
                
                CustomEnchant enchant = new CustomEnchant(key, displayName, maxLevel, allowedItems, effects, levelMultiplier);
                customEnchants.put(key, enchant);
                plugin.getLogger().info("Loaded custom enchantment: " + key);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load custom enchantment: " + key);
                e.printStackTrace();
            }
        }
    }

    public ItemStack createEnchantmentBook(String enchantName, int level) {
        CustomEnchant enchant = customEnchants.get(enchantName);
        if (enchant == null) return null;

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return null;

        meta.setDisplayName(ChatColor.AQUA + enchant.getDisplayName() + " " + level);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + enchant.getDisplayName() + " " + level);
        meta.setLore(lore);

        // Store the custom enchant data
        NamespacedKey key = new NamespacedKey(plugin, "custom_enchant");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, enchantName + ":" + level);

        book.setItemMeta(meta);
        return book;
    }

    public boolean applyCustomEnchant(ItemStack item, String enchantName, int level) {
        CustomEnchant enchant = customEnchants.get(enchantName);
        if (enchant == null) return false;

        if (!enchant.canEnchantItem(item)) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        // Remove existing enchant if present
        lore.removeIf(line -> line.contains(enchant.getDisplayName()));
        
        // Add new enchant
        lore.add(ChatColor.GRAY + enchant.getDisplayName() + " " + level);
        meta.setLore(lore);

        // Store the custom enchant data
        NamespacedKey key = new NamespacedKey(plugin, "custom_enchant_" + enchantName);
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        item.setItemMeta(meta);
        return true;
    }

    public Map<String, CustomEnchant> getCustomEnchants() {
        return customEnchants;
    }
} 