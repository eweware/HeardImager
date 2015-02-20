<%--
  Created by IntelliJ IDEA.
  User: ultradad
  Date: 1/8/15
  Time: 2:14 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>

<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<html>
<head>
    <title></title>
</head>
<body>
This is an upload test.  Choose an image my friend!

<form action="<%= blobstoreService.createUploadUrl("/api/image") %>" method="post" enctype="multipart/form-data">
    <input type="file" name="file"><p/>

    <input type="submit" value="Submit">
</form>

</body>
</html>
