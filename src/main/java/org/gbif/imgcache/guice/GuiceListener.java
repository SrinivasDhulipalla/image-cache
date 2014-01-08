package org.gbif.imgcache.guice;

import org.gbif.imgcache.ImageCacheServlet;

import java.io.IOException;
import java.util.Properties;

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
  private static final String APPLICATION_PROPERTIES = "/application.properties";

  private final ServletModule sm = new ServletModule() {

    @Override
    protected void configureServlets() {
      serve("*").with(ImageCacheServlet.class);
    }
  };

  @Override
  protected Injector getInjector() {
    Properties p = new Properties();
    try {
      p.load(GuiceListener.class.getResourceAsStream(APPLICATION_PROPERTIES));
    } catch (IOException e) {
      LOG.error("Error initiating web application", e);
      throw new IllegalArgumentException(APPLICATION_PROPERTIES + " is missing");
    }

    // new InstrumentationModule(), new MetricsModule(p)
    return Guice.createInjector(sm, new PrivateCacheModule(p));
  }

}
