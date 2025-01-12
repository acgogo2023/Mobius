/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.model.actor.request;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;

/**
 * @author UnAfraid
 */
public class EnchantItemRequest extends AbstractRequest
{
	private volatile int _enchantingItemObjectId;
	private volatile int _enchantingScrollObjectId;
	private volatile int _supportItemObjectId;
	
	public EnchantItemRequest(Player player, int enchantingScrollObjectId)
	{
		super(player);
		_enchantingScrollObjectId = enchantingScrollObjectId;
	}
	
	public Item getEnchantingItem()
	{
		return getPlayer().getInventory().getItemByObjectId(_enchantingItemObjectId);
	}
	
	public void setEnchantingItem(int objectId)
	{
		_enchantingItemObjectId = objectId;
	}
	
	public Item getEnchantingScroll()
	{
		return getPlayer().getInventory().getItemByObjectId(_enchantingScrollObjectId);
	}
	
	public void setEnchantingScroll(int objectId)
	{
		_enchantingScrollObjectId = objectId;
	}
	
	public Item getSupportItem()
	{
		return getPlayer().getInventory().getItemByObjectId(_supportItemObjectId);
	}
	
	public void setSupportItem(int objectId)
	{
		_supportItemObjectId = objectId;
	}
	
	@Override
	public boolean isItemRequest()
	{
		return true;
	}
	
	@Override
	public boolean canWorkWith(AbstractRequest request)
	{
		return !request.isItemRequest();
	}
	
	@Override
	public boolean isUsing(int objectId)
	{
		return (objectId > 0) && ((objectId == _enchantingItemObjectId) || (objectId == _enchantingScrollObjectId) || (objectId == _supportItemObjectId));
	}
}
