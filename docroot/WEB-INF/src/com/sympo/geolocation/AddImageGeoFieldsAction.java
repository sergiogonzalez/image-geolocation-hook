package com.sympo.geolocation;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;

/**
 * @author Sergio González
 */
public class AddImageGeoFieldsAction extends SimpleAction {

	@Override
	public void run(String[] ids) throws ActionException {
		try {
			doRun(GetterUtil.getLong(ids[0]));
		}
		catch (Exception e) {
			throw new ActionException(e);
		}
	}

	protected void doRun(long companyId) throws Exception {
		ExpandoTable expandoTable = null;

		try {
			expandoTable = ExpandoTableLocalServiceUtil.addTable(
				DLFileEntry.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME);
		}
		catch (Exception e) {
			expandoTable = ExpandoTableLocalServiceUtil.getTable(
				DLFileEntry.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME);
		}

		try {
			UnicodeProperties properties = new UnicodeProperties();

			properties.setProperty("hidden", "true");
			properties.setProperty("visible-with-update-permission", "false");

			ExpandoColumn latitude = ExpandoColumnLocalServiceUtil.addColumn(
				expandoTable.getTableId(), "latitude",
				ExpandoColumnConstants.NUMBER);

			ExpandoColumnLocalServiceUtil.updateTypeSettings(
					latitude.getColumnId(), properties.toString());

			if (_log.isInfoEnabled()) {
				_log.info(
					"Custom field 'latitude' added to DLFileEntry entity");
			}

			ExpandoColumn longitude = ExpandoColumnLocalServiceUtil.addColumn(
				expandoTable.getTableId(), "longitude",
				ExpandoColumnConstants.NUMBER);

			ExpandoColumnLocalServiceUtil.updateTypeSettings(
					longitude.getColumnId(), properties.toString());

			if (_log.isInfoEnabled()) {
				_log.info(
					"Custom field 'longitude' added to DLFileEntry entity");
			}
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Couldn't add custom fields 'latitude' and 'longitude'");
			}
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		AddImageGeoFieldsAction.class);

}