package org.gbif.imgcache;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ImageCacheServlet extends HttpServlet {

  private final Logger LOG = LoggerFactory.getLogger(ImageCacheServlet.class);
  private static final long serialVersionUID = 8681716273998041332L;
  private final ImageCacheService cache;
  private static final String SIZE_PARAM = "size";
  private static final String URL_PARAM = "url";


  @Inject
  public ImageCacheServlet(ImageCacheService cache) {
    this.cache = cache;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final ImageSize size = ImageSize.fromString(req.getParameter(SIZE_PARAM));

    try {
      URL url = new URL(req.getParameter(URL_PARAM));
      try {
        CachedImage img = cache.get(url, size);
        resp.setHeader(HttpHeaders.CONTENT_TYPE, img.getMimeType());
        resp.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=604800");
        ByteStreams.copy(img.openStream(), resp.getOutputStream());
      } catch (IOException e) {
        String errMsg = String.format("No image found for url %s ", url);
        LOG.warn(errMsg);
        LOG.debug(errMsg, e);
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, errMsg);
      } catch (NullPointerException e) {
        String errMsg = String.format("Likely unsupported format, or not an image %s ", url);
        LOG.warn(errMsg);
        LOG.debug(errMsg, e);
        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
      } finally {
        resp.flushBuffer();
      }

    } catch (Exception e) {
      LOG.error("Invalid image url requested", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Please provide a valid image url parameter");
    }
  }
}
