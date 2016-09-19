package de.iisys.liferay.hook.blog.ideenreifung;

import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.asset.NoSuchEntryException;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetLinkConstants;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetLinkLocalServiceUtil;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBCategoryConstants;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.service.MBCategoryLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.wiki.DuplicatePageException;
import com.liferay.portlet.wiki.PageTitleException;
import com.liferay.portlet.wiki.model.WikiPage;
import com.liferay.portlet.wiki.service.WikiPageLocalServiceUtil;

public class Ideenreifung extends BaseStrutsPortletAction {
	
	private final String EXPANDO_ID = "Ideen-ID";
	private final String PREF_WIKINODE = "wikiNodeId";
	private final String IDEA_PAGE = "Ideen";
	private long WIKI_NODE = 0;
	
	private String actionType;
	
	private ResourceBundle rb;
	private ThemeDisplay themeDisplay;
	private long scopeGroupId;
	
	@Override
	public void processAction(StrutsPortletAction originalStrutsPortletAction,
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse) throws Exception {
		
		String blogTitle = actionRequest.getParameter("blogTitle");
		String blogContent = actionRequest.getParameter("blogContent");
		String blogDate = actionRequest.getParameter("blogDate");
		String blogID = actionRequest.getParameter("blogID");
		actionType = actionRequest.getParameter("actionType");
		
		themeDisplay = (ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
//		scopeGroupId = themeDisplay.getScopeGroupId();
		scopeGroupId = themeDisplay.getUser().getGroupId();
		rb = ResourceBundle.getBundle("content.Language", actionRequest.getLocale());
		
		if(actionType.equals("messageboard") || actionType.equals("wiki")) {
			try {
				ExpandoColumnCreator.addExpandoAttribute(themeDisplay.getCompanyId(), EXPANDO_ID, ExpandoColumnConstants.LONG, BlogsEntry.class.getName());
				ExpandoColumnCreator.addExpandoAttribute(themeDisplay.getCompanyId(), EXPANDO_ID, ExpandoColumnConstants.LONG, MBMessage.class.getName());
				ExpandoColumnCreator.addExpandoAttribute(themeDisplay.getCompanyId(), EXPANDO_ID, ExpandoColumnConstants.LONG, WikiPage.class.getName());
			} catch (SystemException | PortalException e) {
				e.printStackTrace();
			}
		}
		
		// Check for wiki node:
		PortletPreferences prefs = actionRequest.getPreferences();
		if(prefs.getValue( PREF_WIKINODE, "0").equals("0") ) {
			IdeaPageCreator ipCreator = new IdeaPageCreator(themeDisplay, IDEA_PAGE, scopeGroupId);
			prefs.setValue( PREF_WIKINODE, Long.toString(ipCreator.getIdeaWikiNode(true)) );
			prefs.store();
		}
		WIKI_NODE = Long.parseLong(prefs.getValue(PREF_WIKINODE, "0"));
		System.out.println("WIKI_NODE (hook): "+WIKI_NODE);
		
		if(actionType.equals("messageboard")) {
			MBMessage mbMsg = createForumPost(blogTitle,blogContent);
			if(mbMsg!=null) {
				mbMsg.getExpandoBridge().setAttribute(EXPANDO_ID, blogID);
				LinkedAssetCreator.addMBAssetToBlogEntry(themeDisplay, Long.parseLong(blogID), mbMsg);
			}
		} else if(actionType.equals("wiki")) {
			WikiPage wp = createWikiPage(blogTitle,blogDate);
			if(wp!=null) {
				wp.getExpandoBridge().setAttribute(EXPANDO_ID, blogID);
				LinkedAssetCreator.addWikiAssetToBlogEntry(themeDisplay, Long.parseLong(blogID), wp);
				System.out.println("PageId: "+wp.getPageId());
				System.out.println("NodeId: "+wp.getNodeId());
				System.out.println("PrimaryKey: "+wp.getPrimaryKey());
				System.out.println("ResourcePrimKey: "+wp.getResourcePrimKey());
			}
		}
		
/*		String portletName = (String)actionRequest.getAttribute(WebKeys.PORTLET_ID);
		PortletURL redirectURL = PortletURLFactoryUtil.create(PortalUtil.getHttpServletRequest(actionRequest),portletName,themeDisplay.getLayout().getPlid(), PortletRequest.RENDER_PHASE);
		redirectURL.setParameter("jspPage", "/portlet/wiki/view_all_pages.jsp");
		actionResponse.sendRedirect(redirectURL.toString()); */
		
//		PortalUtil.copyRequestParameters(actionRequest, actionResponse);
//		actionResponse.setRenderParameter("jspPage", "/portlet/wiki/view_all_pages.jsp");
		
		super.processAction(originalStrutsPortletAction, portletConfig, actionRequest, actionResponse);
	}

	/*
	@Override
	public String render(StrutsPortletAction originalPortletAction, PortletConfig portletConfig, RenderRequest renderRequest, RenderResponse renderResponse) throws Exception {		
//		return super.render(portletConfig, renderRequest, renderResponse);
		
		if(actionType.equals("messageboard"))
			return "/portlet/message_boards/view.jsp";
		else if(actionType.equals("wiki")) {
//			long plid = PortalUtil.getPlidFromPortletId(wikiNode.getGroupId(), PortletKeys.WIKI);
//			Layout layout = LayoutLocalServiceUtil.getLayout(plid);
//			return layout.getFriendlyURL();
			
//			return "/portlet/wiki/view_all_pages.jsp";
//			return "/web/ideen/wiki";
			PortletURL portletURL = renderResponse.createRenderURL();
			portletURL.setParameter("jspPage", "/portlet/wiki/view_all_pages.jsp");
			System.out.println("URL: "+portletURL.toString());
			return portletURL.toString();
		}
		else
			return super.render(portletConfig, renderRequest, renderResponse);

	} */

	/**
	 * Creates a thread in the message board's idea-category (defined in the Language properties)
	 * @param title: The thread's title
	 * @param message: The thread's message/body
	 * @return the newly created thread
	 * @throws PortalException 
	 */
	private MBMessage createForumPost(String title, String message) throws PortalException {
		System.out.println("createForumPost()");
		
		MBMessage mbMsg = null;
		String theTitle = titleCheck(title);
		String theMessage = "<< "+LanguageUtil.get(rb, "ideen_mbthread-msg1")+" >>\n\n"+LanguageUtil.format(rb, "ideen_mbthread-msg2", theTitle)+":\n\n"+message;
		
		ServiceContext serviceContext = new ServiceContext();
	    serviceContext.setScopeGroupId(scopeGroupId);
		
		try {
			mbMsg = MBMessageServiceUtil.addMessage(getForumCategoryId(serviceContext), theTitle, theMessage, serviceContext);
			System.out.println("Successfully created "+theTitle+" with status "+mbMsg.getStatus());
		} catch (PortalException e) {
			e.printStackTrace();
			System.out.println("Error: Could not create new thread in message board!");
		}
		return mbMsg;
	}
	
	/**
	 * Returns the message board category-id of the specified idea-category (defined in the Language properties)
	 * If there is no such category, it will be created.
	 * @param serviceContext
	 * @return category id of the category with the name CATEGORY_NAME
	 * @throws PortalException
	 */
	private long getForumCategoryId(ServiceContext serviceContext) throws PortalException {
		List<MBCategory> categories;
		try {
			categories = MBCategoryLocalServiceUtil.getCategories(scopeGroupId);
			for(MBCategory category : categories ){
			    if(category.getName().equals( LanguageUtil.get(rb, "ideen_mb-category-name") ))
			    	return category.getCategoryId();
			}
			MBCategory createdCategory = MBCategoryLocalServiceUtil.addCategory(themeDisplay.getUserId(), MBCategoryConstants.DEFAULT_PARENT_CATEGORY_ID, LanguageUtil.get(rb, "ideen_mb-category-name"), LanguageUtil.get(rb, "ideen_mb-category-desc"), serviceContext);
			return createdCategory.getCategoryId();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * Creates a new page in the wiki with the node: WIKI_NODE.
	 * @param title
	 * @param createDate
	 * @return the newly created wiki page
	 * @throws PortalException
	 */
	private WikiPage createWikiPage(String title, String createDate) throws PortalException {   
	    ServiceContext serviceContext = new ServiceContext();
	    serviceContext.setScopeGroupId(scopeGroupId);
	    
	    Object[] contentArgs = {titleCheck(title), themeDisplay.getUser().getFullName(), createDate, title};
//	    String content = LanguageUtil.format(rb, "ideen_wikipage-content1", contentArgs)
//	    					+ LanguageUtil.get(rb, "ideen_wikipage-content2");
	    String content = themeDisplay.translate("ideen_wikipage-content1", contentArgs)
				+ themeDisplay.translate("ideen_wikipage-content2");
//	    String summary = LanguageUtil.format(rb, "ideen_wikipage-summary", titleCheck(title));
	    String summary = themeDisplay.translate("ideen_wikipage-summary", titleCheck(title));
	    boolean minorEdit = false;
	    
		try {		
			WikiPage wp = WikiPageLocalServiceUtil.addPage(themeDisplay.getUserId(), WIKI_NODE, wikiTitleCheck(title), content, summary, minorEdit, serviceContext);
			
			// add new page to frontpage:
			WikiPage frontPage = WikiPageLocalServiceUtil.getPage(WIKI_NODE, "FrontPage");
			frontPage.setContent("\n*"+createDate+": [["+wp.getTitle()+"]]\n"+frontPage.getContent());
			WikiPageLocalServiceUtil.updateWikiPage(frontPage);
			
			System.out.println("Success: Wiki page "+title+" created.");
			return wp;
		} catch (PageTitleException ex) {
			ex.printStackTrace();
			System.out.println("Warning: PageTitleException!");
//			createWikiPage(wikiTitleCheck(title));
		} catch (DuplicatePageException dpe) {
//			dpe.printStackTrace();
			System.out.println("DuplicatePageException: Wiki page already exists.");
		}
		return null;
	}
	
	private String titleCheck(String formerTitle) {
		if(formerTitle.startsWith("Idee:") || formerTitle.startsWith("Idea:"))
			return formerTitle;
		else
			return LanguageUtil.get(rb, "ideen_idea")+": "+formerTitle;
	}
	
	private String wikiTitleCheck(String title) {
		return title.replace(":", "");
	}
	
	
}
