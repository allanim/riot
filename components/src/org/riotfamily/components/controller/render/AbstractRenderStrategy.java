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
package org.riotfamily.components.controller.render;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.riotfamily.components.config.ComponentListConfiguration;
import org.riotfamily.components.config.ComponentRepository;
import org.riotfamily.components.config.component.AbstractComponent;
import org.riotfamily.components.config.component.Component;
import org.riotfamily.components.dao.ComponentDao;
import org.riotfamily.components.model.ComponentList;
import org.riotfamily.components.model.ComponentVersion;
import org.riotfamily.components.model.Location;
import org.riotfamily.components.model.VersionContainer;

public class AbstractRenderStrategy implements RenderStrategy {
	
	public static final String INHERTING_COMPONENT = "inherit";
	
	protected Log log = LogFactory.getLog(getClass());
	
	protected ComponentDao dao; 
	
	protected ComponentRepository repository;
				
	protected ComponentListConfiguration config;
	
	public AbstractRenderStrategy(ComponentDao dao, 
			ComponentRepository repository, ComponentListConfiguration config) {
		
		this.dao = dao;
		this.repository = repository;
		this.config = config;
	}
	
	public final void render(HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		Location location = getLocation(request);
		render(location, request, response);
	}
	
	protected void render(Location location, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		ComponentList list = getComponentList(location, request);
		if (list != null) {
			renderComponentList(list, request, response);
		}
		else {
			onListNotFound(location, request, response);
		}
	}
	
	public void render(ComponentList list, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		renderComponentList(list, request, response);
	}
	
	protected final Location getLocation(HttpServletRequest request) {
		Location location = config.getLocator().getLocation(request);
		log.debug("List location: " + location);
		return location;
	}
	
	protected void onListNotFound(Location location, 
			HttpServletRequest request, HttpServletResponse response) 
			throws Exception {
		
		log.debug("No ComponentList found for " + location); 
	}
	
	protected VersionContainer getParentContainer(HttpServletRequest request) {
		return (VersionContainer) request.getAttribute(AbstractComponent.CONTAINER);
	}
	
	/**
	 * Returns the ComponentList to be rendered. The default implementation
	 * uses the controller's ComponentDao to look up a list for the given 
	 * location.
	 */
	protected ComponentList getComponentList(Location location, 
			HttpServletRequest request) {
		
		VersionContainer parent = getParentContainer(request);
		if (parent != null) {
			return dao.findComponentList(parent, location.getSlot());
		}
		return dao.findComponentList(location);
	}	
	
	/**
	 * Renders the given list. The default implementation calls 
	 * {@link #getComponentsToRender(ComponentList)} and passes the result
	 * to {@link #renderComponents(List)}.
	 */
	protected void renderComponentList(ComponentList list, 
			HttpServletRequest request, HttpServletResponse response) 
			throws Exception {
		
		List components = getComponentsToRender(list);
		renderComponents(components, request, response);
	}
	
	/**
	 * Renders the given list. The default implementation iterates over the 
	 * given list and calls {@link #renderContainer(VersionContainer, String)} 
	 * for each item. If the list is empty or null, 
	 * {@link #onEmptyComponentList(HttpServletRequest, HttpServletResponse)} is invoked.
	 */
	protected final void renderComponents(List components, 
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		if (components == null || components.isEmpty()) {
			onEmptyComponentList(request, response);
			return;
		}
		
		int i = 0;
		Iterator it = components.iterator();
		while (it.hasNext()) {
			VersionContainer container = (VersionContainer) it.next();
			renderContainer(container, 
					getPositionalClassName(i++, !it.hasNext()),
					request, response);
		}
	}
	
	protected void onEmptyComponentList(HttpServletRequest request, 
			HttpServletResponse response) throws Exception {

	}
	
	/**
	 * Returns a list of VersionContainers. The default implementation
	 * simply returns the list's live components.
	 */
	protected List getComponentsToRender(ComponentList list) {
		return list.getLiveContainers();
	}
	
	/**
	 * Renders the given VersionContainer. The default implementation calls 
	 * {@link #getVersionToRender(VersionContainer) getVersionToRender()} and 
	 * passes the result to {@link #renderComponentVersion(ComponentVersion, String) 
	 * renderComponentVersion()} (if not null). 
	 */
	protected void renderContainer(VersionContainer container, 
			String positionClassName, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {

		ComponentVersion version = getVersionToRender(container);
		if (version != null) {
			renderComponentVersion(version, positionClassName, request, response);
		}
	}
		
	/**
	 * Returns the ComponentVersion to render. The default implementation 
	 * simply returns the component's live version.
	 */
	protected ComponentVersion getVersionToRender(VersionContainer container) {
		return container.getLiveVersion();
	}
	
	/**
	 * Renders the given ComponentVersion. 
	 */
	protected final void renderComponentVersion(ComponentVersion version, 
			String positionClassName, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		String type = version.getType();
		if (INHERTING_COMPONENT.equals(type)) {
			renderParentList(version.getContainer().getList(), request, response);
		}
		else {
			Component component = repository.getComponent(type);		
			renderComponent(component, version, positionClassName, 
					request, response);
		}
	}
	
	protected final void renderParentList(ComponentList list, 
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		Location location = list.getLocation();
		Location parentLocation = config.getLocator().getParentLocation(location);
		log.debug("Parent location: " + parentLocation);
		
		if (parentLocation != null) {
			if (location.equals(parentLocation)) {
				log.warn("Parent location is the same");
				return;
			}
			ComponentList parentList = getComponentList(parentLocation, request);
			if (parentList != null) {
				getStrategyForParentList().render(parentList, request, response);
			}
			else {
				onListNotFound(parentLocation, request, response);
			}
		}
	}
	
	protected RenderStrategy getStrategyForParentList() throws IOException {
		return this;
	}
	
	protected void renderComponent(Component component, 
			ComponentVersion version, String positionClassName,
			HttpServletRequest request, HttpServletResponse response) 
			throws Exception {
		
		component.render(version, positionClassName, request, response);
	}
	
	protected String getPositionalClassName(int position, boolean last) {
		StringBuffer sb = new StringBuffer("component-").append(position + 1);
		if (last) {
			sb.append(" last-component");
		}
		return sb.toString();
	}

}