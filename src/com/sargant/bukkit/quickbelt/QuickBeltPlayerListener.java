package com.sargant.bukkit.quickbelt;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class QuickBeltPlayerListener extends PlayerListener {
	
	private QuickBelt parent;

	public QuickBeltPlayerListener(QuickBelt instance)
	{
		parent = instance;
	}

	public void onPlayerMove(PlayerMoveEvent event) { invCheck(event.getPlayer()); }
	public void onPlayerAnimation(PlayerAnimationEvent event) { invCheck(event.getPlayer()); }
	public void onPlayerDropItem(PlayerDropItemEvent event) { invCheck(event.getPlayer()); }
	
	public void invCheck(Player player) {
		
		if(!parent.force) {
		
			Boolean playerCheck = parent.status.get(player.getName());
		
			if(playerCheck == null || playerCheck == false) {
				return;
			}
		}
		
		ItemStack[] inventory = player.getInventory().getContents();
		
		if(inventory.length != 36) {
			parent.log.warning("Inventory is not 36 in size. I am broken. Sad face.");
			return;
		}
		
		for(Integer i = 0; i < 27; i++) {
			
			if(inventory[i].getType() == Material.AIR) {
				
				inventory[i].setType(inventory[i+9].getType());
				inventory[i].setAmount(inventory[i+9].getAmount());
				inventory[i].setData(inventory[i+9].getData());
				inventory[i].setDurability(inventory[i+9].getDurability());
				
				inventory[i+9].setType(Material.AIR);
				
				if(i < 9 && inventory[i].getType() != Material.AIR && !parent.silent) {
					player.sendMessage(ChatColor.AQUA.toString() + "Replenished slot " + (i+1) + ChatColor.WHITE.toString());
				}
			}
		}
		
		player.getInventory().setContents(inventory);
	}
}
