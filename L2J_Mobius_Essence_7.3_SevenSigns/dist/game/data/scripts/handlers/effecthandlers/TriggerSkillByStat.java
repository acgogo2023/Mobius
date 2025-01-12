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
package handlers.effecthandlers;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Mobius
 */
public class TriggerSkillByStat extends AbstractEffect
{
	private final Stat _stat;
	private final int _skillId;
	private final int _skillLevel;
	private final int _skillSubLevel;
	private final int _min;
	private final int _max;
	
	public TriggerSkillByStat(StatSet params)
	{
		_stat = params.getEnum("stat", Stat.class);
		_skillId = params.getInt("skillId", 0);
		_skillLevel = params.getInt("skillLevel", 1);
		_skillSubLevel = params.getInt("skillSubLevel", 0);
		_min = params.getInt("min", 0);
		_max = params.getInt("max", 9999);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		if (effected == null)
		{
			return;
		}
		
		// In some cases, without ThreadPool, values did not apply.
		final Creature target = effected;
		ThreadPool.schedule(() ->
		{
			final int currentValue = (int) effected.getStat().getValue(_stat);
			if ((currentValue >= _min) && (currentValue <= _max))
			{
				if (!target.isAffectedBySkill(_skillId))
				{
					SkillCaster.triggerCast(target, target, SkillData.getInstance().getSkill(_skillId, _skillLevel, _skillSubLevel));
				}
			}
			else
			{
				target.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, _skillId);
			}
		}, 100);
	}
}
