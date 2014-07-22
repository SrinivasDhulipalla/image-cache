package org.gbif.imgcache;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.inject.name.Named;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.net.*;

public class ImageCacheHdfsService {

    private static final Logger LOG = LoggerFactory.getLogger(ImageCacheHdfsService.class);

    private static final String ENC = "UTF8";
    private static final String PNG_FMT = "png";
    private static final String MIME_TYPE = "image/" + PNG_FMT;
    private static final String DFT_FILENAME = "image";
    private static final String HEAD_METHOD = "HEAD";
    private static final int TIMEOUT_MS = 2 * 60 * 1000;  // 2 minutes
    private static final int CONNECT_TIMEOUT_MS = 30 * 1000;  // 30 seconds

    private final Configuration conf;
    private final String hdfsImgDir;
    private final FileSystem hdfs;

    @Inject
    public ImageCacheHdfsService(@Named("hdfs.namenode") String nameNode, @Named("hdfs.imgdir") String hdfsImgDir) throws IOException {
        LOG.info("Use image repository {}", hdfsImgDir);
        this.hdfsImgDir = hdfsImgDir;
        conf = new Configuration();
        conf.set(CommonConfigurationKeys.FS_DEFAULT_NAME_KEY, nameNode);
        hdfs = FileSystem.get(conf);
    }

    public CachedImage get(URL url, ImageSize size) throws IOException {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(size);

        Path p = urlToHdfs(url);
        if (!hdfs.exists(p)) {
            return new CachedImage(url, size, MIME_TYPE, cacheImage(url, size));

        } else {
            IntWritable key = new IntWritable();
            BytesWritable image = new BytesWritable();
            SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(p));
            try {
                while (reader.next(key, image)) {
                    // correct image size?
                    if (size.ordinal() == key.get()) {
                        // TODO: store original mime type or deduct from image/file suffix
                        return new CachedImage(url, size, MIME_TYPE, image.getBytes());
                    }
                }

            } finally {
                reader.close();
            }
            throw new IOException("Image "+url + " not properly cached for size " + size);
        }
    }

    private byte[] cacheImage(URL url, ImageSize returnSize) throws IOException {
        LOG.info("Caching image {}", url);
        if (exists(url)) {
            // read original image into memory
            final byte[] image = readOriginal(url);
            byte[] requested = new byte[0];

            // start with smallest thumbnail then go to larger files for quicker read access
            final Path path = urlToHdfs(url);
            IntWritable key = new IntWritable();
            SequenceFile.Writer writer = SequenceFile.createWriter(conf, SequenceFile.Writer.file(path),
                SequenceFile.Writer.compression(SequenceFile.CompressionType.NONE),
                SequenceFile.Writer.keyClass(IntWritable.class),
                SequenceFile.Writer.valueClass(BytesWritable.class));
            try {
                for (ImageSize size : ImageSize.values()) {
                    key.set(size.ordinal());
                    byte[] data = processImage(image, size);
                    BytesWritable value = new BytesWritable(data);
                    writer.append(key, value);

                    if (returnSize == size) {
                        requested = data;
                    }
                }
            } finally {
                IOUtils.closeStream(writer);
            }
            return requested;

        } else {
            String errMsg = String.format("Requested file doesn't exist %s", url);
            throw new IOException(errMsg);
        }
    }

    /**
     * Safely reads the original image file into a byte array horing connection timeouts and closing streams.
     */
    private byte[] readOriginal(URL url) throws IOException {
        InputStream source = null;
        Closer closer = Closer.create();
        try {
            URLConnection con = url.openConnection();
            con.setConnectTimeout(CONNECT_TIMEOUT_MS);
            con.setReadTimeout(TIMEOUT_MS);
            source = closer.register(con.getInputStream());
            return ByteStreams.toByteArray(source);

        } finally {
            closer.close();
        }
    }

    /**
     * Checks if the remote URL exists.
     */
    private boolean exists(URL url) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(HEAD_METHOD);
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            LOG.error(String.format("Error getting file %s", url), e);
            return false;
        }
    }

    public Path urlToHdfs(URL url) {
        try {
            String authority = url.getAuthority();
            return new Path(hdfsImgDir, authority + "/" + URLEncoder.encode(url.toString(), ENC));

        } catch (UnsupportedEncodingException e) {
            LOG.error("Error building path for image {}", url, e);
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Creates a byte array representation of a given image size.
     */
    private byte[] processImage(byte[] original, ImageSize size) throws IOException {
        if (ImageSize.ORIGINAL == size) {
            return original;
        }
        Thumbnails.Builder<?> thumb = Thumbnails.of(new ByteArrayInputStream(original))
            .size(size.width, size.height)
            .outputFormat(PNG_FMT);

        // make sure thumbnails have the same square size
        if (ImageSize.THUMBNAIL == size) {
            thumb.keepAspectRatio(false);
        }

        // process
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumb.toOutputStream(bytes);
        return bytes.toByteArray();
    }
}
