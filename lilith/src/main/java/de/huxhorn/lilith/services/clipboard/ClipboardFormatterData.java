/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2011 Joern Huxhorn
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
package de.huxhorn.lilith.services.clipboard;

public final class ClipboardFormatterData
{
	private String name;
	private String description;
	private String accelerator;

	public ClipboardFormatterData()
	{
	}

	public ClipboardFormatterData(ClipboardFormatter clipboardFormatter)
	{
		if(clipboardFormatter == null)
		{
			throw new IllegalArgumentException("clipboardFormatter must not be null!");
		}
		this.name = clipboardFormatter.getName();
		this.description = clipboardFormatter.getDescription();
		this.accelerator = clipboardFormatter.getAccelerator();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getAccelerator()
	{
		return accelerator;
	}

	public void setAccelerator(String accelerator)
	{
		this.accelerator = accelerator;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		ClipboardFormatterData that = (ClipboardFormatterData) o;

		if(accelerator != null ? !accelerator.equals(that.accelerator) : that.accelerator != null) return false;
		if(description != null ? !description.equals(that.description) : that.description != null) return false;
		if(name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (accelerator != null ? accelerator.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "ClipboardFormatterData{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", accelerator='" + accelerator + '\'' +
			'}';
	}
}
