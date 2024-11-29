package kevlar.enchantConfigurator.enchants;

import kevlar.enchantConfigurator.EnchantConfigurator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EnchantmentEffectHandler implements Listener {
    private final EnchantConfigurator plugin;

    public EnchantmentEffectHandler(EnchantConfigurator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        // Check for custom enchants
        for (CustomEnchant enchant : plugin.getEnchantManager().getCustomEnchants().values()) {
            NamespacedKey key = new NamespacedKey(plugin, "custom_enchant_" + enchant.getId());
            Integer level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            
            if (level != null && enchant.getEffects().contains("STRIKE_LIGHTNING")) {
                handleLightningAspect(event, level, enchant);
            }
            // Add more effect checks here
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        // Check for custom enchants
        for (CustomEnchant enchant : plugin.getEnchantManager().getCustomEnchants().values()) {
            NamespacedKey key = new NamespacedKey(plugin, "custom_enchant_" + enchant.getId());
            Integer level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            
            if (level != null && enchant.getEffects().contains("SMELT_ORE")) {
                handleAutoSmelt(event, level, enchant);
            }
            // Add more effect checks here
        }
    }

    private void handleLightningAspect(EntityDamageByEntityEvent event, int level, CustomEnchant enchant) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        double chance = 0.15 * (level * enchant.getLevelMultiplier());
        if (Math.random() <= chance) {
            Location loc = event.getEntity().getLocation();
            event.getEntity().getWorld().strikeLightning(loc);
            // Add extra damage based on level
            event.setDamage(event.getDamage() + (level * 2));
        }
    }

    private void handleAutoSmelt(BlockBreakEvent event, int level, CustomEnchant enchant) {
        Block block = event.getBlock();
        Material type = block.getType();
        ItemStack drop = null;

        // Define smeltable ores and their results
        switch (type) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                drop = new ItemStack(Material.IRON_INGOT);
                break;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                drop = new ItemStack(Material.GOLD_INGOT);
                break;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                drop = new ItemStack(Material.COPPER_INGOT);
                break;
            case ANCIENT_DEBRIS:
                drop = new ItemStack(Material.NETHERITE_SCRAP);
                break;
            // Add more ore types as needed
        }

        if (drop != null) {
            event.setDropItems(false);
            Location loc = block.getLocation();
            loc.getWorld().dropItemNaturally(loc, drop);
        }
    }
} 