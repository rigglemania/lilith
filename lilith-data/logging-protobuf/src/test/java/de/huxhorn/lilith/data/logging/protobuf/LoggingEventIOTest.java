package de.huxhorn.lilith.data.logging.protobuf;

import de.huxhorn.lilith.data.logging.LoggingEvent;
import de.huxhorn.lilith.data.logging.ThrowableInfo;
import de.huxhorn.lilith.data.logging.Message;
import de.huxhorn.lilith.data.logging.Marker;
import de.huxhorn.lilith.data.logging.ExtendedStackTraceElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

public class LoggingEventIOTest
{
	private final Logger logger = LoggerFactory.getLogger(LoggingEventIOTest.class);

	@Test
	public void minimal()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		check(event);
	}

	@Test
	public void applicationId()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		event.setApplicationIdentifier("App");
		check(event);
	}

	@Test
	public void threadName()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		event.setThreadName("Thread-Name");
		check(event);
	}

	@Test
	public void arguments()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		String[] arguments = new String[]{"arg1", "arg2"};
		event.setArguments(arguments);
		check(event);
	}

	@Test
	public void nullArguments()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		String[] arguments = new String[]{"arg1", null, "arg3"};
		event.setArguments(arguments);
		check(event);
	}

	@Test
	public void singleThrowable()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		ThrowableInfo ti = createThrowableInfo("the.exception.class.Name", "Huhu! Exception Message");
		event.setThrowable(ti);
		check(event);
	}

	@Test
	public void multiThrowable()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		ThrowableInfo ti = createThrowableInfo("the.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti2 = createThrowableInfo("another.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti3 = createThrowableInfo("yet.another.exception.class.Name", "Huhu! Exception Message");
		ti.setCause(ti2);
		ti2.setCause(ti3);
		event.setThrowable(ti);
		check(event);
	}

	@Test
	public void mdc()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Map<String, String> mdc = new HashMap<String, String>();
		mdc.put("key1", "value1");
		mdc.put("key2", "value2");
		mdc.put("key3", "value3");
		event.setMdc(mdc);
		check(event);
	}

	@Test
	public void ndc()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Message[] ndc=new Message[]{
			new Message("message"),
			new Message("messagePattern {}", new String[]{"foo"})
		};
		event.setNdc(ndc);
		check(event);
	}

	@Test
	public void singleMarker()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Marker marker = new Marker("marker");
		event.setMarker(marker);
		check(event);
	}

	@Test
	public void childMarker()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Marker marker = new Marker("marker");
		Marker marker2_1 = new Marker("marker2-1");
		Marker marker2_2 = new Marker("marker2-2");
		marker.add(marker2_1);
		marker.add(marker2_2);
		event.setMarker(marker);
		check(event);
	}

	@Test
	public void recursiveMarker()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		Marker marker = new Marker("marker");
		Marker marker2_1 = new Marker("marker2-1");
		Marker marker2_2 = new Marker("marker2-2");
		Marker marker3_1 = new Marker("marker3-1");
		marker.add(marker2_1);
		marker.add(marker2_2);
		marker2_2.add(marker3_1);
		marker3_1.add(marker2_1);
		event.setMarker(marker);
		check(event);
	}

	@Test
	public void callStack()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();
		event.setCallStack(createStackTraceElements());
		check(event);
	}

	@Test
	public void full()
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEvent event = createMinimalEvent();

		event.setThreadName("Thread-Name");

		String[] arguments = new String[]{"arg1", null, "arg3"};
		event.setArguments(arguments);

		ThrowableInfo ti = createThrowableInfo("the.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti2 = createThrowableInfo("another.exception.class.Name", "Huhu! Exception Message");
		ThrowableInfo ti3 = createThrowableInfo("yet.another.exception.class.Name", "Huhu! Exception Message");
		ti.setCause(ti2);
		ti2.setCause(ti3);
		event.setThrowable(ti);

		Map<String, String> mdc = new HashMap<String, String>();
		mdc.put("key1", "value1");
		mdc.put("key2", "value2");
		mdc.put("key3", "value3");
		event.setMdc(mdc);

		Message[] ndc=new Message[]{
			new Message("message"),
			new Message("messagePattern {}", new String[]{"foo"})
		};
		event.setNdc(ndc);

		Marker marker = new Marker("marker");
		Marker marker2_1 = new Marker("marker2-1");
		Marker marker2_2 = new Marker("marker2-2");
		Marker marker3_1 = new Marker("marker3-1");
		marker.add(marker2_1);
		marker.add(marker2_2);
		marker2_2.add(marker3_1);
		marker3_1.add(marker2_1);
		event.setMarker(marker);

		event.setCallStack(createStackTraceElements());
		check(event);
	}

	public LoggingEvent createMinimalEvent()
	{
		LoggingEvent event = new LoggingEvent();
		event.setLogger("Logger");
		event.setLevel(LoggingEvent.Level.INFO);
		event.setTimeStamp(new Date());
		event.setMessagePattern("EventMessage");
		return event;
	}

	public ThrowableInfo createThrowableInfo(String className, String message)
	{
		ThrowableInfo ti = new ThrowableInfo();
		ti.setName(className);
		ti.setMessage(message);
		ti.setStackTrace(createStackTraceElements());
		return ti;
	}

	public ExtendedStackTraceElement[] createStackTraceElements()
	{
		//noinspection ThrowableInstanceNeverThrown
		Throwable t = new Throwable();
		StackTraceElement[] original = t.getStackTrace();

		ExtendedStackTraceElement[] result = new ExtendedStackTraceElement[original.length];
		for(int i = 0; i < original.length; i++)
		{
			StackTraceElement current = original[i];
			result[i] = new ExtendedStackTraceElement(current);

			if(i == 0)
			{
				// codeLocation, version and exact
				result[i].setCodeLocation("CodeLocation");
				result[i].setVersion("Version");
				result[i].setExact(true);
			}
			else if(i == 1)
			{
				// codeLocation, version and exact
				result[i].setCodeLocation("CodeLocation");
				result[i].setVersion("Version");
				result[i].setExact(false);
			}
		}

		return result;
	}

	public void check(LoggingEvent event)
		throws UnsupportedEncodingException, XMLStreamException
	{
		if(logger.isDebugEnabled()) logger.debug("Processing LoggingEvent:\n{}", event);
		byte[] bytes;
		LoggingEvent readEvent;

		bytes = write(event, false);
		readEvent = read(bytes, false);
		if(logger.isDebugEnabled()) logger.debug("LoggingEvent read uncompressed.");
		if(logger.isInfoEnabled()) logger.info("Original marker: {}", toString(event.getMarker()));
		if(logger.isInfoEnabled()) logger.info("Read     marker: {}", toString(readEvent.getMarker()));
		assertEquals(event, readEvent);

		bytes = write(event, true);
		readEvent = read(bytes, true);
		if(logger.isDebugEnabled()) logger.debug("LoggingEvent read compressed.");
		if(logger.isInfoEnabled()) logger.info("Original marker: {}", toString(event.getMarker()));
		if(logger.isInfoEnabled()) logger.info("Read     marker: {}", toString(readEvent.getMarker()));
	}

	public byte[] write(LoggingEvent event, boolean compressing)
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEventSerializer ser = new LoggingEventSerializer(/* TODO: compressing */);
		return ser.serialize(event);
	}

	public LoggingEvent read(byte[] bytes, boolean compressing)
		throws XMLStreamException, UnsupportedEncodingException
	{
		LoggingEventDeserializer des = new LoggingEventDeserializer(/* TODO: compressing*/);
		return des.deserialize(bytes);
	}

	String toString(Marker marker)
	{
		if(marker == null)
		{
			return null;
		}
		StringBuilder result = new StringBuilder();
		Map<String, Marker> processedMarkers = new HashMap<String, Marker>();
		recursiveToString(result, processedMarkers, marker);
		return result.toString();
	}

	private void recursiveToString(StringBuilder result, Map<String, Marker> processedMarkers, Marker marker)
	{
		if(processedMarkers.containsKey(marker.getName()))
		{
			result.append("Marker[ref=").append(marker.getName());
		}
		else
		{
			processedMarkers.put(marker.getName(), marker);
			result.append("Marker[name=").append(marker.getName());
			if(marker.hasReferences())
			{
				result.append(", children={");
				Map<String, Marker> children = marker.getReferences();
				boolean first = true;
				for(Map.Entry<String, Marker> current : children.entrySet())
				{
					if(first)
					{
						first = false;
					}
					else
					{
						result.append(", ");
					}
					recursiveToString(result, processedMarkers, current.getValue());
				}
				result.append("}");
			}
			result.append("]");
		}
	}
}