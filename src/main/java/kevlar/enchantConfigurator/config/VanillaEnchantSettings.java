package kevlar.enchantConfigurator.config;

public class VanillaEnchantSettings {
    private final boolean enabled;
    private final int maxLevel;
    private final int minExpLevel;

    public VanillaEnchantSettings(boolean enabled, int maxLevel, int minExpLevel) {
        this.enabled = enabled;
        this.maxLevel = maxLevel;
        this.minExpLevel = minExpLevel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMinExpLevel() {
        return minExpLevel;
    }
} 