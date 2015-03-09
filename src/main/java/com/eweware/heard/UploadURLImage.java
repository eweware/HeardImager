package com.eweware.heard;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.cloudstorage.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Dave on 3/8/2015.
 */

public class UploadURLImage extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private final GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        HttpSession session = request.getSession();
        String imageURL = request.getParameter("imageurl");
        String finalURL = "";

        if (imageURL != null) {
            URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();

            HTTPResponse fetchResponse = fetchService.fetch(new URL(imageURL));

            String fetchResponseContentType = null;
            for (HTTPHeader header : fetchResponse.getHeaders()) {
                if (header.getName().equalsIgnoreCase("content-type")) {
                    fetchResponseContentType = header.getValue();
                    break;
                }
            }

            if (fetchResponseContentType != null) {
                final String uniqueSym = UUID.randomUUID().toString();
                final GcsFilename fileName = new GcsFilename("heardimages", uniqueSym);
                GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, GcsFileOptions.getDefaultInstance());
                outputChannel.write( ByteBuffer.wrap(fetchResponse.getContent()));
                outputChannel.close();

                GcsFileMetadata metadata = gcsService.getMetadata(fileName);
                long theFileSize = metadata.getLength();

                if (theFileSize > 0) {
                    // now get a serving URL
                    ImagesService imagesService = ImagesServiceFactory.getImagesService();
                    String plainFileName = String.format("/gs/%s/%s", fileName.getBucketName(), fileName.getObjectName());
                    ServingUrlOptions servingOptions = ServingUrlOptions.Builder.withGoogleStorageFileName(plainFileName);
                    finalURL = imagesService.getServingUrl(servingOptions);
                }
            }
        }

        PrintWriter out = response.getWriter();
        out.write(finalURL);
        out.flush();
        out.close();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
