package org.gbif.imgcache.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.gbif.imgcache.ImageCacheHdfsService;

import java.util.Properties;

public class PrivateCacheModule extends PrivateModule{
  private final Properties properties;

  public PrivateCacheModule(Properties properties) {
    this.properties = properties;
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), properties);

    System.out.println(properties);
    bind(ImageCacheHdfsService.class).in(Scopes.SINGLETON);

    expose(ImageCacheHdfsService.class);
  }

}
