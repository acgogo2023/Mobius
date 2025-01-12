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
package org.l2jmobius.gameserver.network.serverpackets.captcha;

import org.l2jmobius.commons.network.WritableBuffer;
import org.l2jmobius.gameserver.model.captcha.Captcha;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;
import org.l2jmobius.gameserver.network.serverpackets.ServerPacket;

public class ReceiveBotCaptchaImage extends ServerPacket
{
	private final Captcha _captcha;
	private final int _time;
	
	public ReceiveBotCaptchaImage(Captcha captcha, int time)
	{
		_captcha = captcha;
		_time = time;
	}
	
	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.RECEIVE_VIP_BOT_CAPTCHA_IMAGE.writeId(this, buffer);
		buffer.writeLong(_captcha.getId());
		buffer.writeByte(2); // unk
		buffer.writeInt(_time);
		buffer.writeBytes(_captcha.getData());
	}
}
