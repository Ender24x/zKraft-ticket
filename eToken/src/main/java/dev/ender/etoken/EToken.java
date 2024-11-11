package dev.ender.etoken;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class EToken extends JavaPlugin implements CommandExecutor {
    private DatabaseManager db;
    private TokenManager tokenManager;
    private TokenInventoryManager tokenInventoryManager;
    private final String PREFIX = "§d§lBaller§7§lMC §8» ";

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', "&4&lBaller&f&lMC » ");
    }



    @Override
    public void onEnable() {
        db = new DatabaseManager(this);
        db.connect();
        db.createTable();

        tokenManager = new TokenManager(this);
        tokenInventoryManager = new TokenInventoryManager(this);

        this.getCommand("assegno").setExecutor(this);
        this.getCommand("riscattare").setExecutor(this);
        this.getCommand("converti").setExecutor(this);
        this.getCommand("gettoni").setExecutor(this);
        this.getCommand("et").setExecutor(this); // Registra il comando "et"


        getServer().getPluginManager().registerEvents(new TokenClickListener(this), this);


        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ETokenPlaceholder(this).register();
            getLogger().info("PlaceholderAPI trovata, placeholder %etoken_totale% registrato.");
        } else {
            getLogger().warning("PlaceholderAPI non trovata. Il placeholder %etoken_totale% non sarà disponibile.");
        }
    }

    @Override
    public void onDisable() {
        if (db != null) {
            db.disconnect();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;


        if (sender instanceof Player) {
            player = (Player) sender;
        }

        switch (cmd.getName().toLowerCase()) {
            case "assegno":
                if (args.length != 1) {
                    sendUsageMessage(player, "assegno <importo>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[0]);
                    db.createAssegno(player != null ? player.getName() : "Console", amount);
                    sendSuccessMessage(player, "Hai creato un assegno di " + amount + " Gettoni.");
                } catch (NumberFormatException e) {
                    sendErrorMessage(player, "Importo non valido.");
                }
                break;

            case "riscattare":
                if (args.length != 1) {
                    sendUsageMessage(player, "riscattare <importo>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[0]);
                    if (db.riscattareAssegno(player != null ? player.getName() : "Console", amount)) {
                        sendSuccessMessage(player, "Hai riscattato " + amount + " Gettoni.");
                    } else {
                        sendErrorMessage(player, "Non hai assegni sufficienti per riscattare.");
                    }
                } catch (NumberFormatException e) {
                    sendErrorMessage(player, "Importo non valido.");
                }
                break;

            case "converti":
                if (args.length != 1) {
                    sendUsageMessage(player, "converti <importo>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[0]);
                    if (db.convertiAssegnoInGettoni(player != null ? player.getName() : "Console", amount)) {
                        // Crea un nuovo ItemStack di Ender Eye
                        ItemStack tokenItem = new ItemStack(Material.EYE_OF_ENDER, 1);

                        // Imposta il nome e la descrizione dell'item
                        ItemMeta meta = tokenItem.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "GETTONE"); // Nome in rosa
                            List<String> lore = new ArrayList<>();
                            lore.add(ChatColor.GRAY + "Valore: " + amount + " Gettoni"); // Descrizione
                            lore.add(ChatColor.GRAY + "Convertito da: " + (player != null ? player.getName() : "Console")); // Aggiungi il nome del giocatore
                            meta.setLore(lore);
                            tokenItem.setItemMeta(meta);
                        }

                        // Aggiungi l'item all'inventario del giocatore
                        if (player != null) {
                            player.getInventory().addItem(tokenItem);
                            sendSuccessMessage(player, "Hai convertito " + amount + " assegni in un GETTONE.");
                        } else {
                            Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.GREEN + "Hai convertito " + amount + " assegni in un GETTONE.");
                        }
                    } else {
                        sendErrorMessage(player, "Non hai assegni sufficienti per convertire.");
                    }
                } catch (NumberFormatException e) {
                    sendErrorMessage(player, "Importo non valido.");
                }
                break;

            case "gettoni":
                if (player == null) {
                    Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "Questo comando può essere usato solo dai giocatori.");
                    return true;
                }
                double balance = db.getSaldo(player.getName());
                sendSuccessMessage(player, "Hai " + balance + " Gettoni.");
                break;

            case "et":
                if (args.length != 3) {
                    sendUsageMessage(player, "et <give/remove> <giocatore> <quantità>");
                    return true;
                }

                String action = args[0].toLowerCase();
                String targetName = args[1];
                Player target = Bukkit.getPlayer(targetName);


                if (player == null) {

                    if (target == null) {
                        Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "Il giocatore specificato non è online.");
                        return true;
                    }
                } else {

                    if (target == null) {
                        sendErrorMessage(player, "Il giocatore specificato non è online.");
                        return true;
                    }
                }

                try {
                    double amount = Double.parseDouble(args[2]);
                    if (action.equals("give")) {
                        db.createAssegno(target.getName(), amount);
                        sendSuccessMessage(player, "Hai dato " + amount + " Gettoni a " + target.getName() + ".");
                        target.sendMessage(PREFIX + ChatColor.GREEN + "Hai ricevuto " + amount + " Gettoni da " + (player != null ? player.getName() : "la console") + ".");
                    } else if (action.equals("remove")) {
                        double removedAmount = amount; // La somma da rimuovere
                        if (db.removeAssegno(target.getName(), removedAmount)) {
                            sendSuccessMessage(player, "Hai rimosso " + removedAmount + " Gettoni a " + target.getName() + ".");
                            target.sendMessage(PREFIX + ChatColor.RED + "Hai perso " + removedAmount + " Gettoni.");
                        } else {
                            sendErrorMessage(player, "Il giocatore non ha abbastanza Gettoni per rimuoverli.");
                        }
                    } else {
                        sendErrorMessage(player, "Azione non valida. Usa 'give' o 'remove'.");
                    }
                } catch (NumberFormatException e) {
                    sendErrorMessage(player, "Quantità non valida.");
                }
                break;

            default:
                return false;
        }

        return true;
    }

    private void sendUsageMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Uso corretto: /" + message);
        } else {
            Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "Uso corretto: /" + message);
        }
    }

    private void sendSuccessMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(PREFIX + ChatColor.GREEN + message);
        } else {
            Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.GREEN + message);
        }
    }

    private void sendErrorMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(PREFIX + ChatColor.RED + message);
        } else {
            Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + message);
        }
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public TokenInventoryManager getTokenInventoryManager() {
        return tokenInventoryManager;
    }

    public DatabaseManager getDatabaseManager() {
        return db;
    }
}
