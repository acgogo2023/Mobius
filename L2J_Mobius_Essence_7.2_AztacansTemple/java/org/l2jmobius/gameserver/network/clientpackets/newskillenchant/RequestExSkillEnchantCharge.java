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
package org.l2jmobius.gameserver.network.clientpackets.newskillenchant;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.data.xml.SkillEnchantData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.EnchantItemExpHolder;
import org.l2jmobius.gameserver.model.holders.EnchantStarHolder;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.SkillEnchantHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.ClientPacket;
import org.l2jmobius.gameserver.network.serverpackets.newskillenchant.ExSkillEnchantCharge;
import org.l2jmobius.gameserver.network.serverpackets.newskillenchant.ExSkillEnchantInfo;

/**
 * @author Serenitty
 */
public class RequestExSkillEnchantCharge extends ClientPacket
{
	private int _skillId;
	private final List<ItemHolder> _itemList = new ArrayList<>();
	
	@Override
	protected void readImpl()
	{
		_skillId = readInt();
		readInt(); // level
		readInt(); // sublevel
		int size = readInt();
		for (int i = 0; i < size; i++)
		{
			final int objectId = readInt();
			final long count = readLong();
			if (count > 0)
			{
				_itemList.add(new ItemHolder(objectId, count));
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player != null)
		{
			final Skill skill = player.getKnownSkill(_skillId);
			if (skill == null)
			{
				return;
			}
			
			final SkillEnchantHolder skillEnchantHolder = SkillEnchantData.getInstance().getSkillEnchant(skill.getId());
			if (skillEnchantHolder == null)
			{
				return;
			}
			
			final EnchantStarHolder starHolder = SkillEnchantData.getInstance().getEnchantStar(skillEnchantHolder.getStarLevel());
			if (starHolder == null)
			{
				return;
			}
			
			int curExp = player.getSkillEnchantExp(starHolder.getLevel());
			long feeAdena = 0;
			for (ItemHolder itemCharge : _itemList)
			{
				final Item item = player.getInventory().getItemByObjectId(itemCharge.getId());
				if (item == null)
				{
					PacketLogger.warning(getClass().getSimpleName() + " Player" + player.getName() + " trying charge skill exp enchant with not exist item by objectId - " + itemCharge.getId());
					continue;
				}
				
				final EnchantItemExpHolder itemExpHolder = SkillEnchantData.getInstance().getEnchantItem(starHolder.getLevel(), item.getId());
				if (itemExpHolder != null)
				{
					feeAdena = itemCharge.getCount() * starHolder.getFeeAdena();
					if (player.getAdena() < feeAdena)
					{
						player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
						player.sendPacket(new ExSkillEnchantCharge(skill.getId(), 0));
						return;
					}
					else if (itemExpHolder.getStarLevel() <= starHolder.getLevel())
					{
						curExp += itemExpHolder.getExp() * itemCharge.getCount();
						player.destroyItem("Charge", item, itemCharge.getCount(), null, true);
					}
					else
					{
						PacketLogger.warning(getClass().getSimpleName() + " Player" + player.getName() + " trying charge item with not support star level skillstarLevel-" + starHolder.getLevel() + " itemStarLevel-" + itemExpHolder.getStarLevel() + " itemId-" + itemExpHolder.getId());
					}
				}
				else
				{
					PacketLogger.warning(getClass().getSimpleName() + " Player" + player.getName() + " trying charge skill with missed item on XML  itemId-" + item.getId());
				}
			}
			player.setSkillEnchantExp(starHolder.getLevel(), Math.min(starHolder.getExpMax(), curExp));
			player.reduceAdena("ChargeFee", feeAdena, null, true);
			player.sendPacket(new ExSkillEnchantCharge(skill.getId(), 0));
			player.sendPacket(new ExSkillEnchantInfo(skill, player));
		}
	}
}
