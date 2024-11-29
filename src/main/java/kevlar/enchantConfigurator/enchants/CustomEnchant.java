package kevlar.enchantConfigurator.enchants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomEnchant {
    private final String id;
    private final String displayName;
    private final int maxLevel;
    private final List<String> allowedItems;
    private final List<String> effects;
    private final double levelMultiplier;

    public CustomEnchant(String id, String displayName, int maxLevel, List<String> allowedItems, 
                        List<String> effects, double levelMultiplier) {
        this.id = id;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.allowedItems = allowedItems;
        this.effects = effects;
        this.levelMultiplier = levelMultiplier;
    }

    public boolean canEnchantItem(ItemStack item) {
        return allowedItems.contains(item.getType().name());
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public List<String> getAllowedItems() {
        return allowedItems;
    }

    public List<String> getEffects() {
        return effects;
    }

    public double getLevelMultiplier() {
        return levelMultiplier;
    }
} 