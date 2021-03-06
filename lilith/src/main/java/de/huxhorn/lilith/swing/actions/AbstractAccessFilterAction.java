/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2013 Joern Huxhorn
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
package de.huxhorn.lilith.swing.actions;

import de.huxhorn.lilith.data.eventsource.EventWrapper;
import de.huxhorn.lilith.data.access.AccessEvent;

import javax.swing.*;
import java.io.Serializable;

public abstract class AbstractAccessFilterAction
	extends AbstractFilterAction
{
	private static final long serialVersionUID = -4293055398325177424L;

	protected AccessEvent accessEvent;

	protected AbstractAccessFilterAction()
	{
	}

	protected AbstractAccessFilterAction(String name)
	{
		super(name);
	}

	protected AbstractAccessFilterAction(String name, Icon icon)
	{
		super(name, icon);
	}

	@Override
	public final void setEventWrapper(EventWrapper eventWrapper)
	{
		setAccessEvent(resolveAccessEvent(eventWrapper));
	}

	public final void setAccessEvent(AccessEvent accessEvent)
	{
		this.accessEvent = accessEvent;
		updateState();
	}

	public static AccessEvent resolveAccessEvent(EventWrapper eventWrapper)
	{
		if(eventWrapper == null)
		{
			return null;
		}
		Serializable event = eventWrapper.getEvent();
		if(event == null)
		{
			return null;
		}
		if(event instanceof AccessEvent)
		{
			return (AccessEvent) event;
		}
		return null;
	}
}
