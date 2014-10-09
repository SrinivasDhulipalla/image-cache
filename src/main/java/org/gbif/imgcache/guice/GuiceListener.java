package org.gbif.imgcache.guice;

import org.gbif.imgcache.ImageCacheServlet;
import org.gbif.utils.file.properties.PropertiesUtil;
import org.gbif.ws.app.ConfUtils;

import java.io.IOException;
import java.util.Properties;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sets up the rendering Servlet, and the Cube injection.
 * This can operate in 2 modes:
 * i) Using a memory cube, that reads from a GZipped CSV file with lat,lng,count (for local testing)
 * ii) Using an HBase backed cube (for real operation)
 */
public class GuiceListener extends GuiceServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(GuiceListener.class);
  private static final String APPLICATION_PROPERTIES = "application.properties";
  private static final String ERROR_MSG = "Error initiating web application";

  private final ServletModule sm = new ServletModule() {

    @Override
    protected void configureServlets() {
      serve("*").with(ImageCacheServlet.class);
    }
  };

  @Override
  protected Injector getInjector() {
    try {
      return Guice.createInjector(sm,
                                  new PrivateCacheModule(PropertiesUtil.readFromFile(ConfUtils.getAppConfFile(
                                    APPLICATION_PROPERTIES))));
    } catch (IOException ex) {
      LOG.error(ERROR_MSG, ex);
      Throwables.propagate(ex);
    }
    throw new IllegalStateException(ERROR_MSG);
  }

}
