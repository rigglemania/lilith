import de.huxhorn.lilith.data.logging.LoggingEvent

/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2009 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Returns true if the MDC of the logging event contains a
 * "username" entry that equals the searchString.
 */
def event = input?.event;

if(event instanceof LoggingEvent)
{
	def mdc = event.mdc;
	if(mdc)
	{
		return (mdc['username'] == searchString);
	}
}
return false;
