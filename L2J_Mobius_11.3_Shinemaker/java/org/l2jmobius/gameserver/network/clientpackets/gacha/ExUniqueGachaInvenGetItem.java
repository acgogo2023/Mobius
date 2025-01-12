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
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.network.clientpackets.ClientPacket;
import org.l2jmobius.gameserver.network.serverpackets.gacha.UniqueGachaInvenGetItem;

public class ExUniqueGachaInvenGetItem extends ClientPacket
{
	private List<ItemHolder> _requestedItems;
	
	@Override
	protected void readImpl()
	{
		final int size = readInt();
		_requestedItems = new ArrayList<>(size);
		for (int index = 0; index < size; index++)
		{
			_requestedItems.add(new ItemHolder(readInt(), readLong()));
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final boolean isSuccess = UniqueGachaManager.getInstance().receiveItemsFromTemporaryWarehouse(player, _requestedItems);
		player.sendPacket(new UniqueGachaInvenGetItem(isSuccess));
	}
}
