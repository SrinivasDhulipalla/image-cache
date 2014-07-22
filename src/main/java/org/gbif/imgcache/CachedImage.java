package org.gbif.imgcache;

import com.google.common.io.InputSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CachedImage implements InputSupplier<InputStream> {
    private final URL source;
    private final ImageSize size;
    private final String mimeType;
    private final byte[] image;

    public CachedImage(URL source, ImageSize size, String mimeType, byte[] image) {
        this.source = source;
        this.size = size;
        this.mimeType = mimeType;
        this.image = image;
    }

    @Override
    public InputStream getInput() throws IOException {
        return new ByteArrayInputStream(image);
    }

    public ImageSize getSize() {
        return size;
    }

    public URL getSource() {
        return source;
    }

    public String getMimeType() {
        return mimeType;
    }

}
