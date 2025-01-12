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
package org.l2jmobius.gameserver.model.zone.type;

import org.l2jmobius.gameserver.data.xml.TimedHuntingZoneData;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.MapRegionManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.TimedHuntingZoneHolder;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author Mobius
 */
public class TimedHuntingZone extends ZoneType
{
	public TimedHuntingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if (!creature.isPlayer())
		{
			return;
		}
		
		final Player player = creature.getActingPlayer();
		if (player != null)
		{
			player.setInsideZone(ZoneId.TIMED_HUNTING, true);
			
			for (TimedHuntingZoneHolder holder : TimedHuntingZoneData.getInstance().getAllHuntingZones())
			{
				if (!player.isInTimedHuntingZone(holder.getZoneId()))
				{
					continue;
				}
				
				final int remainingTime = player.getTimedHuntingZoneRemainingTime(holder.getZoneId());
				if (remainingTime > 0)
				{
					player.startTimedHuntingZone(holder.getZoneId());
					if (holder.isPvpZone())
					{
						if (!player.isInsideZone(ZoneId.PVP))
						{
							player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
						}
						
						player.setInsideZone(ZoneId.PVP, true);
						if (player.hasServitors())
						{
							player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.PVP, true));
						}
						if (player.hasPet())
						{
							player.getPet().setInsideZone(ZoneId.PVP, true);
						}
					}
					else if (holder.isNoPvpZone())
					{
						player.setInsideZone(ZoneId.NO_PVP, true);
						if (player.hasServitors())
						{
							player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.NO_PVP, true));
						}
						if (player.hasPet())
						{
							player.getPet().setInsideZone(ZoneId.NO_PVP, true);
						}
					}
					
					// Send player info to nearby players.
					if (!player.isTeleporting())
					{
						player.broadcastInfo();
					}
					return;
				}
				break;
			}
			
			if (!player.isGM())
			{
				player.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN));
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (!creature.isPlayer())
		{
			return;
		}
		
		final Player player = creature.getActingPlayer();
		if (player != null)
		{
			player.setInsideZone(ZoneId.TIMED_HUNTING, false);
			
			final TimedHuntingZoneHolder holder = player.getTimedHuntingZone();
			if (holder != null)
			{
				if (holder.isPvpZone())
				{
					player.setInsideZone(ZoneId.PVP, false);
					if (player.hasServitors())
					{
						player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.PVP, false));
					}
					if (player.hasPet())
					{
						player.getPet().setInsideZone(ZoneId.PVP, false);
					}
					
					if (!player.isInsideZone(ZoneId.PVP))
					{
						creature.sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
					}
				}
				else if (holder.isNoPvpZone())
				{
					player.setInsideZone(ZoneId.NO_PVP, false);
					if (player.hasServitors())
					{
						player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.NO_PVP, false));
					}
					if (player.hasPet())
					{
						player.getPet().setInsideZone(ZoneId.NO_PVP, false);
					}
				}
				
				// Send player info to nearby players.
				if (!player.isTeleporting())
				{
					player.broadcastInfo();
				}
			}
		}
	}
}
