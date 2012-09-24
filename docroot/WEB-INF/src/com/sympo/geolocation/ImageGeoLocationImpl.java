/**
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

package com.sympo.geolocation;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.util.DLProcessor;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoRowLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

import java.io.BufferedInputStream;
import java.io.InputStream;

import java.util.Set;

/**
 * @author Sergio González
 */
public class ImageGeoLocationImpl implements DLProcessor {

	public void cleanUp(FileEntry fileEntry) {
	}

	public void cleanUp(FileVersion fileVersion) {
		try {
			ExpandoRowLocalServiceUtil.deleteExpandoRow(
				fileVersion.getFileVersionId());
		}
		catch (Exception e) {
		}
	}

	public void exportGeneratedFiles(
			PortletDataContext portletDataContext, FileEntry fileEntry,
			Element fileEntryElement)
		throws Exception {
	}

	public void importGeneratedFiles(
			PortletDataContext portletDataContext, FileEntry fileEntry,
			FileEntry importedFileEntry, Element fileEntryElement)
		throws Exception {
	}

	public boolean isSupported(FileVersion fileVersion) {
		if (fileVersion == null) {
			return false;
		}

		return isSupported(fileVersion.getMimeType());
	}

	public boolean isSupported(String mimeType) {
		if (Validator.isNull(mimeType)) {
			return false;
		}

		return _imageMimeTypes.contains(mimeType);
	}

	/**
	 * Launches the processor's work with respect to the given file version.
	 *
	 * @param fileVersion the latest file version to process
	 */
	public void trigger(FileVersion fileVersion) {
		GeoLocation geoLocation = null;

		try {
			InputStream is = fileVersion.getContentStream(false);

			BufferedInputStream bf = new BufferedInputStream(is);

			Metadata metadata = ImageMetadataReader.readMetadata(bf, false);

			GpsDirectory gpsDirectory = metadata.getDirectory(
				GpsDirectory.class);

			geoLocation = gpsDirectory.getGeoLocation();
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Couldn't obtain 'latitude' and 'longitude' of " +
						fileVersion.getTitle());
			}
		}

		if (geoLocation != null) {
			double latitude = geoLocation.getLatitude();
			double longitude = geoLocation.getLongitude();

			try {
				ExpandoValueLocalServiceUtil.addValue(
					fileVersion.getCompanyId(), DLFileEntry.class.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME, "latitude",
					fileVersion.getFileVersionId(), latitude);

				ExpandoValueLocalServiceUtil.addValue(
					fileVersion.getCompanyId(), DLFileEntry.class.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME, "longitude",
					fileVersion.getFileVersionId(), longitude);
			}
			catch (Exception e) {
				if (_log.isErrorEnabled()) {
					_log.error(
						"Couldn't store 'latitude' and 'longitude' of " +
							fileVersion.getTitle());
				}
			}
		}
	}

	private static Log _log = LogFactoryUtil.getLog(ImageGeoLocationImpl.class);

	private Set<String> _imageMimeTypes = SetUtil.fromArray(
		PropsUtil.getArray(PropsKeys.DL_FILE_ENTRY_PREVIEW_IMAGE_MIME_TYPES));

}