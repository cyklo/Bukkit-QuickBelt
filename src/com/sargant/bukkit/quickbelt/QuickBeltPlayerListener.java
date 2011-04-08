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

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class QuickBeltPlayerListener extends PlayerListener {
	
	private QuickBelt parent;

	public QuickBeltPlayerListener(QuickBelt instance) {
		parent = instance;
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) { cleanup(event); }
	
	@Override
	public void onPlayerKick(PlayerKickEvent event) { cleanup(event); }

	@Override
	public void onPlayerMove(PlayerMoveEvent event) { invCheck(event); }
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) { invCheck(event); }
	
	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event) { invCheck(event); }
	
	private void invCheck(PlayerEvent pev) {
		
		// Check the player wants to use quickbelt
		Player player = pev.getPlayer();
		if(!parent.isPlayerEnabled(player)) return;
		
		// We use Lists for hash-checking
		List<ItemStack> current_inv = Arrays.asList(player.getInventory().getContents());
		List<ItemStack> previous_inv = parent.inventories.get(player.getName());
		
		// If user doesn't exist, insert them to the inventories
		// We may still want to update, so don't cancel
		if(previous_inv == null) {
			parent.inventories.put(player.getName(), current_inv);
			return;
			
		// If the user existed and hasn't changed, give up
		} else if(previous_inv.equals(current_inv)) {
			return;
		
		// Otherwise the user has changed, and we want to process
		} else {
			parent.inventories.put(player.getName(), current_inv);
		}
		
		if(current_inv.size() != 36) {
			parent.log.warning("Inventory is not 36 in size. I am broken. Sad face.");
			return;
		}
		
		// Given the split in the numbers, there are two easy ways of doing this, 
		// either slots first, columns second, or columns first, slots second
		// Doing it the second way to guarantee a vacant slot gets filled form whatever height
		algorithmDrop(player, previous_inv, current_inv);

		parent.inventories.put(player.getName(), current_inv);
		player.getInventory().setContents((ItemStack[]) current_inv.toArray());
	}
	
	private void cleanup(PlayerEvent event) {
		// No need to clean up just a few bytes...
	}
	
	
	private Boolean dropColumns(List<ItemStack> inv, String whatSlots) {
		
		Boolean didDrop = false;
		
		if(whatSlots == null) whatSlots = "all";
		
		for(Integer i = 9; i <= 26; i++) {
			if(!whatSlots.equals("all")) {
				Integer colNumber = 1 + (i % 9);
				if(!whatSlots.contains(String.valueOf(colNumber))) continue;
			}
			
			if(!isAir(inv.get(i)) && isAir(inv.get(i+9))) { 
			    didDrop = true;
			    ItemStack swap = inv.get(i+9);
	            inv.set(i+9, inv.get(i));
	            inv.set(i, swap);
			}
			
		}
		return didDrop;
	}
	
	private void algorithmDrop(Player player, List<ItemStack> previous_inv, List<ItemStack> current_inv) {
		
		String slotsString = parent.useSlots.get(player.getName());
		dropColumns(current_inv, slotsString);
		
		if(slotsString == null) slotsString = "all";
		
		Boolean didDrop = false;
		// Secondly, we'll move things into their empty slots if vacant
		for(Integer i = 0; i <= 8; i++) {
			
			if(!slotsString.equals("all") && !slotsString.contains(String.valueOf(i+1))) continue;
			
			if(isAir(current_inv.get(i)) && !isAir(current_inv.get(i+27))) {
				didDrop = true;
				ItemStack swap = current_inv.get(i+27);
				current_inv.set(i+27, current_inv.get(i));
				current_inv.set(i, swap);
				
				if(!parent.silent) {
					player.sendMessage(ChatColor.AQUA.toString() + "Replenished slot " + (i+1) + ChatColor.WHITE.toString());
				}
			}
		}
		
		// Now we catch up on ourselves and keep dropping to the bottom
		while(didDrop == true) {
			didDrop = dropColumns(current_inv,  parent.useSlots.get(player.getName()));
		}
	}
	
	private boolean isAir(ItemStack m) {
	    if(m == null) return true;
	    if(m.getType() == Material.AIR) return true;
	    return false;
	}
}
