package dev.ender.etoken;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TokenClickListener implements Listener {
    private final EToken plugin;

    public TokenClickListener(EToken plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTokenClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Controlla che l'oggetto non sia nullo e abbia un metadato
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.LIGHT_PURPLE + "GETTONE")) {
                List<String> lore = meta.getLore();

                // Assicurati che la lore esista e contenga il valore
                if (lore != null && lore.size() > 0) {
                    String amountString = lore.get(0).replace(ChatColor.GRAY + "Valore: ", "").replace(" Gettoni", "");

                    try {
                        double amount = Double.parseDouble(amountString);

                        // Gestisce l'interazione con il gettone (clic sinistro o destro)
                        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR ||
                                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                            // Riscatta il gettone
                            player.sendMessage(ChatColor.GREEN + "Hai riscattato il gettone per un valore di: " + amount);

                            // Aggiungi i gettoni al database per il giocatore
                            plugin.getDatabaseManager().aggiungiGettoni(player.getName(), amount);

                            // Rimuovi completamente il gettone dall'inventario
                            player.getInventory().remove(item);
                        } else if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR ||
                                event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
                            // Mostra il valore del gettone senza riscattarlo
                            player.sendMessage(ChatColor.YELLOW + "Il valore del gettone è: " + amount);
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Il valore del gettone non è valido!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Il tuo GETTONE non ha un valore valido!");
                }
            }
        }
    }
}
