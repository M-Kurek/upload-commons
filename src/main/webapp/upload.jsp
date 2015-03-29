<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
	"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Java commons-fileupload demo</title>
</head>
<body>
<div id="fileInfo">
<%
out.println("<p>" + new java.util.Date() + "</p>");
if(session.getAttribute("uploadedFile") != null) {
   out.print("<h3>Uploaded already :" + session.getAttribute("uploadedFile") +"</h3>");
}
%>
</div>
<div id="form">
	<form method="post" action="uploadFile" enctype="multipart/form-data">
		Select file to upload: 
		<input type="file" name="uploadFile" /> 
		<br/><br/> 
		<input type="submit" value="Upload" />
	</form>
</div>
</body>
</html>