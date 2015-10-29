#image-cache


Provides a simple caching mechanism for image files (png,jpg,etc.), the exposed service receives an URL as parameter and
caches its content for subsequent requests for that resource. Additionally, it creates several sizes of the image that 
can be requested in different contexts in the GBIF portal, the image versions stored in the cache are:
  * ORIGINAL
  * LARGE: 1024x768 px
  * MIDSIZE: 627x442 px
  * SMALL: 230x172 px
  * THUMBNAIL: 100x100 px

##To build the project

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
The files application.properties and logback.xml are excluded from the target jar file using the Maven Shade plugin.

##How to run this service

This service is based on the [gbif-microservice](https://github.com/gbif/gbif-microservice) project which means that the
jar file can be executed using the parameters described in the [gbif-microservice](https://github.com/gbif/gbif-microservice)
project; additionally, this service requires a logback.xml file in the `conf` directory located at same level where the
jar file (such directory is added to the application classpath).
