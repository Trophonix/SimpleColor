package com.trophonix.simplecolor;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleColor extends JavaPlugin implements Listener {

  private static final ChatColor[] COLORS = ChatColor.values();

  private boolean cancel;
  private boolean removeCode;
  private String errorMessage;

  @Override public void onEnable() {
    saveDefaultConfig();
    load();
    getServer().getPluginManager().registerEvents(this, this);
  }

  private void load() {
    cancel = getConfig().getBoolean("noPermission.cancel", false);
    removeCode = getConfig().getBoolean("noPermission.removeCode", true);
    errorMessage = getConfig().getString("noPermission.errorMessage");
    if (errorMessage != null) {
      errorMessage = ChatColor.translateAlternateColorCodes('&', errorMessage);
    }
  }

  @EventHandler (priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent event) {
    if (event.getPlayer().hasPermission("simplecolor.chat.*")) {
      event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
      return;
    }
    for (ChatColor color : COLORS) {
      if (!event.getMessage().contains("&" + color.getChar())) continue;
      if (event.getPlayer().hasPermission("simplecolor.chat." + color.getChar())) {
        event.setMessage(event.getMessage().replace("&" + color.getChar(), color.toString()));
      } else {
        if (removeCode) {
          event.setMessage(event.getMessage().replace("&" + color.getChar(), ""));
        } else if (cancel) {
          event.setCancelled(true);
        }
        if (errorMessage != null && !errorMessage.isEmpty()) {
          event.getPlayer().sendMessage(errorMessage
            .replace("{NAME}", color.name().toLowerCase().replace("_", " ")
            .replace("{CHAR}", new String(new char[]{color.getChar()}))));
        }
      }
    }
  }

  @EventHandler
  public void onSign(SignChangeEvent event) {
    if (event.getPlayer().hasPermission("simplecolor.sign.*")) {
      for (int i = 0; i < event.getLines().length; i++) {
        String line = event.getLine(i);
        if (line == null) continue;
        event.setLine(i, ChatColor.translateAlternateColorCodes('&', line));
      }
      return;
    }
    for (ChatColor color : COLORS) {
      for (int i = 0; i < event.getLines().length; i++) {
        String line = event.getLine(i);
        if (line == null || !line.contains("&" + color.getChar())) continue;
        if (event.getPlayer().hasPermission("simplecolor.chat." + color.getChar())) {
          event.setLine(i, line.replace("&" + color.getChar(), color.toString()));
        } else {
          if (cancel) {
            event.setCancelled(true);
            event.getBlock().breakNaturally();
          } else if (removeCode) {
            event.setLine(i, line.replace("&" + color.getChar(), ""));
          }
          if (errorMessage != null && !errorMessage.isEmpty()) {
            event.getPlayer().sendMessage(errorMessage
              .replace("{NAME}", color.name().toLowerCase().replace("_", " ")
              .replace("{CHAR}", new String(new char[]{color.getChar()}))));
          }
        }
      }
    }
  }

}
