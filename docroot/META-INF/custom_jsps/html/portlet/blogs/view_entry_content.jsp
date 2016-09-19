<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%--@ page import="com.liferay.portal.kernel.util.StringUtil" --%>
<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page import="com.liferay.portlet.blogs.model.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="com.liferay.portlet.messageboards.service.MBCategoryLocalServiceUtil" %>
<%@ page import="com.liferay.portlet.messageboards.model.*" %>
<%@ page import="com.liferay.portal.theme.ThemeDisplay" %>


<liferay-util:buffer var="html">
    <liferay-util:include page="/html/portlet/blogs/view_entry_content.portal.jsp" />
</liferay-util:buffer>

<%-- <%
html = StringUtil.add(
    html,
    "Didn't find what you were looking for? Refine your search and " +
        "try again!",
    "\n");
%> --%>

<%! String siteName = ""; %>
<%! long categoryId = 0; %>
<%! ThemeDisplay themeDisplay; %>

<% themeDisplay = (ThemeDisplay) request.getAttribute(com.liferay.portal.kernel.util.WebKeys.THEME_DISPLAY);
List<MBCategory> categories = MBCategoryLocalServiceUtil.getCategories(themeDisplay.getScopeGroupId());
for(MBCategory category : categories ) {
	if(category.getName().equals("Ideenverwaltung")) {
		categoryId = category.getCategoryId();
		break;
	}
}

siteName = themeDisplay.getSiteGroupName();
%>

<%@ include file="/html/portlet/blogs/init.jsp" %>
<% BlogsEntry entry = (BlogsEntry)request.getAttribute("view_entry_content.jsp-entry"); %>

<%= html %>

<%! String blogDate; %>
<%	SimpleDateFormat dateFormatOut = new SimpleDateFormat("dd.MM.yyyy");
	blogDate = dateFormatOut.format(entry.getCreateDate()); %>

<portlet:actionURL var="messageBoardActionURL">
	<portlet:param name="struts_action" value="/blogs/discuss_blogentry" />
	<portlet:param name="blogID" value="<%= Long.toString(entry.getEntryId()) %>" />
	<portlet:param name="blogTitle" value="<%= HtmlUtil.escape(entry.getTitle()) %>" />
	<portlet:param name="blogContent" value="<%= HtmlUtil.escape(entry.getContent()) %>" />
	<portlet:param name="actionType" value="messageboard" />
</portlet:actionURL>

<portlet:actionURL var="wikiActionURL">
	<portlet:param name="struts_action" value="/blogs/discuss_blogentry" />
	<portlet:param name="blogID" value="<%= Long.toString(entry.getEntryId()) %>" />
	<portlet:param name="blogTitle" value="<%= HtmlUtil.escape(entry.getTitle()) %>" />
	<portlet:param name="blogContent" value="<%= HtmlUtil.escape(entry.getContent()) %>" />
	<portlet:param name="blogDate" value="<%= HtmlUtil.escape(blogDate) %>" />
	<portlet:param name="actionType" value="wiki" />
</portlet:actionURL>


<div class="entry-after-footer">
	<strong><liferay-ui:message key="ideen_mb-category-name" /></strong> <liferay-ui:message key="ideen_for" /> &quot;<%= HtmlUtil.escape(entry.getTitle()) %>&quot;:<br />
	<div style="float:left; padding-right:10px; margin-right:10px; border-right:1px solid #999;">
	<liferay-ui:icon
		iconCssClass="icon-comments"
		label="<%= true %>"
		message="ideen_mb-discuss1"
		url="<%= messageBoardActionURL.toString() %>"
	/> <% if(!siteName.equals("")) { %>(<liferay-ui:message key="ideen_mb-discuss2" arguments="<%= siteName %>" />)<% } %></div>
	<liferay-ui:icon
		iconCssClass="icon-file"
		label="<%= true %>"
		message="ideen_3community-text"
		url="<%= wikiActionURL.toString() %>"
	/>
	
	<div class="separator" style="clear:both; border-width:2px 0;"></div>
</div>