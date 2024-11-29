package kevlar.enchantConfigurator.config;

import kevlar.enchantConfigurator.EnchantConfigurator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final EnchantConfigurator plugin;
    private FileConfiguration customEnchants;
    private File customEnchantsFile;
    private Map<Enchantment, VanillaEnchantSettings> vanillaEnchantSettings;

    public ConfigManager(EnchantConfigurator plugin) {
        this.plugin = plugin;
        this.vanillaEnchantSettings = new HashMap<>();
    }

    public void loadConfigs() {
        // Create default config if it doesn't exist
        plugin.saveDefaultConfig();
        
        // Setup custom enchants config
        customEnchantsFile = new File(plugin.getDataFolder(), "customenchants.yml");
        if (!customEnchantsFile.exists()) {
            customEnchantsFile.getParentFile().mkdirs();
            plugin.saveResource("customenchants.yml", false);
        }
        
        customEnchants = YamlConfiguration.loadConfiguration(customEnchantsFile);
        
        // Load vanilla enchant settings
        loadVanillaEnchantSettings();
    }

    private void loadVanillaEnchantSettings() {
        vanillaEnchantSettings.clear();
        FileConfiguration config = plugin.getConfig();

        if (config.contains("vanilla_enchants")) {
            for (String enchantName : config.getConfigurationSection("vanilla_enchants").getKeys(false)) {
                try {
                    Enchantment enchant = Enchantment.getByName(enchantName);
                    if (enchant != null) {
                        String path = "vanilla_enchants." + enchantName;
                        boolean enabled = config.getBoolean(path + ".enabled", true);
                        int maxLevel = config.getInt(path + ".max_level", enchant.getMaxLevel());
                        int minExpLevel = config.getInt(path + ".min_exp_level", 1);

                        vanillaEnchantSettings.put(enchant, new VanillaEnchantSettings(enabled, maxLevel, minExpLevel));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid enchantment name in config: " + enchantName);
                }
            }
        }
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        customEnchants = YamlConfiguration.loadConfiguration(customEnchantsFile);
        loadVanillaEnchantSettings();
    }

    public FileConfiguration getCustomEnchants() {
        return customEnchants;
    }

    public void saveCustomEnchants() {
        try {
            customEnchants.save(customEnchantsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save custom enchants config!");
            e.printStackTrace();
        }
    }

    public VanillaEnchantSettings getVanillaEnchantSettings(Enchantment enchant) {
        return vanillaEnchantSettings.getOrDefault(enchant, 
            new VanillaEnchantSettings(true, enchant.getMaxLevel(), 1));
    }

    public boolean isEnchantRemovalAllowed() {
        return plugin.getConfig().getBoolean("allow_enchant_removal", true);
    }

    public boolean isAllowCombiningAboveMax() {
        return plugin.getConfig().getBoolean("allow_combining_above_max", true);
    }
} 