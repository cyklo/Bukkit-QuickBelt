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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sargant.bukkit.common.Common;

public class QuickBelt extends JavaPlugin {
	
	protected HashMap<String, Boolean> status;
	protected final Logger log;
	protected QuickBeltPlayerListener playerListener;
	protected Boolean force;
	protected Boolean silent;
	
	public QuickBelt() {
		log = Logger.getLogger("Minecraft");
		playerListener = new QuickBeltPlayerListener(this);
		status = new HashMap<String, Boolean>();
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
		
		List <String> users = getConfiguration().getStringList("quickbelt", null);
		
		if(users != null) {
			for(String u : users) {
				status.put(u, getConfiguration().getBoolean("quickbelt."+u+".enabled", false));
			}
		}
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Highest, this);

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
			
			Boolean playerStatus = (Boolean) status.get(player.getName()); 
			
			if(playerStatus == null || playerStatus == false) {
				retstr += ChatColor.RED.toString() + " disabled\n";
				retstr += ChatColor.YELLOW.toString() + "Type '/qb on' to enable";
			} else {
				retstr += ChatColor.GREEN.toString() + " enabled\n";
				retstr += ChatColor.YELLOW.toString() + "Type '/qb off' to disable";
			}
			
			retstr += ChatColor.WHITE.toString();
			player.sendMessage(retstr);
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("off")) {
			
			status.put(player.getName(), false);
			
			getConfiguration().setProperty("quickbelt." + player.getName() + ".enabled", false);
			getConfiguration().save();
			
			player.sendMessage(ChatColor.AQUA.toString() + "QuickBelt disabled" + ChatColor.AQUA.toString());
			return true;
			
		} else if(args[0].equalsIgnoreCase("on")) {
			
			status.put(player.getName(), true);
			getConfiguration().setProperty("quickbelt." + player.getName() + ".enabled", true);
			getConfiguration().save();
			
			player.sendMessage(ChatColor.AQUA.toString() + "QuickBelt enabled" + ChatColor.AQUA.toString());
			return true;
		}
			
		player.sendMessage(ChatColor.AQUA.toString() + "Unrecognized command");
		return false;
	}
}
