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
package de.huxhorn.lilith.conditions;

import de.huxhorn.lilith.data.access.AccessEvent;
import de.huxhorn.lilith.data.eventsource.EventWrapper;

public class HttpRequestUriCondition
	implements LilithCondition, SearchStringCondition
{
	private static final long serialVersionUID = -110122328082728065L;

	public static final String DESCRIPTION = "HttpRequestURI==";

	private String searchString;

	public HttpRequestUriCondition()
	{
		this(null);
	}

	public HttpRequestUriCondition(String searchString)
	{
		this.searchString=searchString;
	}

	public void setSearchString(String searchString)
	{
		this.searchString = searchString;
	}

	public String getSearchString()
	{
		return searchString;
	}

	public String getDescription()
	{
		return DESCRIPTION;
	}

	public boolean isTrue(Object value)
	{
		if(searchString == null)
		{
			return false;
		}
		if(searchString.length() == 0)
		{
			return true;
		}
		if(value instanceof EventWrapper)
		{
			EventWrapper wrapper = (EventWrapper) value;
			Object eventObj = wrapper.getEvent();
			if(eventObj instanceof AccessEvent)
			{
				AccessEvent event = (AccessEvent) eventObj;

				return searchString.equals(event.getRequestURI());
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HttpRequestUriCondition that = (HttpRequestUriCondition) o;

		if (searchString != null ? !searchString.equals(that.searchString) : that.searchString != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return searchString != null ? searchString.hashCode() : 0;
	}

	public HttpRequestUriCondition clone()
		throws CloneNotSupportedException
	{
		return (HttpRequestUriCondition) super.clone();
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append(getDescription());
		result.append(searchString);
		return result.toString();
	}
}
