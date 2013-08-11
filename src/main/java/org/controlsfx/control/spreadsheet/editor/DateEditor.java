/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.controlsfx.control.spreadsheet.editor;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.controlsfx.control.spreadsheet.control.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DateCell;


/**
 *
 * Specialization of the {@link Editor} Class.
 * It displays a {@link DatePicker}.
 */
public class DateEditor extends Editor {

	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private final DatePicker datePicker;
	private EventHandler<KeyEvent> eh;

	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	public DateEditor() {
		super();
		datePicker = new DatePicker();
	}

	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	@Override
	public void startEdit() {
		super.startEdit();
		
		attachEnterEscapeEventHandler();

		// If the GridCell is deselected, we commit.
		// Sometimes, when you you touch the scrollBar when editing, this is called way
		// too late and the GridCell is null, so we need to be careful.
		il = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {

				if (spreadsheetCell != null && spreadsheetCell.isEditing()) {
					commitEdit();
					spreadsheetCell.commitEdit(cell);
				}
				end();
			}
		};

		spreadsheetCell.selectedProperty().addListener(il);

		spreadsheetCell.setGraphic(datePicker);

		final Runnable r = new Runnable() {
			@Override
			public void run() {
				datePicker.requestFocus();
			}
		};
		Platform.runLater(r);
	}
	
	/***************************************************************************
     *                                                                         *
     * Protected Methods                                                       *
     *                                                                         *
     **************************************************************************/
	@Override
	protected void begin(DataCell<?> cell, SpreadsheetCell bc) {
		this.cell = cell;
		this.spreadsheetCell = bc;
		final DateCell dc = (DateCell) cell;
		datePicker.setValue(dc.getCellValue());
	}

	@Override
	protected void end() {
		super.end();
		
		if(spreadsheetCell != null) {
			spreadsheetCell.selectedProperty().removeListener(il);
		}
		
		if(datePicker.isShowing()){
			datePicker.hide();
		}
		
		datePicker.removeEventFilter(KeyEvent.KEY_PRESSED, eh);
		this.cell = null;
		this.spreadsheetCell = null;
		il = null;
	}

	@Override
	protected DataCell<?> commitEdit() {
		final DateCell temp = (DateCell) this.cell;

		temp.setCellValue(datePicker.getValue());
		return cell;
	}

	@Override
	protected void cancelEdit() {
		end();
	}

	@Override
	protected Control getControl() {
		return datePicker;
	}

	@Override
	protected void attachEnterEscapeEventHandler() {
		/**
		 * We need to add an EventFilter because otherwise the DatePicker
		 * will block "escape" and "enter".
		 * But when "enter" is hit, we need to runLater the commit because
		 * the value has not yet hit the DatePicker itself.
		 */
		eh = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					final Runnable r = new Runnable() {
						@Override
						public void run() {
							commitEdit();
							spreadsheetCell.commitEdit(cell);
							end();
						}
					};
					Platform.runLater(r);
				} else if (t.getCode() == KeyCode.ESCAPE) {
					spreadsheetCell.cancelEdit();
					cancelEdit();
				}
			}
		};

		datePicker.addEventFilter(KeyEvent.KEY_PRESSED,eh);
	}
}