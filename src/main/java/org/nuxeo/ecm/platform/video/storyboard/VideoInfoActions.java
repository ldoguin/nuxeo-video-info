/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 */
package org.nuxeo.ecm.platform.video.storyboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;

/**
 *
 * @author ldoguin
 */
@Name("videoInfoActions")
@Scope(ScopeType.EVENT)
public class VideoInfoActions {

    @In(create = true, required = false)
    protected CoreSession documentManager;

    @In
    protected NavigationContext navigationContext;

    @In(create = true, required = false)
    protected StoryboardActions storyboardActions;

    protected String timecode;

    protected String textItem;

    protected List<TextItem> textItems;

    protected String start;

    protected String end;

    protected List<SubtitleItem> subtitleItems;

    @Create
    public void initTextItems() throws PropertyException, ClientException {
        DocumentModel doc = navigationContext.getCurrentDocument();
        refreshTextItems(doc);
        refreshSubTitleItems(doc);
    }

    private void refreshTextItems(DocumentModel doc) throws PropertyException,
            ClientException {
        if (!doc.hasFacet(VideoInfoConstants.VIDEO_INFO_FACET)) {
            textItems = Collections.emptyList();
            return;
        }
        int size = doc.getProperty(VideoInfoConstants.TEXT_PROPERTY).getValue(
                List.class).size();
        List<TextItem> items = new ArrayList<TextItem>(size);
        for (int i = 0; i < size; i++) {
            items.add(new TextItem(doc, VideoInfoConstants.TEXT_PROPERTY, i));
        }
        Collections.sort(items);
        textItems = items;
    }

    public void addTextItem(DocumentModel doc) throws PropertyException,
            ClientException {
        if (!doc.hasFacet(VideoInfoConstants.VIDEO_INFO_FACET)) {
            doc.addFacet(VideoInfoConstants.VIDEO_INFO_FACET);
        }
        Property p = doc.getProperty(VideoInfoConstants.TEXT_PROPERTY);
        Map<String, Serializable> textItemMap = new HashMap<String, Serializable>();
        textItemMap.put("text", textItem);
        textItemMap.put("timecode", timecode);
        p.addValue(textItemMap);
        doc = documentManager.saveDocument(doc);
        refreshTextItems(doc);
    }

    public void removeTextItem(DocumentModel doc, TextItem textItem)
            throws PropertyException, ClientException {
        Property p = doc.getProperty(VideoInfoConstants.TEXT_PROPERTY + "/"
                + textItem.getPosition());
        p.remove();
        doc = documentManager.saveDocument(doc);
        refreshTextItems(doc);
    }

    private void refreshSubTitleItems(DocumentModel doc)
            throws PropertyException, ClientException {
        if (!doc.hasFacet(VideoInfoConstants.VIDEO_INFO_FACET)) {
            subtitleItems = Collections.emptyList();
            return;
        }
        int size = doc.getProperty(VideoInfoConstants.SUBTITLE_PROPERTY).getValue(
                List.class).size();
        List<SubtitleItem> items = new ArrayList<SubtitleItem>(size);
        for (int i = 0; i < size; i++) {
            items.add(new SubtitleItem(doc,
                    VideoInfoConstants.SUBTITLE_PROPERTY, i));
        }
        Collections.sort(items);
        subtitleItems = items;
    }

    public void addSubTitleItem(DocumentModel doc) throws PropertyException,
            ClientException {
        if (!doc.hasFacet(VideoInfoConstants.VIDEO_INFO_FACET)) {
            doc.addFacet(VideoInfoConstants.VIDEO_INFO_FACET);
        }
        Map<String, Serializable> subtitleItemMap = new HashMap<String, Serializable>();
        subtitleItemMap.put("text", textItem);
        subtitleItemMap.put("start", start);
        subtitleItemMap.put("end", end);
        Property p = doc.getProperty(VideoInfoConstants.SUBTITLE_PROPERTY);
        List<Map<String, Serializable>> subs = (List<Map<String, Serializable>>)p.getValue();
        for (Map<String, Serializable> sub : subs) {
            double s1 = (Double) sub.get("start");
            double s2 = (Double) subtitleItemMap.get("start");
            if (s2 > s1) {

            }
        }
        p.addValue(subtitleItemMap);
        writeWebVTTFile(doc);
        doc = documentManager.saveDocument(doc);
        refreshSubTitleItems(doc);
    }

    private void writeWebVTTFile(DocumentModel doc) throws PropertyException,
            ClientException {
        StringBuilder sb = new StringBuilder();
        sb.append("WEBVTT\n");
        sb.append("\n");
        for (SubtitleItem item : subtitleItems) {
            sb.append(item.position + "\n");
            sb.append(item.start + " --> " + item.end + "\n");
            sb.append(item.text + "\n");
            sb.append("\n");
        }
        StringBlob b = new StringBlob(sb.toString());
        b.setEncoding("utf-8");
        b.setMimeType("text/vtt");
        b.setFilename("subtitiles.vtt");
        doc.setPropertyValue(VideoInfoConstants.WEBVTT_PROPERTY, b);
    }

    public String getSubtitlesURL(DocumentModel doc) throws PropertyException,
            ClientException {
        Property p = doc.getProperty(VideoInfoConstants.WEBVTT_PROPERTY);
        if (p == null) {
            return null;
        }
        Blob b = p.getValue(Blob.class);
        if (b == null) {
            return null;
        }
        return DocumentModelFunctions.bigFileUrl(doc,
                VideoInfoConstants.WEBVTT_PROPERTY, b.getFilename());
    }

    public void removeSubTitleItem(DocumentModel doc, SubtitleItem subtitleItem)
            throws PropertyException, ClientException {
        Property p = doc.getProperty(VideoInfoConstants.SUBTITLE_PROPERTY + "/"
                + subtitleItem.getPosition());
        p.remove();
        doc = documentManager.saveDocument(doc);
        refreshSubTitleItems(doc);
    }

    public String getStoryboardItemsAsJsonSettings(DocumentModel doc)
            throws PropertyException, ClientException {
        List<StoryboardItem> items = storyboardActions.getItems(doc);
        ObjectMapper o = new ObjectMapper();
        ObjectNode settings = o.createObjectNode();
        for (StoryboardItem storyboardItem : items) {
            ObjectNode thumb = o.createObjectNode();
            thumb.put("src", storyboardItem.getUrl());
            settings.put(storyboardItem.getTimecode().split("\\.")[0], thumb);
        }
        return settings.toString();
    }

    public List<TextItem> getTextItems() {
        return textItems;
    }

    public void setTextItems(List<TextItem> textItems) {
        this.textItems = textItems;
    }

    public String getTextItem() {
        return textItem;
    }

    public void setTextItem(String textItem) {
        this.textItem = textItem;
    }

    public String getTimecode() {
        return timecode;
    }

    public void setTimecode(String timecode) {
        this.timecode = timecode;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public List<SubtitleItem> getSubtitleItems() {
        return subtitleItems;
    }

    public void setSubtitleItems(List<SubtitleItem> subtitleItems) {
        this.subtitleItems = subtitleItems;
    }

}
