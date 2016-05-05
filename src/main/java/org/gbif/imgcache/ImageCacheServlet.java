package org.gbif.imgcache;

import java.io.IOException;
import java.net.MalformedURLException;
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

    URL url = null;
    try {
      LOG.debug("URL parameter is {}", req.getParameter(URL_PARAM));
      url = new URL(req.getParameter(URL_PARAM));

      CachedImage img = cache.get(url, size);
      resp.setHeader(HttpHeaders.CONTENT_TYPE, img.getMimeType());
      resp.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000");
      ByteStreams.copy(img.openStream(), resp.getOutputStream());
    } catch (MalformedURLException e) {
      String errMsg = String.format("Invalid image URL requested %s", req.getParameter(URL_PARAM));
      LOG.warn(errMsg);
      LOG.debug(errMsg, e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, errMsg);
    } catch (IOException e) {
      String errMsg = String.format("No image found for url %s", url);
      LOG.warn(errMsg);
      LOG.debug(errMsg, e);
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, errMsg);
    } catch (NullPointerException e) {
      String errMsg = String.format("Likely unsupported format, or not an image %s", url);
      LOG.warn(errMsg);
      LOG.debug(errMsg, e);
      resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
    } catch (Exception e) {
      String errMsg = String.format("Unknown error processing URL %s", url);
      LOG.error(errMsg, e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errMsg);
    } finally {
      resp.flushBuffer();
    }
  }
}
