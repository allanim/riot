package org.riotfamily.riot.list.command.core;

import org.riotfamily.riot.dao.CutAndPasteEnabledDao;
import org.riotfamily.riot.list.command.CommandContext;
import org.riotfamily.riot.list.command.CommandResult;
import org.riotfamily.riot.list.command.result.SetRowStyleResult;
import org.riotfamily.riot.list.command.result.ShowListResult;
import org.riotfamily.riot.list.command.support.AbstractCommand;
import org.riotfamily.riot.list.command.support.Clipboard;
import org.riotfamily.riot.list.ui.render.RenderContext;

public class CutCommand extends AbstractCommand {

	private static final String CUT_ROW_STYLE = "cut";
	
	public boolean isEnabled(RenderContext context) {
		if (context.getDao() instanceof CutAndPasteEnabledDao) {
			Clipboard cb = Clipboard.get(context);
			if (cb.isCut(context)) {
				context.addRowStyle(CUT_ROW_STYLE);
			}
			return true;	
		}
		return false;
	}
	
	public CommandResult execute(CommandContext context) {
		Clipboard cb = Clipboard.get(context);
		boolean empty = cb.isEmpty();
		cb.cut(context);
		if (empty) {
			return new SetRowStyleResult(context.getObjectId(), CUT_ROW_STYLE);
		}
		else {
			return new ShowListResult(context);	
		}
	}
}
