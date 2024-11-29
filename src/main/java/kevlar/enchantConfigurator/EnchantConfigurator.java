package kevlar.enchantConfigurator;

import kevlar.enchantConfigurator.commands.EnchantConfiguratorCommand;
import kevlar.enchantConfigurator.config.ConfigManager;
import kevlar.enchantConfigurator.enchants.CustomEnchantManager;
import kevlar.enchantConfigurator.listeners.EnchantmentListeners;
import kevlar.enchantConfigurator.enchants.EnchantmentEffectHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantConfigurator extends JavaPlugin {
    private static EnchantConfigurator instance;
    private ConfigManager configManager;
    private CustomEnchantManager enchantManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default configs
        saveDefaultConfig();
        saveResource("customenchants.yml", false);
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs(); // Load configs before creating CustomEnchantManager
        
        this.enchantManager = new CustomEnchantManager(this);
        this.enchantManager.loadCustomEnchants();
        
        // Register commands
        getCommand("enchantconfigurator").setExecutor(new EnchantConfiguratorCommand(this));
        getCommand("enchantconfigurator").setTabCompleter(new EnchantConfiguratorCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new EnchantmentListeners(this), this);
        getServer().getPluginManager().registerEvents(new EnchantmentEffectHandler(this), this);
        
        getLogger().info("EnchantConfigurator has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnchantConfigurator has been disabled!");
    }

    public static EnchantConfigurator getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CustomEnchantManager getEnchantManager() {
        return enchantManager;
    }
}
