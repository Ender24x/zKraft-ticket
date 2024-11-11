package dev.ender.etoken;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.logging.Level;

public class TokenInventoryManager {
    private final EToken plugin;

    private static final Material TOKEN_MATERIAL = Material.EYE_OF_ENDER;
    private static final String TOKEN_NAME = ChatColor.LIGHT_PURPLE + "GETTONE";
    private static final String LORE_PREFIX = ChatColor.GRAY + "Valore: ";

    public TokenInventoryManager(EToken plugin) {
        this.plugin = plugin;
    }

    public double getPlayerTokenTotal(Player player) {
        double totalTokens = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isTokenItem(item)) {
                List<String> lore = item.getItemMeta().getLore();
                if (lore != null && !lore.isEmpty()) {
                    String amountString = lore.get(0).replace(LORE_PREFIX, "");
                    try {
                        totalTokens += Double.parseDouble(amountString);
                    } catch (NumberFormatException e) {
                        // Logga l'errore di formato
                        plugin.getLogger().log(Level.WARNING, "Formato di gettone non valido: " + amountString);
                    }
                }
            }
        }

        return totalTokens;
    }

    private boolean isTokenItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return item.getType() == TOKEN_MATERIAL &&
                meta != null &&
                meta.hasDisplayName() &&
                TOKEN_NAME.equals(meta.getDisplayName());
    }
}
