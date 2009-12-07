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
package de.huxhorn.lilith.swing;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;
import java.util.Arrays;

import de.huxhorn.sulky.swing.KeyStrokes;
import de.huxhorn.sulky.conditions.Condition;
import de.huxhorn.sulky.conditions.Not;
import de.huxhorn.lilith.conditions.*;
import de.huxhorn.lilith.swing.preferences.SavedCondition;

public class FindPanel<T extends Serializable>
	extends JPanel
{
	private final Logger logger = LoggerFactory.getLogger(FindPanel.class);

	private EventWrapperViewPanel<T> eventWrapperViewPanel;
	private FindNextAction findNextAction;
	private FindPreviousAction findPrevAction;
	private CloseFindAction closeFindAction;

	private JButton findPrevButton;
	private JButton findNextButton;

	private JToggleButton findNotButton;
	private JComboBox findTypeCombo;
	private JComboBox findTextCombo;

	private static final String GROOVY_IDENTIFIER = "#groovy#";
	private static final String SAVED_CONDITION_IDENTIFIER = "#condition#";
	private static final Color ERROR_COLOR = new Color(0x990000);
	private static final Color NO_ERROR_COLOR = Color.BLACK;

	private static final String EVENT_CONTAINS_CONDITION = "event.contains";
	private static final String MESSAGE_CONTAINS_CONDITION = "message.contains";
	private static final String LOGGER_STARTS_WITH_CONDITION = "logger.startsWith";
	private static final String LOGGER_EQUALS_CONDITION = "logger.equals";
	private static final String LEVEL_CONDITION = "Level>=";
	private static final String CALL_LOCATION_CONDITION = "CallLocation";
	private static final String NAMED_CONDITION = "Named";
	private static final String[] DEFAULT_CONDITIONS = new String[]{
		EVENT_CONTAINS_CONDITION,
		MESSAGE_CONTAINS_CONDITION,
		LOGGER_STARTS_WITH_CONDITION,
		LOGGER_EQUALS_CONDITION,
		LEVEL_CONDITION,
		CALL_LOCATION_CONDITION,
		NAMED_CONDITION,
	};

	// TODO: Named condition combo values & condition
	// TODO: Level>= combo values
	// TODO: previousSearchStrings combo values
	// TODO: Focus traversal
	private MainFrame mainFrame;
	public static final String CONDITION_PROPERTY = "condition";
	private Condition condition;

	public FindPanel(EventWrapperViewPanel<T> eventWrapperViewPanel)
	{
		this.eventWrapperViewPanel = eventWrapperViewPanel;
		this.mainFrame=this.eventWrapperViewPanel.getMainFrame();
		initUi();
	}

	private void initUi()
	{
		closeFindAction = new CloseFindAction();
		JButton closeFindButton = new JButton(closeFindAction);
		closeFindButton.setMargin(new Insets(0, 0, 0, 0));
		GridBagConstraints gbc=new GridBagConstraints();
		setLayout(new GridBagLayout());
		gbc.insets = new Insets(0,0,0,0);
		gbc.anchor = GridBagConstraints.LINE_START; // like WEST
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(closeFindButton, gbc);
		//findPanel.addSeparator();

		gbc.gridx = 1;
		add(new JLabel(" Find: "), gbc);

		ActionListener findTypeModifiedListener = new FindTypeSelectionActionListener();
		findTypeCombo = new JComboBox();
		// not editable, so decorator will be strict
		AutoCompleteDecorator.decorate(this.findTypeCombo);

		findTypeCombo.addActionListener(findTypeModifiedListener);
		findNotButton = new JToggleButton("!");
		findNotButton.addActionListener(findTypeModifiedListener);
		findNotButton.setToolTipText("Not - inverts condition");
		findNotButton.setMargin(new Insets(0, 0, 0, 0));
		findTextCombo = new JComboBox();
		findTextCombo.setEditable(true); // so decorator won't be strict
		AutoCompleteDecorator.decorate(this.findTextCombo);

		gbc.gridx = 2;
		gbc.fill=GridBagConstraints.VERTICAL;
		add(findNotButton, gbc);

		gbc.gridx = 3;
		gbc.fill=GridBagConstraints.VERTICAL;
		add(findTypeCombo, gbc);

		gbc.gridx = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill=GridBagConstraints.BOTH;
		add(findTextCombo, gbc);

		findPrevAction = new FindPreviousAction();
		findPrevButton = new JButton(findPrevAction);
		findPrevButton.setMargin(new Insets(0, 0, 0, 0));

		gbc.gridx = 5;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill=GridBagConstraints.NONE;
		add(findPrevButton, gbc);

		findNextAction = new FindNextAction();
		findNextButton = new JButton(findNextAction);
		findNextButton.setMargin(new Insets(0, 0, 0, 0));

		gbc.gridx = 6;
		add(findNextButton, gbc);

		FindTextFieldListener findTextFieldListener = new FindTextFieldListener();
		JTextComponent findEditorComponent = getFindEditorComponent();
		if(findEditorComponent instanceof JTextField)
		{
			((JTextField)findEditorComponent).addActionListener(findTextFieldListener);
		}
		else
		{
			if(logger.isWarnEnabled()) logger.warn("findEditorComponent ({}) is not instanceof JTextField!", findEditorComponent.getClass().getName());
		}
		if(findEditorComponent != null)
		{
			findEditorComponent.getDocument().addDocumentListener(findTextFieldListener);
			findEditorComponent.setForeground(NO_ERROR_COLOR);
		}
		ReplaceFilterAction replaceFilterAction = new ReplaceFilterAction();

		KeyStrokes.registerCommand(this, findNextAction, "FIND_NEXT_ACTION");
		KeyStrokes.registerCommand(this, findPrevAction, "FIND_PREV_ACTION");
		KeyStrokes.registerCommand(this, closeFindAction, "CLOSE_FIND_ACTION");
		KeyStrokes.registerCommand(findTextCombo, replaceFilterAction, "REPLACE_FILTER_ACTION");
	}

	private void setCondition(Condition condition)
	{
		Object oldValue=getCondition();
		try
		{
			if(condition != null)
			{
				this.condition = condition.clone();
			}
			else
			{
				this.condition = null;
			}
		}
		catch (CloneNotSupportedException e)
		{
			this.condition = null;
			if(logger.isWarnEnabled()) logger.warn("Condition "+condition+" does not support cloning!", e);
		}
		Object newValue=getCondition();
		findPrevAction.setEnabled(this.condition != null);
		findNextAction.setEnabled(this.condition != null);
		firePropertyChange(CONDITION_PROPERTY, oldValue, newValue);
	}

	public Condition getCondition()
	{
		if(condition == null)
		{
			return null;
		}
		try
		{
			return condition.clone();
		}
		catch (CloneNotSupportedException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Condition "+condition+" does not support cloning!", e);
		}
		return null;
	}

	private void updateCondition()
	{
		setCondition(createCondition());
	}

	private Condition createCondition()
	{
		String text=null;
		JTextComponent findEditorComponent = getFindEditorComponent();
		if(findEditorComponent != null)
		{
			text=findEditorComponent.getText();
		}

		Condition condition;

		String errorMessage = null;
		if(text == null)
		{
			text = "";
		}
		if(text.startsWith(GROOVY_IDENTIFIER))
		{
			String scriptName = text.substring(GROOVY_IDENTIFIER.length());

			int idx = scriptName.indexOf('#');
			if(idx > -1)
			{
				if(idx + 1 < scriptName.length())
				{
					text = scriptName.substring(idx + 1);
				}
				else
				{
					text = "";
				}
				scriptName = scriptName.substring(0, idx);
			}
			else
			{
				text = "";
			}
			if(logger.isDebugEnabled())
			{
				logger.debug("GroovyCondition with scriptName '{}' and searchString '{}'", scriptName, text);
			}
			File resolvedScriptFile = mainFrame.resolveConditionScriptFile(scriptName);
			if(resolvedScriptFile != null)
			{
				// there is a file...
				condition = new GroovyCondition(resolvedScriptFile.getAbsolutePath(), text);
			}
			else
			{
				errorMessage = "Couldn't find groovy script '" + scriptName + "'.";
				condition = null;
			}
		}
		else if(text.startsWith(SAVED_CONDITION_IDENTIFIER))
		{
			String conditionName = text.substring(SAVED_CONDITION_IDENTIFIER.length());
			SavedCondition savedCondition = mainFrame.getApplicationPreferences().resolveSavedCondition(conditionName);
			if(savedCondition != null)
			{
				condition = savedCondition.getCondition();
			}
			else
			{
				errorMessage = "Couldn't find saved condition '" + conditionName + "'.";
				condition = null;
			}
		}
		else
		{
			// create condition matching the selected type
			String selectedType = (String) findTypeCombo.getSelectedItem();
			if(EVENT_CONTAINS_CONDITION.equals(selectedType))
			{
				condition = new EventContainsCondition(text);
			}
			else if(MESSAGE_CONTAINS_CONDITION.equals(selectedType))
			{
				condition = new MessageContainsCondition(text);
			}
			else if(LOGGER_STARTS_WITH_CONDITION.equals(selectedType))
			{
				condition = new LoggerStartsWithCondition(text);
			}
			else if(LOGGER_EQUALS_CONDITION.equals(selectedType))
			{
				condition = new LoggerEqualsCondition(text);
			}
			else if(LEVEL_CONDITION.equals(selectedType))
			{
				condition = new LevelCondition(text);
			}
			else if(CALL_LOCATION_CONDITION.equals(selectedType))
			{
				condition = new CallLocationCondition(text);
			}
			else
			{
				// we assume a groovy condition...
				File resolvedScriptFile = mainFrame.resolveConditionScriptFile(selectedType);
				if(resolvedScriptFile != null)
				{
					// there is a file...
					condition = new GroovyCondition(resolvedScriptFile.getAbsolutePath(), text);
				}
				else
				{
					errorMessage = "Couldn't find condition '"+selectedType+"'!";
					condition = null;
				}
			}
		}
		if(findEditorComponent != null)
		{
			if(errorMessage != null)
			{
				// problem with condition
				findEditorComponent.setForeground(ERROR_COLOR);
				findEditorComponent.setToolTipText(errorMessage);
			}
			else
			{
				findEditorComponent.setForeground(NO_ERROR_COLOR);
				findEditorComponent.setToolTipText(null);
			}
		}
		if(condition != null)
		{
			// wrap in Not if not is selected.
			if(findNotButton.isSelected())
			{
				condition = new Not(condition);
			}
		}
		return condition;
	}

	public void resetFind()
	{
		JTextComponent findEditorComponent = getFindEditorComponent();
		if(findEditorComponent != null)
		{
			findEditorComponent.setText("");
		}
	}

	public void updateUi()
	{
		initTypeCombo();
		// select correct type in combo
		Condition condition = eventWrapperViewPanel.getFilterCondition(); // TODO: check!
		boolean not = false;
		if(condition instanceof Not)
		{
			Not notCondition = (Not) condition;
			not = true;
			condition = notCondition.getCondition();
		}
		if(condition != null)
		{
			String conditionName = null;
			if(condition instanceof EventContainsCondition)
			{
				conditionName = EVENT_CONTAINS_CONDITION;
			}
			else if(condition instanceof MessageContainsCondition)
			{
				conditionName = MESSAGE_CONTAINS_CONDITION;
			}
			else if(condition instanceof LoggerStartsWithCondition)
			{
				conditionName = LOGGER_STARTS_WITH_CONDITION;
			}
			else if(condition instanceof LoggerEqualsCondition)
			{
				conditionName = LOGGER_EQUALS_CONDITION;
			}
			else if(condition instanceof GroovyCondition)
			{
				GroovyCondition groovyCondition = (GroovyCondition) condition;
				String scriptFileName = groovyCondition.getScriptFileName();
				if(scriptFileName != null)
				{
					File scriptFile = new File(scriptFileName);
					conditionName = scriptFile.getName();
				}
			}
			if(conditionName != null)
			{
				findTypeCombo.setSelectedItem(conditionName);
			}
		}
		findNotButton.setSelected(not);
	}

	private void initTypeCombo()
	{
		Vector<String> itemsVector = new Vector<String>();

		itemsVector.addAll(Arrays.asList(DEFAULT_CONDITIONS));

		String[] groovyConditions = mainFrame.getAllConditionScriptFiles();
		if(groovyConditions != null)
		{
			itemsVector.addAll(Arrays.asList(groovyConditions));
		}

		ComboBoxModel model = new DefaultComboBoxModel(itemsVector);
		findTypeCombo.setModel(model);
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if(logger.isDebugEnabled()) logger.debug("Visible: {}", visible);
	}

	private JTextComponent getFindEditorComponent()
	{
		Component findComponent = findTextCombo.getEditor().getEditorComponent();
		if(findComponent instanceof JTextComponent)
		{
			return (JTextComponent) findComponent;
		}
		if(logger.isWarnEnabled()) logger.warn("findComponent ({}) is not instanceof JTextComponent!", findComponent.getClass().getName());
		return null;
	}

	public void requestComboFocus()
	{
		findTextCombo.requestFocusInWindow();
		findTextCombo.getEditor().selectAll();		
	}

	public void enableFindComponents(boolean enabled, Condition condition)
	{
		// TODO: check if this can be changed.
		closeFindAction.setEnabled(enabled);
		findTextCombo.setEnabled(enabled);
		if(condition != null)
		{
			findPrevAction.setEnabled(enabled);
			findNextAction.setEnabled(enabled);
		}
		else
		{
			findPrevAction.setEnabled(false);
			findNextAction.setEnabled(false);
		}
	}

	private class FindTextFieldListener
		implements ActionListener, DocumentListener
	{

		public void actionPerformed(ActionEvent e)
		{
			updateCondition();
			if(logger.isDebugEnabled()) logger.debug("modifiers: " + e.getModifiers());
			JTextComponent findEditorComponent = getFindEditorComponent();
			if(findEditorComponent != null)
			{
				findEditorComponent.selectAll();
			}
			eventWrapperViewPanel.createFilteredView();
		}

		public void insertUpdate(DocumentEvent e)
		{
			updateCondition();
		}

		public void removeUpdate(DocumentEvent e)
		{
			updateCondition();
		}

		public void changedUpdate(DocumentEvent e)
		{
			updateCondition();
		}
	}

	/**
	 * This action has different enabled logic than the one in ViewActions
	 */
	private class FindNextAction
		extends AbstractAction
	{
		private static final long serialVersionUID = -6469494975854597398L;

		public FindNextAction()
		{
			super();
			Icon icon;
			{
				URL url = EventWrapperViewPanel.class.getResource("/tango/16x16/actions/go-down.png");
				if(url != null)
				{
					icon = new ImageIcon(url);
				}
				else
				{
					icon = null;
				}
			}
			putValue(Action.SMALL_ICON, icon);
			putValue(Action.SHORT_DESCRIPTION, "Find next.");
			KeyStroke accelerator = KeyStrokes.resolveAcceleratorKeyStroke(KeyStrokes.COMMAND_ALIAS + " shift G");
			if(logger.isDebugEnabled()) logger.debug("accelerator: {}", accelerator);
			putValue(Action.ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e)
		{
			// TODO: simply findNext()
			eventWrapperViewPanel.findNext(eventWrapperViewPanel.getSelectedRow(), eventWrapperViewPanel.getFilterCondition());
		}
	}

	/**
	 * This action has different enabled logic than the one in ViewActions
	 */
	private class FindPreviousAction
		extends AbstractAction
	{
		private static final long serialVersionUID = -8192948220602398223L;

		public FindPreviousAction()
		{
			super();
			Icon icon;
			{
				URL url = EventWrapperViewPanel.class.getResource("/tango/16x16/actions/go-up.png");
				if(url != null)
				{
					icon = new ImageIcon(url);
				}
				else
				{
					icon = null;
				}
			}
			putValue(Action.SMALL_ICON, icon);
			putValue(Action.SHORT_DESCRIPTION, "Find previous.");
			KeyStroke accelerator = KeyStrokes.resolveAcceleratorKeyStroke(KeyStrokes.COMMAND_ALIAS + " G");
			if(logger.isDebugEnabled()) logger.debug("accelerator: {}", accelerator);
			putValue(Action.ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e)
		{
			// TODO: simply findPrevious()
			eventWrapperViewPanel.findPrevious(eventWrapperViewPanel.getSelectedRow(), eventWrapperViewPanel.getFilterCondition());
		}
	}

	private class CloseFindAction
		extends AbstractAction
	{
		private static final long serialVersionUID = -7757686292973276423L;

		public CloseFindAction()
		{
			super();
			Icon icon;
			{
				URL url = EventWrapperViewPanel.class.getResource("/tango/16x16/emblems/emblem-unreadable.png");
				if(url != null)
				{
					icon = new ImageIcon(url);
				}
				else
				{
					icon = null;
				}
			}
			putValue(Action.SMALL_ICON, icon);
			putValue(Action.SHORT_DESCRIPTION, "Close");
			KeyStroke accelerator = KeyStrokes.resolveAcceleratorKeyStroke("ESCAPE");
			if(logger.isDebugEnabled()) logger.debug("accelerator: {}", accelerator);
			putValue(Action.ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e)
		{
			ViewContainer container = eventWrapperViewPanel.resolveContainer();
			if(container != null)
			{
				ProgressGlassPane progressPanel = container.getProgressPanel();
				progressPanel.getFindCancelAction().cancelSearch();
				setVisible(false);
			}
		}
	}

	private class ReplaceFilterAction
		extends AbstractAction
	{
		private static final long serialVersionUID = 3876315232050114189L;

		public ReplaceFilterAction()
		{
			super();
			putValue(Action.SHORT_DESCRIPTION, "Replace filter.");
			KeyStroke accelerator = KeyStrokes.resolveAcceleratorKeyStroke("shift ENTER");
			if(logger.isDebugEnabled()) logger.debug("accelerator: {}", accelerator);
			putValue(Action.ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e)
		{
			if(logger.isInfoEnabled()) logger.info("Replace filter.");
			ViewContainer<T> container = eventWrapperViewPanel.resolveContainer();
			if(container != null)
			{
				container.replaceFilteredView(eventWrapperViewPanel); // TODO: check!
			}
		}
	}

	private class FindTypeSelectionActionListener
		implements ActionListener
	{

		public void actionPerformed(ActionEvent e)
		{
			updateCondition();
		}
	}
}
