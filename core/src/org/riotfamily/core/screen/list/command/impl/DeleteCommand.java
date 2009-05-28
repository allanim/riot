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
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 *
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.core.screen.list.command.impl;

import org.riotfamily.core.screen.list.command.CommandContext;
import org.riotfamily.core.screen.list.command.Selection;
import org.riotfamily.core.screen.list.command.impl.dialog.YesNoCommand;
import org.riotfamily.forms.Form;
import org.riotfamily.forms.element.StaticText;

public class DeleteCommand extends YesNoCommand {

	public boolean isEnabled(CommandContext context, Selection selection) {
		return selection.size() > 0;
	}

	@Override
	protected void initForm(Form form, CommandContext context, Selection selection) {
		String codes[] = {
				"confirm.delete." + context.getScreen().getId(),
				"confirm.delete." + context.getScreen().getDao().getEntityClass(),
				"confirm.delete"
		};
		String label = null;
		if (selection.size() == 1) {
			label = context.getScreen().getItemLabel(selection.getSingleItem().getObject());
		}
		Object[] args = { 
				selection.size(),
				label
		};
		String question = context.getMessageResolver().getMessage(codes, args, 
				"Do you really want to delete the selected element(s)?");

		form.addElement(new StaticText(question));
	}
}
