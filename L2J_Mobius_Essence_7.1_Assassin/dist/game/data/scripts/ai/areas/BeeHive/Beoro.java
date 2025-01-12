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
package ai.areas.BeeHive;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.AggroInfo;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;
import ai.others.Atingo;

public class Beoro extends AbstractNpcAI
{
	private static final int BEORO = 25948;
	private static final int HERMIT_BEORO = 25949;
	private static final int ROYDA = 25950;
	private static final int SIN_EATER = 25951;
	private static final NpcStringId[] ANNOUNCEMENTS_50_HP =
	{
		NpcStringId.I_WILL_FIGHT_TO_THE_DEATH,
		NpcStringId.DON_T_FORGET_THAT_BEE_HIVE_IS_A_TRAINING_GROUND_FOR_PETS_IF_YOU_RE_NOT_A_PET_DON_T_COME,
		NpcStringId.I_AM_THE_RULER_OF_BEE_HIVE_YOUR_ATTACKS_ARE_NOTHING_MORE_THAN_BEE_STINGS_TO_ME,
		NpcStringId.YOU_ARE_NOT_PETS_BUT_YOU_STILL_WANT_ME_TO_SURRENDER_DIE,
	};
	private static final SkillHolder BEORO_ROAR = new SkillHolder(48469, 1);
	private static final Map<Integer, Boolean> ROYDA_SPAWN_HP_PERCENTAGES = new HashMap<>();
	static
	{
		ROYDA_SPAWN_HP_PERCENTAGES.put(80, false);
		ROYDA_SPAWN_HP_PERCENTAGES.put(70, false);
		ROYDA_SPAWN_HP_PERCENTAGES.put(60, false);
		ROYDA_SPAWN_HP_PERCENTAGES.put(47, false);
		ROYDA_SPAWN_HP_PERCENTAGES.put(36, false);
		ROYDA_SPAWN_HP_PERCENTAGES.put(25, false);
		ROYDA_SPAWN_HP_PERCENTAGES.put(17, false);
		ROYDA_SPAWN_HP_PERCENTAGES.put(7, false);
	}
	private boolean _beoroTransformAttempted = false;
	
	private Beoro()
	{
		addSpawnId(BEORO);
		addAttackId(BEORO, HERMIT_BEORO, ROYDA);
		addSkillSeeId(BEORO, HERMIT_BEORO, ROYDA);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		_beoroTransformAttempted = false;
		for (int key : ROYDA_SPAWN_HP_PERCENTAGES.keySet())
		{
			ROYDA_SPAWN_HP_PERCENTAGES.put(key, false);
		}
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if ((npc.getId() != ROYDA) && !attacker.isPet())
		{
			petrify(npc, attacker);
			npc.setCurrentHp(npc.getCurrentHp() + damage);
			return null;
		}
		
		if (npc.getId() == BEORO)
		{
			synchronized (this)
			{
				if (!_beoroTransformAttempted && (npc.getCurrentHpPercent() < 50))
				{
					_beoroTransformAttempted = true;
					npc.broadcastPacket(new ExShowScreenMessage(ANNOUNCEMENTS_50_HP[Rnd.get(ANNOUNCEMENTS_50_HP.length)], ExShowScreenMessage.BOTTOM_RIGHT, 5000, false));
					if (Rnd.get(100) < 50)
					{
						((Attackable) addSpawn(HERMIT_BEORO, npc)).addDamageHate(attacker, 1, 5000);
						npc.deleteMe();
					}
				}
			}
		}
		if ((npc.getId() == BEORO) || (npc.getId() == HERMIT_BEORO))
		{
			synchronized (npc)
			{
				for (Entry<Integer, Boolean> entry : ROYDA_SPAWN_HP_PERCENTAGES.entrySet())
				{
					if ((entry.getKey() <= npc.getCurrentHpPercent()) && !entry.getValue())
					{
						ROYDA_SPAWN_HP_PERCENTAGES.put(entry.getKey(), true);
						((Attackable) addSpawn(ROYDA, npc, true, 10 * 60 * 1000)).addDamageHate(attacker, 1, 5000);
						break;
					}
				}
			}
		}
		if (npc.getId() == ROYDA)
		{
			synchronized (npc)
			{
				if ((npc.getScriptValue() == 0) && (npc.getCurrentHpPercent() < 70))
				{
					npc.setScriptValue(1);
					final int currentPetId = Rnd.get(100) < 25 ? SIN_EATER : Atingo.PETS[Rnd.get(Atingo.PETS.length)];
					((Attackable) addSpawn(currentPetId, npc, true, 10 * 60 * 1000)).addDamageHate(attacker, 1, 5000);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		if (isSummon)
		{
			return null;
		}
		
		boolean isTarget = false;
		for (WorldObject target : targets)
		{
			if (target == npc)
			{
				isTarget = true;
				break;
			}
		}
		if (isTarget)
		{
			petrify(npc, caster);
		}
		
		return null;
	}
	
	private void petrify(Npc npc, Playable playable)
	{
		playable.stopSkillEffects(SkillFinishType.REMOVED, 50196); // Opal
		playable.stopSkillEffects(SkillFinishType.REMOVED, 1411); // Mystic Immunity
		BEORO_ROAR.getSkill().applyEffects(npc, playable);
		
		final AggroInfo aggr = ((Attackable) npc).getAggroList().getOrDefault(playable, null);
		if (aggr != null)
		{
			((Attackable) npc).reduceHate(playable, -aggr.getHate());
		}
		if (npc.getTarget() == playable)
		{
			npc.setTarget(null);
		}
	}
	
	public static void main(String[] args)
	{
		new Beoro();
	}
}
