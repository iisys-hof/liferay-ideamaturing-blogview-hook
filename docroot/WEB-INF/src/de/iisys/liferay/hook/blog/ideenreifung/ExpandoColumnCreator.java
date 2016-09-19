package de.iisys.liferay.hook.blog.ideenreifung;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portlet.expando.DuplicateColumnNameException;
import com.liferay.portlet.expando.DuplicateTableNameException;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;

public class ExpandoColumnCreator {
	
	/**
	 * Creates a new expando column (e.g. custom field) for the given class (e.g. wiki page).
	 * @param companyId:
	 * @param columnName: the new column's name
	 * @param type: the new column's type (e.g. ExpandoColumnConstants.LONG)
	 * @param className: the expanded class (e.g. WikiPage.class.getName())
	 * @return the new column
	 * @throws PortalException
	 * @throws SystemException
	 */
	public static ExpandoColumn addExpandoAttribute(long companyId, String columnName, int type, String className)
			throws PortalException, SystemException {

			ExpandoTable table = null;
			ExpandoColumn expandoColumn = null;
        
			try {
                 /* A little note about this:
                    Using ExpandoTableConstants.DEFAULT_TABLE_NAME you are trying to add the default expando column.
                                And it will always fail, because that table aready exists within liferay.
                                You can change this value to create a new table, for example:
                    table = ExpandoTableLocalServiceUtil.addTable(companyId, User.class.getName(), "MYTABLE");
                    And of course you can change User.class.getName(), to add expando values to any other liferay model.
                    Usign ExpandoTableConstants.DEFAULT_TABLE_NAME You are going to be able to see and modify them as Custom fields from control Panel.
                    If you create other table, you are not going to be able to modify them from control panel.
				*/
				
				table = ExpandoTableLocalServiceUtil.addTable(companyId, className, ExpandoTableConstants.DEFAULT_TABLE_NAME);
			} catch (DuplicateTableNameException dtne) {
				// Get the default table for User Custom Fields
				table = ExpandoTableLocalServiceUtil.getTable(companyId, className, ExpandoTableConstants.DEFAULT_TABLE_NAME);
			}
			
			table = ExpandoTableLocalServiceUtil.getTable(companyId, className, ExpandoTableConstants.DEFAULT_TABLE_NAME);

			try {   
				expandoColumn = ExpandoColumnLocalServiceUtil.addColumn(table.getTableId(),columnName, type);
				// set column to "hidden":
				UnicodeProperties up = new UnicodeProperties();
				up.put(ExpandoColumnConstants.PROPERTY_HIDDEN, "true");
				expandoColumn.setTypeSettingsProperties(up);
				ExpandoColumnLocalServiceUtil.updateExpandoColumn(expandoColumn);
				System.out.println("Custom Field successfully created.");
			} catch (DuplicateColumnNameException dcne) {
//				System.out.println("Custom Field is already created.");
				expandoColumn = ExpandoColumnLocalServiceUtil.getColumn(companyId, className,ExpandoTableConstants.DEFAULT_TABLE_NAME,columnName);
			}
			
			return expandoColumn;
	}
}
