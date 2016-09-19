package de.iisys.liferay.hook.blog.ideenreifung;

import java.util.List;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.wiki.model.WikiPage;
import com.liferay.portlet.wiki.service.WikiPageLocalServiceUtil;

public class IdeaPageCreator {
	
	private String IDEA_PAGE;
	private ThemeDisplay themeDisplay;
	private long scopeGroupId;
	
	public IdeaPageCreator(ThemeDisplay themeDisplay, String IDEA_PAGE, long scopeGroupId) {
		this.themeDisplay = themeDisplay;
		this.scopeGroupId = scopeGroupId;
		this.IDEA_PAGE = IDEA_PAGE;
	}
	
	public long getIdeaWikiNode(boolean firstTry) {
		System.out.println("getIdeaWikiNode()");
		List<WikiPage> pages = WikiPageLocalServiceUtil.getWikiPages(0, Integer.MAX_VALUE);

		for(WikiPage wp : pages) {
			try {
				String where = GroupLocalServiceUtil.getGroupDescriptiveName(wp.getNode().getGroupId(), themeDisplay.getLocale());
				if(where.equals(IDEA_PAGE))
					return wp.getNodeId();
				
			} catch (PortalException e) {
				e.printStackTrace();
			}
	       	
		}
		if(firstTry)
			return createIdeaCommunity();
		else
			return 0;
	}
	
	private long createIdeaCommunity() {
		System.out.println("createIdeaCommunity()");
		Group ideaGroup = null;
		
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(scopeGroupId);
		
		List<Group> groups = GroupLocalServiceUtil.getGroups(0, Integer.MAX_VALUE);
		for(Group gr : groups) {
			if(gr.getName().equals(IDEA_PAGE)) {
				ideaGroup = gr;
				break;
			}
		}
		
		if(ideaGroup==null) {
			String className = "com.liferay.portal.model.Group";
			long classPK = 0;
			long liveGroupId = 0;
			String description = "";
			boolean manualMembership = true;
			boolean site = true;
			boolean active = true;
			String friendlyURL = "/"+IDEA_PAGE.toLowerCase();
	
			try {
				ideaGroup = GroupLocalServiceUtil.addGroup(themeDisplay.getUserId(), GroupConstants.DEFAULT_PARENT_GROUP_ID, className, classPK, liveGroupId, IDEA_PAGE, description, GroupConstants.TYPE_SITE_OPEN, manualMembership, GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, friendlyURL, site, active, serviceContext);	
			} catch (PortalException e) {
				e.printStackTrace();
				System.out.println("Error: Could not create group/community "+IDEA_PAGE);
			}
		}
		
		
		boolean addedPages = false;
		if(ideaGroup!=null)
			addedPages = addPagesToCommunity(ideaGroup, serviceContext);
		
		if(addedPages)
			return getIdeaWikiNode(false);
		else
			return 0;
	}
	
	private boolean addPagesToCommunity(Group group, ServiceContext serviceContext) {
		System.out.println("addPagesToCommunity()");
		String friendlyUrl = "/wiki";
		Layout layout = null;
		
		List<Layout> layouts = LayoutLocalServiceUtil.getLayouts(group.getGroupId(), false, LayoutConstants.TYPE_PORTLET);
		for(Layout l : layouts) {
			if(l.getFriendlyURL().equals(friendlyUrl)) {
				layout = l;
				break;
			}
		}
		
		if(layout == null) {
			try {
				layout= LayoutLocalServiceUtil.addLayout(
						themeDisplay.getUserId(),
						group.getGroupId(),
						false,										// privateLayout
						LayoutConstants.DEFAULT_PARENT_LAYOUT_ID,
						"Wiki",										// name
						group.getName()+" - Wiki",					// html-title
						"Wiki"+"_description",						// description
						LayoutConstants.TYPE_PORTLET,
						false,										// hidden
						"/"+"wiki",									// friendlyUrl
						serviceContext);
				
			} catch (PortalException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet)layout.getLayoutType();
		layoutTypePortlet.setLayoutTemplateId(themeDisplay.getUserId(), PropsUtil.get(PropsKeys.LAYOUT_DEFAULT_TEMPLATE_ID), false);
		boolean addedPortlet = addPortlet(layoutTypePortlet, 2, PortletKeys.WIKI);
		
		LayoutLocalServiceUtil.updateLayout(layout);
		return addedPortlet;
	}
	
	private boolean addPortlet(LayoutTypePortlet layoutTypePortlet, int column, String portletKeysId) {
		System.out.println("addPortlet("+portletKeysId+")");
		try {
			layoutTypePortlet.addPortletIds(0, StringUtil.split(portletKeysId), "column-"+column, false);
			return true;
		} catch (PortalException e) {
			e.printStackTrace();
			return false;
		}
	}

	private final String PREF_WIKINODE = "wikiNodeId";
}
