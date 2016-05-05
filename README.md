# Image Cache

Image Cache provides a simple caching mechanism for image files.  The exposed service receives a URL as a parameter and
caches its content for subsequent requests for that resource.  It creates several sizes of the image that 
can be requested in different contexts in the GBIF portal.  The image versions stored in the cache are:
  * ORIGINAL
  * LARGE: 1024×768 px
  * MIDSIZE: 627×442 px
  * SMALL: 230×172 px
  * THUMBNAIL: 100×100 px, cropped to the centre if the original isn't square

Image format support is that supported by `[javax.imageio](https://docs.oracle.com/javase/8/docs/api/javax/imageio/package-summary.html#package.description)`.
By default, that includes JPEG, PNG, BMP, WBMP and GIF support.  TIFF support has been added using the [TwelveMonkey's ImageIO library](http://haraldk.github.io/TwelveMonkeys/).

## To build the project

This project requires only one setting `imgcache.repository` which should contain the path where the cached files 
are stored. The profile below can be used to run the service locally:
```
    <profile>
      <id>tmp</id>
      <properties>
        <imgcache.repository>/tmp/image-cache</imgcache.repository>
      </properties>
    </profile>
```

To build the project use the Maven command:    
```
mvn clean package install
```

The files `application.properties` and `logback.xml` are excluded from the target JAR file using the Maven Shade plugin.

## How to run this service

This service is based on the [gbif-microservice](https://github.com/gbif/gbif-microservice) project which means that the
JAR file can be executed using the parameters described in the [gbif-microservice](https://github.com/gbif/gbif-microservice)
project.  Additionally, this service requires a `logback.xml` file in the `conf` directory located at same level where the
JAR file (such directory is added to the application classpath).
