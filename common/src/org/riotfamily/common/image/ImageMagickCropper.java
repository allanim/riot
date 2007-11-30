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
package org.riotfamily.common.image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.riotfamily.common.io.CommandUtils;
import org.springframework.util.StringUtils;

/**
 * @author Felix Gnass [fgnass at neteye dot de]
 *
 */
public class ImageMagickCropper implements ImageCropper {

	private String convertCommand = "convert";
	
	private boolean repage = true;

	public void setConvertCommand(String convertCommand) {
		this.convertCommand = convertCommand;
	}
	
	/**
	 * Sets whether the "+repage" operation should be used. If set to 
	 * <code>false</code>, "-page +0+0" is used instead (for IM 5 and earlier).
	 * Default is <code>true</code> 
	 */
	public void setRepage(boolean repage) {
		this.repage = repage;
	}

	public void cropImage(File source, File dest, int width, int height,
			int x, int y, int scaledWidth) throws IOException {
		
		ArrayList cmd = new ArrayList();
		cmd.add(convertCommand);
		cmd.add(source.getAbsolutePath());
		cmd.add("-resize");
		cmd.add(scaledWidth + "x>");
		cmd.add("-crop");
		cmd.add(width + "x" + height + "+" + x + "+" + y);
		if (repage) {
			cmd.add("+repage");
		}
		else {
			cmd.add("-page");
			cmd.add("+0+0");
		}
		cmd.add("-quality");
		cmd.add("100");
		
		cmd.add(dest.getAbsolutePath());
		CommandUtils.exec(StringUtils.toStringArray(cmd));
	}
	
}