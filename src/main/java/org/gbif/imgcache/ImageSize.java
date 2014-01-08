package org.gbif.imgcache;

import com.google.common.base.Strings;

public enum ImageSize {
  ORIGINAL(-1, -1),
  LARGE(1024, 768),
  MIDSIZE(627, 442),
  SMALL(230, 172),
  THUMBNAIL(100, 100);

  public final int width;
  public final int height;

  private ImageSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public static ImageSize fromString(String x) {
    // default to thumbnail
    if (!Strings.isNullOrEmpty(x)) {
      char c = x.toUpperCase().trim().charAt(0);
      for (ImageSize is : values()) {
        if (is.name().charAt(0) == c) {
          return is;
        }
      }
    }
    return ImageSize.THUMBNAIL;
  }

}
