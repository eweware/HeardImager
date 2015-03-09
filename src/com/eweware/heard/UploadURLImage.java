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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Dave on 3/8/2015.
 */

public class UploadURLImage extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        HttpSession session = request.getSession();
        String imageURL = request.getParameter("imageurl");

        if (imageURL != null) {
            URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();

            HTTPResponse fetchResponse = fetchService.fetch(new URL(imageURL));

            String fetchResponseContentType = null;
            for (HTTPHeader header : fetchResponse.getHeaders()) {
                // For each request header, check whether the name equals
                // "Content-Type"; if so, store the value of this header
                // in a member variable
                if (header.getName().equalsIgnoreCase("content-type")) {
                    fetchResponseContentType = header.getValue();
                    break;
                }
            }

            if (fetchResponseContentType != null) {
                String attachmentName = "file";
                String attachmentFileName = imageURL.substring(imageURL.lastIndexOf("/")+1);
                String crlf = "\r\n";
                String twoHyphens = "--";
                String boundary =  "*****";

                // ok, we have it!  Now upload that sucker
                String submissionURLstr = blobstoreService.createUploadUrl("/api/image");
                URL submitURL = new URL(submissionURLstr);

                HttpURLConnection connection = (HttpURLConnection) submitURL.openConnection();
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);


                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

                dataOutputStream.writeBytes(twoHyphens + boundary + crlf);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
                dataOutputStream.writeBytes(crlf);

                // convert the file
                dataOutputStream.write(fetchResponse.getContent());

                dataOutputStream.writeBytes(crlf);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                dataOutputStream.flush();
                dataOutputStream.close();

                // now get the response
                InputStream responseStream = new BufferedInputStream(connection.getInputStream());
                String finalURL = "";


                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(responseStreamReader.readLine());
                responseStreamReader.close();

                finalURL = stringBuilder.toString();

                if (connection.getResponseCode() != 200) {
                    finalURL = Integer.toString(connection.getResponseCode()) + " - " + connection.getResponseMessage() + " - " +  finalURL;
                }

                PrintWriter out = response.getWriter();
                out.write(finalURL);
                out.flush();
                out.close();

            }

        }



    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
