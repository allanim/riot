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
package org.riotfamily.common.io;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

/**
 * FilterReader that replaces tokens with values form a Properties instance.
 */
public class PropertyFilterReader extends AbstractTokenFilterReader {

	private Map properties;
	
	public PropertyFilterReader(Reader in) {
		super(in);
	}
	
	public PropertyFilterReader(Reader in, Map properties) {
		super(in);
		this.properties = properties;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String getReplacement(String key) {
		return (String) properties.get(key);
	}
}
