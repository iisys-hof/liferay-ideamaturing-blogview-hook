package de.iisys.liferay.hook.blog.ideenreifung;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.asset.model.AssetLinkConstants;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetLinkLocalServiceUtil;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.wiki.model.WikiPage;

public class LinkedAssetCreator {
	
	public static void addWikiAssetToBlogEntry(ThemeDisplay themeDisplay, long entryId, WikiPage wp) {
		try {			
			long wikiAssetId = AssetEntryLocalServiceUtil.getEntry( WikiPage.class.getName(), wp.getResourcePrimKey() ).getEntryId();
			addAssetToBlogEntry(themeDisplay, entryId, wikiAssetId);
		} catch (PortalException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void addMBAssetToBlogEntry(ThemeDisplay themeDisplay, long entryId, MBMessage mbMessage) {
		try {
			long mbMAssetId = AssetEntryLocalServiceUtil.getEntry( MBMessage.class.getName(), mbMessage.getMessageId() ).getEntryId();
			addAssetToBlogEntry(themeDisplay, entryId, mbMAssetId);
		} catch (PortalException e) {
			e.printStackTrace();
		}
	}
	
	private static void addAssetToBlogEntry(ThemeDisplay themeDisplay, long entryId, long secondAssetId) {
		BlogsEntry entry = BlogsEntryLocalServiceUtil.fetchBlogsEntry(entryId);
		try {
			long blogAssetId = AssetEntryLocalServiceUtil.getEntry( BlogsEntry.class.getName(), entry.getEntryId() ).getEntryId();
			AssetLinkLocalServiceUtil.addLink(themeDisplay.getUserId(), blogAssetId, secondAssetId, AssetLinkConstants.TYPE_RELATED, 0);
		} catch (PortalException e) {
			e.printStackTrace();
		}
	}
}
