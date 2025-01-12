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
package org.l2jmobius.gameserver.network.clientpackets.gacha;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.instancemanager.events.UniqueGachaManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.clientpackets.ClientPacket;
import org.l2jmobius.gameserver.network.serverpackets.gacha.UniqueGachaInvenItemList;

public class ExUniqueGachaInvenItemList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		readByte(); // _inventoryType
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final List<Item> items = new ArrayList<>(UniqueGachaManager.getInstance().getTemporaryWarehouse(player));
		final int totalSize = items.size();
		final int perPage = 150;
		final int totalPages = totalSize / perPage;
		
		for (int i = 0; i <= totalPages; i++)
		{
			// Page on client should start from 1, not 0.
			// If page is set to 0 - nothing shows up at all.
			player.sendPacket(new UniqueGachaInvenItemList((i + 1), (totalPages + 1), items.subList(i * perPage, Math.min((i + 1) * perPage, totalSize))));
		}
	}
}