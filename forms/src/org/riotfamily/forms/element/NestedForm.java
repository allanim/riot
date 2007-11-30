/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * The Original Code is Riot.
 * 
 * The Initial Developer of the Original Code is
 * Neteye GmbH.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 * 
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.forms.element;

import java.util.List;

import org.riotfamily.forms.AbstractEditorBase;
import org.riotfamily.forms.BeanEditor;
import org.riotfamily.forms.Container;
import org.riotfamily.forms.ContainerElement;
import org.riotfamily.forms.Editor;
import org.riotfamily.forms.EditorBinder;
import org.riotfamily.forms.Element;
import org.riotfamily.forms.MessageUtils;
import org.riotfamily.forms.TemplateUtils;
import org.riotfamily.forms.event.Button;
import org.riotfamily.forms.event.JavaScriptEvent;
import org.riotfamily.forms.request.FormRequest;
import org.riotfamily.forms.support.MapOrBeanWrapper;
import org.springframework.util.Assert;

/**
 * Element to edit nested beans.
 */
public class NestedForm extends TemplateElement implements 
		ContainerElement, Editor, BeanEditor {
		
	private EditorBinder editorBinder;

	private Container elements = new Container();
	
	private boolean present;
	
	private boolean indent = true;
	
	private String buttonLabelKeySet = "form.nestedForm.set";
	
	private String buttonLabelKeyRemove = "form.nestedForm.remove";
	
	public NestedForm() {
		super("form");
		addComponent("elements", elements);
		addComponent("toggleButton", new ToggleButton());
	}
	
	public void setIndent(boolean indent) {
		this.indent = indent;
		setTemplate(TemplateUtils.getTemplatePath(NestedForm.class, 
				indent ? null : "_noindent"));
	}	

	protected void setEditorBinder(EditorBinder editorBinder) {
		this.editorBinder = editorBinder;
	}
	
	public void setBeanClass(Class beanClass) {
		EditorBinder editorBinder = new EditorBinder(new MapOrBeanWrapper(beanClass));
		setEditorBinder(editorBinder);
	}
	
	public Class getBeanClass() {
		return editorBinder != null ? editorBinder.getBeanClass() : null; 
	}
	
	/**
	 * Invoked by {@link AbstractEditorBase#setEditorBinding} when the nested 
	 * form is bound to a property.
	 */
	protected void afterBindingSet() {
		if (editorBinder == null) {
			setBeanClass(getEditorBinding().getPropertyType());
		}
		editorBinder.setParent(getEditorBinding());
	}
	
	public Editor getEditor(String property) {
		return editorBinder.getEditor(property);
	}
	
	protected void afterFormContextSet() {		
		editorBinder.registerPropertyEditors(
				getFormContext().getPropertyEditorRegistrars());
	}
	
	public String getLabel() {
		return indent ? super.getLabel() : null;
	}
	
	public boolean isPresent() {
		return present;
	}
	
	protected void toggle() {
		present = !present;
		if (getFormListener() != null) {
			getFormListener().elementChanged(this);			
		}
	}
	
	public void processRequest(FormRequest request) {
		if (present || isRequired()) {
			super.processRequest(request);
		}
	}
	
	/**
	 * Sets the given value as backingObject on the internal EditorBinder and
	 * initializes the bound editors.
	 * 
	 * @see org.riotfamily.forms.Editor#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		this.present = isRequired() || value != null;		
		editorBinder.setBackingObject(value);
		editorBinder.initEditors();
	}

	/**
	 * Invokes the EditorBinder to populate the backingObject and returns the
	 * populated instance.
	 * 
	 * @see org.riotfamily.forms.Editor#getValue()
	 */
	public Object getValue() {
		if (present || isRequired()) {			
			return editorBinder.populateBackingObject();
		}
		return null;
	}

	public void bind(Editor editor, String property) {
		Assert.notNull(editorBinder, "The NestedForm must either be bound " +
				"to a property or setBeanClass() must be invoked before " +
				"nested editors can be added.");
		
		editorBinder.bind(editor, property);
	}

	public void addElement(Element element) {
		elements.addElement(element);
	}
	
	public void addElement(Editor element, String property) {
		addElement(element);
		bind(element, property);
	}
	
	public void removeElement(Element element) {
		elements.removeElement(element);
	}
	
	public List getElements() {
		return elements.getElements();
	}

	public String getProperty() {
		if (getEditorBinding() == null) {
			return null;
		}
		return getEditorBinding().getProperty();
	}

	private class ToggleButton extends Button {
		
		private ToggleButton() {
			setCssClass("button button-toggle");
			setTabIndex(2);
		}
		
		public String getLabel() {
			String key = present ? buttonLabelKeyRemove : buttonLabelKeySet;
			String label = MessageUtils.getMessage(this, key);
			if (label == null) {
				label = present ? "Remove" : "Set";
			}
			return label;
		}

		protected void onClick() {
			toggle();
		}
		
		public int getEventTypes() {
			return JavaScriptEvent.ON_CLICK;
		}
	}
}