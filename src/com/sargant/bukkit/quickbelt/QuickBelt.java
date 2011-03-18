// QuickBelt - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.quickbelt;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sargant.bukkit.common.Common;

public class QuickBelt extends JavaPlugin {
	
	protected HashMap<String, String> status;
	protected HashMap<String, String> useSlots;
	protected HashMap<String, List<ItemStack>> inventories;
	protected final Logger log;
	protected QuickBeltPlayerListener playerListener;
	protected Boolean force;
	protected Boolean silent;
	
	public QuickBelt() {
		log = Logger.getLogger("Minecraft");
		playerListener = new QuickBeltPlayerListener(this);
		status = new HashMap<String, String>();
		useSlots = new HashMap<String, String>();
		inventories = new HashMap<String, List<ItemStack>>();
		force = false;
		silent = false;
	}

	@Override
	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	@Override
	public void onEnable() {
		
		getDataFolder().mkdirs();
		File yml = new File(getDataFolder(), "config.yml");

		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				getConfiguration().setProperty("quickbelt", null);
				getConfiguration().save();
			} catch (IOException ex){
				log.warning(getDescription().getName() + ": could not generate config.yml. Are the file permissions OK?");
			}
		}
		
		// Load in the values from the configuration file
		
		List <String> keys = Common.getRootKeys(this);
		
		if(keys == null || !keys.contains("quickbelt")) {
			log.warning(getDescription().getName() + ": configuration file is corrupt. Please delete it and start over.");
			return;
		}
		
		if(keys.contains("force")) {
			force = getConfiguration().getBoolean("force", false);
		} else {
			getConfiguration().setProperty("force", false);
			getConfiguration().save();
		}
		
		if(keys.contains("silent")) {
			silent = getConfiguration().getBoolean("silent", false);
		} else {
			getConfiguration().setProperty("silent", false);
			getConfiguration().save();
		}
		
		List <String> users = getConfiguration().getKeys("quickbelt");
		
		if(users != null) {
			for(String u : users) {
				String st = getConfiguration().getString("quickbelt."+u+".enabled", "false");
				status.put(u, st);
				
				String sl = getConfiguration().getString("quickbelt."+u+".slots", "all");
				useSlots.put(u, sl);
			}
		}
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);
		
		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Lowest, this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			return false;
		}
		
		if(force) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if(args.length < 1) {
			String retstr = ChatColor.YELLOW.toString();
			
			retstr += "Your QuickBelt is currently";
			
			String playerStatus = status.get(player.getName()); 
			
			if(playerStatus == null || playerStatus.equalsIgnoreCase("false")) {
				retstr += ChatColor.RED.toString() + " disabled\n";
				retstr += ChatColor.YELLOW.toString() + "Type '/qb drop' to enable";
			} else {
				retstr += "set to " + ChatColor.GREEN.toString() + " drop mode\n";
				retstr += ChatColor.YELLOW.toString() + "Type '/qb off' to disable";
			}
			
			retstr += ChatColor.WHITE.toString();
			player.sendMessage(retstr);
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("off")) {
			
			status.put(player.getName(), "off");
			updateConfig(player.getName(), "enabled", "off");
			
			player.sendMessage(ChatColor.AQUA.toString() + "QuickBelt disabled");
			return true;
			
		} else if(args[0].equalsIgnoreCase("drop")) {
			
			status.put(player.getName(), "drop");
			updateConfig(player.getName(), "enabled", "drop");
			
			player.sendMessage(ChatColor.AQUA.toString() + "QuickBelt set to drop mode");
			return true;
			
		} else if(args[0].equalsIgnoreCase("slots")) {
			
			if(args.length < 2) {
				
				String slotsString = useSlots.get(player.getName());
				
				if(useSlots.get(player.getName()).equals("all")) {
					player.sendMessage(ChatColor.AQUA.toString() + "All slots enabled");
				} else if(slotsString.matches("^[0-9]+$")) {
					player.sendMessage(slotsStatus(slotsString));
				} else {
					player.sendMessage(ChatColor.RED + "Your slots record is unreadable.");
				}
				
				return true;
			}
			
			else if(args[1].equalsIgnoreCase("all")) {
				
				useSlots.put(player.getName(), "all");
				updateConfig(player.getName(), "slots", "all");
				
				player.sendMessage(ChatColor.AQUA.toString() + "All slots enabled");
				return true;
				
			} else if(args[1].matches("^[0-9]+$")) {
				
				useSlots.put(player.getName(), args[1]);
				updateConfig(player.getName(), "slots", args[1]);

				player.sendMessage(slotsStatus(args[1]));
				return true;
			}
			
		}
			
		player.sendMessage(ChatColor.AQUA.toString() + "Unrecognized command");
		return false;
	}
	
	private void updateConfig(String player, String component, String value) {
		getConfiguration().setProperty("quickbelt." + player + "." + component, value);
		getConfiguration().save();
	}
	
	private String slotsStatus(String slots) {
		String retval = ChatColor.AQUA.toString() + "Slot status:";
		
		for(int i = 1; i <= 9; i++) {
			retval += (slots.contains(String.valueOf(i))) ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
			retval += " " + i;
		}
		
		return retval;
	}
	
	protected Boolean isPlayerEnabled(Player p) {
		
		if(force) return true;
		if(p == null) return false;
		
		String pchk = status.get(p.getName());
		
		if(pchk == null) return false;
		if(pchk.equalsIgnoreCase("false")) return false;
		if(pchk.equalsIgnoreCase("off")) return false;
		
		return true;
	}
}
