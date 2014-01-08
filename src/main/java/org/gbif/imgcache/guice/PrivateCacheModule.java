package org.gbif.imgcache.guice;

import org.gbif.imgcache.ImageCacheService;

import java.util.Properties;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class PrivateCacheModule extends PrivateModule{
  private final Properties properties;

  public PrivateCacheModule(Properties properties) {
    this.properties = properties;
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), properties);

    System.out.println(properties);
    bind(ImageCacheService.class).in(Scopes.SINGLETON);

    expose(ImageCacheService.class);
  }

}
