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
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

public class SubtitleItem extends HashMap<String, Serializable> implements
        Serializable, Comparable<SubtitleItem> {

    private static final long serialVersionUID = 1L;

    public static final Log log = LogFactory.getLog(SubtitleItem.class);

    protected final DocumentModel doc;

    protected final int position;

    protected String start = "0";

    protected String end = "0";

    protected String text;

    protected Double tcStart;

    protected Double tcEnd;

    public SubtitleItem(DocumentModel doc, String basePropertyPath, int position)
            throws PropertyException, ClientException {
        this.doc = doc;
        this.position = position;
        String propertyPath = basePropertyPath + "/" + position;
        try {
            tcStart = doc.getProperty(propertyPath + "/start").getValue(
                    Double.class);
            if (tcStart != null) {
                double hours = Math.floor(tcStart / 3600);
                double minutes = Math.floor((tcStart / 60) - (hours * 60));
                double seconds = tcStart - (hours * 3600) - (minutes * 60);
                DecimalFormat decimalFormater = new DecimalFormat("00");
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(
                        Locale.US);
                DecimalFormat secondsformatter = new DecimalFormat("00.000",
                        symbols);
                start = String.format("%s:%s:%s",
                        decimalFormater.format(hours),
                        decimalFormater.format(minutes),
                        secondsformatter.format(seconds));
            }
            tcEnd = doc.getProperty(propertyPath + "/end").getValue(
                    Double.class);
            if (tcEnd != null) {
                double hours = Math.floor(tcEnd / 3600);
                double minutes = Math.floor((tcEnd / 60) - (hours * 60));
                double seconds = tcEnd - (hours * 3600) - (minutes * 60);
                DecimalFormat decimalFormater = new DecimalFormat("00");
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(
                        Locale.US);
                DecimalFormat secondsformatter = new DecimalFormat("00.000",
                        symbols);
                end = String.format("%s:%s:%s", decimalFormater.format(hours),
                        decimalFormater.format(minutes),
                        secondsformatter.format(seconds));
            }
        } catch (Exception e) {
            log.warn(e);
        }
        text = doc.getProperty(
                VideoInfoConstants.SUBTITLE_PROPERTY + "/" + position + "/text").getValue(
                String.class);
    }

    public String getText() {
        return text;
    }

    public String getStart() {
        return start;
    }

    public Double getTcStart() {
        return tcStart;
    }

    public String getEnd() {
        return end;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int compareTo(SubtitleItem o) {
        if (tcStart == o.tcStart) {
            return 0;
        } else if (tcStart < o.tcStart) {
            return -1;
        } else {
            return 1;
        }
    }
}
