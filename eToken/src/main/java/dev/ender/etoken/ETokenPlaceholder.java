package dev.ender.etoken;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ETokenPlaceholder extends PlaceholderExpansion {
    private final EToken plugin;

    public ETokenPlaceholder(EToken plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "etoken";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("totale")) {
            double saldo = plugin.getDatabaseManager().getSaldo(player.getName());
            return String.valueOf(saldo);
        }

        if (identifier.equals("token_inv")) {
            double totalTokens = plugin.getTokenInventoryManager().getPlayerTokenTotal(player);
            return String.valueOf(totalTokens);
        }

        return null;
    }
}
