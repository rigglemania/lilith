/*
 * Lilith - a log event viewer.
 * Copyright (C) 2007-2014 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2014 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.lilith.log4j.xml;

import de.huxhorn.lilith.data.eventsource.EventIdentifier;
import de.huxhorn.lilith.data.eventsource.EventWrapper;
import de.huxhorn.lilith.data.eventsource.SourceIdentifier;
import de.huxhorn.lilith.data.logging.LoggingEvent;
import de.huxhorn.sulky.buffers.AppendOperation;
import de.huxhorn.sulky.tasks.AbstractProgressingCallable;

import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Log4jImportCallable
	extends AbstractProgressingCallable<Long>
{
	private final Logger logger = LoggerFactory.getLogger(Log4jImportCallable.class);

	public static final String CLOSING_LOG4J_EVENT_TAG = "</log4j:event>";
	public static final String LOG4J_NAMESPACE = "xmlns:log4j=\"http://jakarta.apache.org/log4j/\"";
	public static final String OPENING_LOG4J_EVENT_TAG_EXCL_NS = "<log4j:event ";
	public static final String OPENING_LOG4J_EVENT_TAG_INCL_NS = OPENING_LOG4J_EVENT_TAG_EXCL_NS + LOG4J_NAMESPACE + " ";

	private File inputFile;
	private AppendOperation<EventWrapper<LoggingEvent>> buffer;
	private LoggingEventReader instance;
	private long result;

	public Log4jImportCallable(File inputFile, AppendOperation<EventWrapper<LoggingEvent>> buffer)
	{
		this.buffer = buffer;
		this.inputFile = inputFile;
		instance = new LoggingEventReader();
	}

	public AppendOperation<EventWrapper<LoggingEvent>> getBuffer()
	{
		return buffer;
	}

	public File getInputFile()
	{
		return inputFile;
	}

	public Long call()
		throws Exception
	{
		if(!inputFile.isFile())
		{
			throw new IllegalArgumentException("'" + inputFile.getAbsolutePath() + "' is not a file!");
		}
		if(!inputFile.canRead())
		{
			throw new IllegalArgumentException("'" + inputFile.getAbsolutePath() + "' is not a readable!");
		}
		long fileSize = inputFile.length();
		setNumberOfSteps(fileSize);
		FileInputStream fis = new FileInputStream(inputFile);
		CountingInputStream cis = new CountingInputStream(fis);

		String fileName=inputFile.getName().toLowerCase();
		BufferedReader br;
		if(fileName.endsWith(".gz"))
		{
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(cis), "UTF-8"));
		}
		else
		{
			br = new BufferedReader(new InputStreamReader(cis, "UTF-8"));
		}

		StringBuilder builder = new StringBuilder();

		result = 0;
		for(; ;)
		{
			String line = br.readLine();
			setCurrentStep(cis.getByteCount());
			if(line == null)
			{
				evaluate(builder.toString());
				break;
			}
			for(; ;)
			{
				int closeIndex = line.indexOf(CLOSING_LOG4J_EVENT_TAG);
				if(closeIndex >= 0)
				{
					int endIndex = closeIndex + CLOSING_LOG4J_EVENT_TAG.length();
					builder.append(line.subSequence(0, endIndex));
					evaluate(builder.toString());
					builder.setLength(0);
					line = line.substring(endIndex);
				}
				else
				{
					builder.append(line);
					builder.append("\n");
					break;
				}
			}
		}
		return result;
	}

	private void evaluate(String eventStr)
	{
		eventStr = prepare(eventStr);

		try
		{
			LoggingEvent event = readEvent(eventStr);
			if(event != null)
			{
				result++;
				EventWrapper<LoggingEvent> wrapper = new EventWrapper<LoggingEvent>();
				wrapper.setEvent(event);
				SourceIdentifier sourceIdentifier = new SourceIdentifier(inputFile.getAbsolutePath());
				EventIdentifier eventId = new EventIdentifier(sourceIdentifier, result);
				wrapper.setEventIdentifier(eventId);
				buffer.add(wrapper);
			}
		}
		catch(XMLStreamException e)
		{
			// ignore
		}
		catch(UnsupportedEncodingException e)
		{
			// ignore
		}
	}

	private String prepare(String eventStr)
	{
		if(!eventStr.contains(LOG4J_NAMESPACE))
		{
			eventStr = eventStr.replace(OPENING_LOG4J_EVENT_TAG_EXCL_NS, OPENING_LOG4J_EVENT_TAG_INCL_NS);
			if(logger.isDebugEnabled()) logger.debug("After change: {}", eventStr);
		}

		return eventStr;
	}

	private LoggingEvent readEvent(String eventStr)
		throws XMLStreamException, UnsupportedEncodingException
	{
		byte[] bytes = eventStr.getBytes("UTF-8");
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		XMLStreamReader reader = inputFactory.createXMLStreamReader(new InputStreamReader(in, "utf-8"));
		return instance.read(reader);
	}
}
