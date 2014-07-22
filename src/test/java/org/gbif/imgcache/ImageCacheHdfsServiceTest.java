package org.gbif.imgcache;

import org.junit.Test;

import java.net.URL;

public class ImageCacheHdfsServiceTest {

    @Test
    public void testUrlToHdfs() throws Exception {
        ImageCacheHdfsService service = new ImageCacheHdfsService("hdfs://c1n1.gbif.org:8020", "/tmp/imgcache");
        for (String u : new String[]{"http://www.gbif.org/logo.png", "http://www.gbif.org/image?format=png&id=1324"}) {
            URL url = new URL(u);
            System.out.println(service.urlToHdfs(url));
        }
    }
}