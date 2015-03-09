<%--
  Created by IntelliJ IDEA.
  User: Dave
  Date: 3/8/2015
  Time: 8:31 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
This is an URL upload test.  Choose an image my friend!

<form action="/api/image/url" method="post">
    <input type="text" name="imageurl"><br>
    <input type="submit" value="Submit">
</form>

</body>
</html>
