package kevlar.enchantConfigurator.commands;

import kevlar.enchantConfigurator.EnchantConfigurator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantConfiguratorCommand implements CommandExecutor, TabCompleter {
    private final EnchantConfigurator plugin;

    public EnchantConfiguratorCommand(EnchantConfigurator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /enchantconfigurator <customenchant|reload|give>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("enchantconfigurator.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                plugin.getConfigManager().reloadConfigs();
                plugin.getEnchantManager().loadCustomEnchants();
                sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                break;

            case "customenchant":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                if (!sender.hasPermission("enchantconfigurator.enchant")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /enchantconfigurator customenchant <enchant>");
                    return true;
                }
                handleCustomEnchant((Player) sender, args[1]);
                break;

            case "give":
                if (!sender.hasPermission("enchantconfigurator.give")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /enchantconfigurator give <player> <enchant>");
                    return true;
                }
                handleGiveBook(sender, args[1], args[2]);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand!");
                break;
        }

        return true;
    }

    private void handleCustomEnchant(Player player, String enchantName) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must hold an item to enchant!");
            return;
        }

        if (plugin.getEnchantManager().applyCustomEnchant(item, enchantName, 1)) {
            player.sendMessage(ChatColor.GREEN + "Successfully applied enchantment!");
        } else {
            player.sendMessage(ChatColor.RED + "Could not apply enchantment to this item!");
        }
    }

    private void handleGiveBook(CommandSender sender, String playerName, String enchantName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        ItemStack book = plugin.getEnchantManager().createEnchantmentBook(enchantName, 1);
        if (book == null) {
            sender.sendMessage(ChatColor.RED + "Invalid enchantment!");
            return;
        }

        target.getInventory().addItem(book);
        sender.sendMessage(ChatColor.GREEN + "Gave enchanted book to " + target.getName());
        target.sendMessage(ChatColor.GREEN + "You received an enchanted book!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("customenchant");
            completions.add("reload");
            completions.add("give");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "customenchant":
                case "give":
                    if (args[0].equalsIgnoreCase("give")) {
                        Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                    } else {
                        completions.addAll(plugin.getEnchantManager().getCustomEnchants().keySet());
                    }
                    break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(plugin.getEnchantManager().getCustomEnchants().keySet());
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
} 