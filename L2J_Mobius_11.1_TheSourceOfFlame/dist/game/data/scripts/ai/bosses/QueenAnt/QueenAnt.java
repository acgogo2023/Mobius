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
package ai.bosses.QueenAnt;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

/**
 * Queen Ant's AI
 * @author Mobius
 */
public class QueenAnt extends AbstractNpcAI
{
	// NPC
	private static final int QUEEN_ANT = 29381;
	// Status
	private static final byte ALIVE = 0; // Queen Ant is spawned.
	private static final byte DEAD = 1; // Queen Ant has been killed.
	// Location
	private static final int QUEEN_X = -6505;
	private static final int QUEEN_Y = 183040;
	private static final int QUEEN_Z = -3419;
	
	private QueenAnt()
	{
		addKillId(QUEEN_ANT);
		addSpawnId(QUEEN_ANT);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(QUEEN_ANT);
		if (GrandBossManager.getInstance().getStatus(QUEEN_ANT) == DEAD)
		{
			// Load the unlock date and time for queen ant from DB.
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0) // If queen ant is locked until a certain time, mark it so and start the unlock timer the unlock time has not yet expired.
			{
				startQuestTimer("queen_unlock", temp, null, null);
			}
			else // The time has already expired while the server was offline. Immediately spawn queen ant.
			{
				final GrandBoss queen = (GrandBoss) addSpawn(QUEEN_ANT, QUEEN_X, QUEEN_Y, QUEEN_Z, 0, false, 0);
				GrandBossManager.getInstance().setStatus(QUEEN_ANT, ALIVE);
				spawnBoss(queen);
			}
		}
		else
		{
			int locX = info.getInt("loc_x");
			int locY = info.getInt("loc_y");
			int locZ = info.getInt("loc_z");
			final int heading = info.getInt("heading");
			final double hp = info.getDouble("currentHP");
			final double mp = info.getDouble("currentMP");
			final GrandBoss queen = (GrandBoss) addSpawn(QUEEN_ANT, locX, locY, locZ, heading, false, 0);
			queen.setCurrentHpMp(hp, mp);
			spawnBoss(queen);
		}
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "queen_unlock":
			{
				final GrandBoss queen = (GrandBoss) addSpawn(QUEEN_ANT, QUEEN_X, QUEEN_Y, QUEEN_Z, 0, false, 0);
				GrandBossManager.getInstance().setStatus(QUEEN_ANT, ALIVE);
				spawnBoss(queen);
				break;
			}
			case "DISTANCE_CHECK":
			{
				if ((npc == null) || npc.isDead())
				{
					cancelQuestTimers("DISTANCE_CHECK");
				}
				else if (npc.calculateDistance2D(npc.getSpawn()) > 6000)
				{
					((Attackable) npc).clearAggroList();
					npc.teleToLocation(QUEEN_X, QUEEN_Y, QUEEN_Z);
					npc.setCurrentHp(npc.getMaxHp());
					npc.setCurrentMp(npc.getMaxMp());
				}
				break;
			}
		}
		return super.onEvent(event, npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		GrandBossManager.getInstance().setStatus(QUEEN_ANT, DEAD);
		
		// Calculate Min and Max respawn times randomly.
		final long baseIntervalMillis = Config.QUEEN_ANT_SPAWN_INTERVAL * 3600000;
		final long randomRangeMillis = Config.QUEEN_ANT_SPAWN_RANDOM * 3600000;
		final long respawnTime = baseIntervalMillis + getRandom(-randomRangeMillis, randomRangeMillis);
		startQuestTimer("queen_unlock", respawnTime, null, null);
		
		// Also save the respawn time so that the info is maintained past restarts.
		final StatSet info = GrandBossManager.getInstance().getStatSet(QUEEN_ANT);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatSet(QUEEN_ANT, info);
		
		// Stop distance check task.
		cancelQuestTimers("DISTANCE_CHECK");
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		cancelQuestTimer("DISTANCE_CHECK", npc, null);
		startQuestTimer("DISTANCE_CHECK", 5000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	private void spawnBoss(GrandBoss npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
	}
	
	public static void main(String[] args)
	{
		new QueenAnt();
	}
}
