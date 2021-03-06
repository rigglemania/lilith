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
package de.huxhorn.lilith.swing.preferences;

import de.huxhorn.lilith.swing.ApplicationPreferences;

import java.awt.*;

import javax.swing.*;

public class WindowsPanel
	extends JPanel
{
	private ApplicationPreferences applicationPreferences;

	// Windows
	private JCheckBox showingToolbarCheckbox;
	private JCheckBox showingStatusbarCheckbox;
	private JCheckBox internalFramesCheckbox;
	private JCheckBox maximizeInternalFramesCheckbox;
	private JCheckBox autoOpenCheckbox;
	private JCheckBox autoFocusCheckbox;
	private JCheckBox autoCloseCheckbox;
	private JCheckBox showIdentifierCheckbox;

	public WindowsPanel(PreferencesDialog preferencesDialog)
	{
		applicationPreferences = preferencesDialog.getApplicationPreferences();
		createUI();
	}

	private void createUI()
	{
		showingToolbarCheckbox = new JCheckBox("Show toolbar.");
		showingStatusbarCheckbox = new JCheckBox("Show statusbar.");
		internalFramesCheckbox = new JCheckBox("Use internal frames.");
		maximizeInternalFramesCheckbox = new JCheckBox("Maximize internal frames.");
		showIdentifierCheckbox = new JCheckBox("Show identifier for named sources.");
		autoOpenCheckbox = new JCheckBox("Automatically open new views on connection.");
		autoCloseCheckbox = new JCheckBox("Automatically close inactive views on disconnection.");
		autoFocusCheckbox = new JCheckBox("Automatically focus window of new view.");

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.weightx = 1;
		gbc.gridx = 0;

		gbc.gridy = 0;
		add(showingToolbarCheckbox, gbc);

		gbc.gridy++;
		add(showingStatusbarCheckbox, gbc);

		gbc.gridy++;
		add(internalFramesCheckbox, gbc);

		gbc.gridy++;
		add(maximizeInternalFramesCheckbox, gbc);

		gbc.gridy++;
		add(autoOpenCheckbox, gbc);

		gbc.gridy++;
		add(autoFocusCheckbox, gbc);

		gbc.gridy++;
		add(autoCloseCheckbox, gbc);

		gbc.weighty = 1;
		gbc.gridy++;
		add(showIdentifierCheckbox, gbc);
	}

	public void initUI()
	{
		showingToolbarCheckbox.setSelected(applicationPreferences.isShowingToolbar());
		showingStatusbarCheckbox.setSelected(applicationPreferences.isShowingStatusbar());
		internalFramesCheckbox.setSelected(applicationPreferences.isUsingInternalFrames());
		maximizeInternalFramesCheckbox.setSelected(applicationPreferences.isMaximizingInternalFrames());
		autoOpenCheckbox.setSelected(applicationPreferences.isAutoOpening());
		autoFocusCheckbox.setSelected(applicationPreferences.isAutoFocusingWindow());
		autoCloseCheckbox.setSelected(applicationPreferences.isAutoClosing());
		showIdentifierCheckbox.setSelected(applicationPreferences.isShowingIdentifier());
	}

	public void saveSettings()
	{
		applicationPreferences.setShowingToolbar(showingToolbarCheckbox.isSelected());
		applicationPreferences.setShowingStatusbar(showingStatusbarCheckbox.isSelected());
		applicationPreferences.setUsingInternalFrames(internalFramesCheckbox.isSelected());
		applicationPreferences.setMaximizingInternalFrames(maximizeInternalFramesCheckbox.isSelected());
		applicationPreferences.setAutoOpening(autoOpenCheckbox.isSelected());
		applicationPreferences.setAutoFocusingWindow(autoFocusCheckbox.isSelected());
		applicationPreferences.setAutoClosing(autoCloseCheckbox.isSelected());
		applicationPreferences.setShowingIdentifier(showIdentifierCheckbox.isSelected());
	}
}
