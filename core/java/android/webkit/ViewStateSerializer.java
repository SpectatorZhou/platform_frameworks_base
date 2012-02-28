/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.webkit;

import android.graphics.Point;
import android.graphics.Region;
import android.webkit.WebViewCore.DrawData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @hide
 */
class ViewStateSerializer {

    private static final int WORKING_STREAM_STORAGE = 16 * 1024;

    static final int VERSION = 1;

    static boolean serializeViewState(OutputStream stream, WebViewClassic web)
            throws IOException {
        int baseLayer = web.getBaseLayer();
        if (baseLayer == 0) {
            return false;
        }
        DataOutputStream dos = new DataOutputStream(stream);
        dos.writeInt(VERSION);
        dos.writeInt(web.getContentWidth());
        dos.writeInt(web.getContentHeight());
        return nativeSerializeViewState(baseLayer, dos,
                new byte[WORKING_STREAM_STORAGE]);
    }

    static DrawData deserializeViewState(InputStream stream, WebViewClassic web)
            throws IOException {
        DataInputStream dis = new DataInputStream(stream);
        int version = dis.readInt();
        if (version != VERSION) {
            throw new IOException("Unexpected version: " + version);
        }
        int contentWidth = dis.readInt();
        int contentHeight = dis.readInt();
        int baseLayer = nativeDeserializeViewState(dis,
                new byte[WORKING_STREAM_STORAGE]);

        final WebViewCore.DrawData draw = new WebViewCore.DrawData();
        draw.mViewState = new WebViewCore.ViewState();
        int viewWidth = web.getViewWidth();
        int viewHeight = web.getViewHeightWithTitle() - web.getTitleHeight();
        draw.mViewSize = new Point(viewWidth, viewHeight);
        draw.mContentSize = new Point(contentWidth, contentHeight);
        draw.mViewState.mDefaultScale = web.getDefaultZoomScale();
        draw.mBaseLayer = baseLayer;
        draw.mInvalRegion = new Region(0, 0, contentWidth, contentHeight);
        return draw;
    }

    private static native boolean nativeSerializeViewState(int baseLayer,
            OutputStream stream, byte[] storage);

    // Returns a pointer to the BaseLayer
    private static native int nativeDeserializeViewState(
            InputStream stream, byte[] storage);

    private ViewStateSerializer() {}
}
