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
package org.l2jmobius.gameserver.model.quest.newquestdata;

/**
 * @author Magik
 */
public class NewQuestLocation
{
	private final int _startLocationId;
	private final int _endLocationId;
	private final int _questLocationId;
	
	public NewQuestLocation(int startLocationId, int endLocationId, int questLocationId)
	{
		_startLocationId = startLocationId;
		_endLocationId = endLocationId;
		_questLocationId = questLocationId;
	}
	
	public int getStartLocationId()
	{
		return _startLocationId;
	}
	
	public int getEndLocationId()
	{
		return _endLocationId;
	}
	
	public int getQuestLocationId()
	{
		return _questLocationId;
	}
}
