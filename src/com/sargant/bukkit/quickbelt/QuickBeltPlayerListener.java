package com.sargant.bukkit.quickbelt;

import java.util.Arrays;
import java.util.List;

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
	
	@Override
	public void onPlayerQuit(PlayerEvent event) { cleanup(event); }
	
	@Override
	public void onPlayerKick(PlayerKickEvent event) { cleanup(event); }

	@Override
	public void onPlayerMove(PlayerMoveEvent event) { invCheck(event); }
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) { invCheck(event); }
	
	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event) { invCheck(event); }
	
	private void invCheck(PlayerEvent pev) {
		
		Player player = pev.getPlayer();
		
		if(!parent.force) {
		
			String playerCheck = parent.status.get(player.getName());
		
			if(playerCheck == null || playerCheck.equalsIgnoreCase("false")) {
				return;
			}
		}
		
		// We use Lists for hash-checking
		List<ItemStack> inv = Arrays.asList(player.getInventory().getContents());
		
		// If user doesn't exist, insert them to the inventories
		// We may still want to update, so don't cancel
		if(null == parent.inventories.get(player.getName())) {
			parent.inventories.put(player.getName(), inv);
			
		// If the user existed and hasn't changed, give up
		} else if(parent.inventories.get(player.getName()).equals(inv)) {
			return;
		
		// Otherwise the user has changed, and we want to process
		} else {
			parent.inventories.put(player.getName(), inv);
		}
		
		if(inv.size() != 36) {
			parent.log.warning("Inventory is not 36 in size. I am broken. Sad face.");
			return;
		}
		
		// Given the split in the numbers, there are two easy ways of doing this, 
		// either slots first, columns second, or columns first, slots second
		// Doing it the second way to guarantee a vacant slot gets filled form whatever height
		
		// Firstly, let's shuffle everything to the bottom
		String slotsString = parent.useSlots.get(player.getName());
		dropColumns(inv, slotsString);
		
		Boolean didDrop = false;
		// Secondly, we'll move things into their empty slots if vacant
		for(Integer i = 0; i <= 8; i++) {
			
			if(!slotsString.equals("all") && !slotsString.contains(String.valueOf(i+1))) continue;
			
			if(inv.get(i).getType() == Material.AIR && inv.get(i+27).getType() != Material.AIR) {
				didDrop = true;
				ItemStack swap = inv.get(i+27);
				inv.set(i+27, inv.get(i));
				inv.set(i, swap);
				player.sendMessage(ChatColor.AQUA.toString() + "Replenished slot " + (i+1) + ChatColor.WHITE.toString());
			}
		}
		
		// Now we catch up on ourselves and keep dropping to the bottom
		while(didDrop == true) {
			didDrop = dropColumns(inv,  parent.useSlots.get(player.getName()));
		}
		
		parent.inventories.put(player.getName(), inv);
		player.getInventory().setContents((ItemStack[]) inv.toArray());
	}
	
	private void cleanup(PlayerEvent event) {
		parent.inventories.remove(event.getPlayer().getName());
		parent.status.remove(event.getPlayer().getName());
	}
	
	
	private Boolean dropColumns(List<ItemStack> inv, String whatSlots) {
		
		Boolean didDrop = false;
		
		for(Integer i = 9; i <= 26; i++) {
			
			if(!whatSlots.equals("all")) {
				Integer colNumber = 1 + (i % 9);
				if(!whatSlots.contains(String.valueOf(colNumber))) continue;
			}
			
			if(inv.get(i).getType() != Material.AIR && inv.get(i+9).getType() == Material.AIR) {
				didDrop = true;
				ItemStack swap = inv.get(i+9);
				inv.set(i+9, inv.get(i));
				inv.set(i, swap);
			}
		}
		
		return didDrop;
	}
	
}
