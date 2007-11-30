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
package org.riotfamily.cachius;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Provides static methods to tag cache items.
 */
public class TaggingContext {

	private static final String REQUEST_ATTRIBUTE = TaggingContext.class.getName();

	private Log log = LogFactory.getLog(TaggingContext.class);

	private TaggingContext parent;

	private HashSet tags;

	/**
	 * Private constructor that creates a nested context.
	 */
	private TaggingContext(TaggingContext parent) {
		this.parent = parent;
	}

	/**
	 * Returns the parent context, or <code>null</code> if it is the root
	 * context.
	 */
	public TaggingContext getParent() {
		return this.parent;
	}

	/**
	 * Adds the given tag. If the context is a nested context, the ancestors are
	 * also tagged.
	 * 
	 * @throws IllegalArgumentException if the tag is <code>null</code>
	 */
	public void addTag(String tag) {
		Assert.notNull(tag, "Tag must not be null.");
		if (tags == null) {
			tags = new HashSet();
		}
		tags.add(tag);
		if (parent != null) {
			parent.addTag(tag);
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("Adding tag: " + tag);
			}
		}
	}

	/**
	 * Returns the tags assigned via the {@link #addTag(String)} method.
	 */
	public Set getTags() {
		return this.tags;
	}

	
	// -- Static methods ------------------------------------------------------

	
	public static void tag(HttpServletRequest request, String tag) {
		TaggingContext context = getContext(request);
		if (context != null) {
			context.addTag(tag);
		}
	}
	
	public static void tag(String tag) {
		TaggingContext context = getContext();
		if (context != null) {
			context.addTag(tag);
		}
	}

	/**
	 * Opens a nested context.
	 * @see #popTags(HttpServletRequest) 
	 */
	public static void openNestedContext(HttpServletRequest request) {
		TaggingContext parent = getContext(request);
		request.setAttribute(REQUEST_ATTRIBUTE, new TaggingContext(parent));
	}

	/**
	 * Closes the current context and returns the tags that have been assigned.
	 * The method may return <code>null</code> if no tags were set or no open 
	 * context exists.
	 * <p><b>Note:</b> Only invoke this method if you opened a nested context
	 * before. 
	 */
	public static Set popTags(HttpServletRequest request) {
		TaggingContext top = getContext(request);
		if (top != null) {
			request.setAttribute(REQUEST_ATTRIBUTE, top.getParent());
			return top.getTags();
		}
		return null;
	}

	/**
	 * Retrieves the current context from the given request. The method will 
	 * return <code>null</code> if no open context exists.
	 */
	public static TaggingContext getContext(HttpServletRequest request) {
		return (TaggingContext) request.getAttribute(REQUEST_ATTRIBUTE);
	}

	/**
	 * Retrieves the current context using Spring's 
	 * {@link RequestContextHolder}. 
	 */
	public static TaggingContext getContext() {
		return (TaggingContext) RequestContextHolder.getRequestAttributes()
				.getAttribute(REQUEST_ATTRIBUTE, 
				RequestAttributes.SCOPE_REQUEST);
	}

}