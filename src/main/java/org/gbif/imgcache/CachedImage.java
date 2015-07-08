package org.gbif.imgcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.google.common.io.ByteSource;
import com.google.common.io.InputSupplier;

public class CachedImage extends ByteSource {
  private final URL source;
  private final ImageSize size;
  private final String mimeType;
  private final File location;

  public CachedImage(URL source, ImageSize size, String mimeType, File location) {
    this.source = source;
    this.size = size;
    this.mimeType = mimeType;
    this.location = location;
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

  @Override
  public InputStream openStream() throws IOException {
    try {
      return new FileInputStream(location);
    } catch (FileNotFoundException e) {
      throw new IOException("Can't open cached image at " + location);
    }
  }
}
