/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Olivier Grisel
 */
package org.nuxeo.ecm.platform.video.storyboard;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

public class TextItem implements Serializable, Comparable<TextItem> {

    private static final long serialVersionUID = 1L;

    public static final Log log = LogFactory.getLog(TextItem.class);

    protected final DocumentModel doc;

    protected final int position;

    protected String timecode = "0";

    protected String displayTime = "0";

    protected Double tc;

    protected String text;

    public TextItem(DocumentModel doc, String basePropertyPath, int position)
            throws PropertyException, ClientException {
        this.doc = doc;
        this.position = position;
        String propertyPath = basePropertyPath + "/" + position;
        try {
            tc = doc.getProperty(propertyPath + "/timecode").getValue(
                    Double.class);
            if (tc != null) {
                double hours = Math.floor(tc / 3600);
                double minutes = Math.floor((tc / 60) - (hours * 60));
                double seconds = tc - (hours * 3600)
                        - (minutes * 60);
                DecimalFormat decimalFormater = new DecimalFormat("00");
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                DecimalFormat secondsformatter = new DecimalFormat("00.000", symbols);
                timecode = String.format("%s:%s:%s",
                        decimalFormater.format(hours),
                        decimalFormater.format(minutes),
                        secondsformatter.format(seconds));
            }
        } catch (Exception e) {
            log.warn(e);
        }
        text = doc.getProperty(
                VideoInfoConstants.TEXT_PROPERTY + "/" + position + "/text").getValue(
                String.class);
    }

    public String getText() {
        return text;
    }

    public Double getTc() {
        return tc;
    }

    public String getTimecode() {
        return timecode;
    }

    public int getPosition() {
        return position;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    @Override
    public int compareTo(TextItem o) {
        if (tc == o.tc) {
            return 0;
        } else if (tc < o.tc) {
            return -1;
        } else {
            return 1;
        }
    }
}
