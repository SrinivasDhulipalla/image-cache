package org.gbif.imgcache.guice;

import org.gbif.imgcache.ImageCacheService;

import java.util.Properties;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateCacheModule extends PrivateModule {
  private static final Logger LOG = LoggerFactory.getLogger(PrivateCacheModule.class);

  private final Properties properties;

  public PrivateCacheModule(Properties properties) {
    this.properties = properties;
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), properties);

    LOG.info("Properties: {}", properties);
    bind(ImageCacheService.class).in(Scopes.SINGLETON);

    expose(ImageCacheService.class);
  }

}
