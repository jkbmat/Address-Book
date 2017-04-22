<%-- 
    Document   : test
    Created on : Apr 22, 2014, 11:01:42 AM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>AddressBook</title>
        <link rel="shortcut icon" href="./img/favicon.png" />
        <link rel="stylesheet" type="text/css" href="./css/tooltipster.css" />
        <link rel="stylesheet" type="text/css" href="./css/tooltipster-addressbook.css" />
        <link href='http://fonts.googleapis.com/css?family=Source+Sans+Pro|Roboto:400,900,700&subset=latin,latin-ext,cyrillic-ext,greek-ext,vietnamese,cyrillic,greek' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" href="./css/style.css" />
        <script src='./js/jquery-2.1.0.min.js'></script>
        <script src='./js/jquery.easing.1.3.js'></script>
        <script type="text/javascript" src="./js/jquery.tooltipster.min.js"></script>
        <script src='./js/script.js'></script>
    </head>
    <body>
        <div id="wrapper">
            <h1 id="header">AddressBook</h1>
            <div id='panel'>
                <form method="post" id='adder'>
                    <strong>Add a new person:&nbsp;&nbsp;</strong>
                    <input type="text" name='firstname' placeholder="First name">
                    <input type="text" name='nickname' placeholder="Nick name">
                    <input type="text" name='lastname' placeholder="Last name">
                    <input type='submit' name='submit' value='add' class='add'>
                </form>
            </div>
            <div id="content">
                <c:if test='${not empty message}'>
                    <div id='message'>
                        <c:out value='${message}' />
                    </div>
                </c:if>
                <ul id='list'>
                <c:forEach items="${contacts}" var="j">
                    <li class='contact' pid='${j.key}'>
                        <h2 class='name'>
                            <span class='firstname'><c:out value="${j.value.firstname}"/></span>
                        
                            <span class="nickname"><c:if test='${not empty j.value.nickname}'>"<c:out value="${j.value.nickname}"/>"</c:if></span>
                            <span class='lastname'><c:out value="${j.value.lastname}"/></span>
                            &nbsp;<a title="Edit name" href="javascript:editp(${j.key})" pid='${j.key}' class='editbutton tooltip'>
                                <img src="./img/edit.png" width="16" height="16" />
                            </a>
                        </h2>    
                        
                        <div class="right">
                            
                            <form method='post'>
                                <input type="hidden" name='pid' value="${j.key}">
                                <input title="Delete this person and all their contacts" type="submit" name='submit' value='delete' class='delete tooltip'>
                            </form>
                            <br><br>
                            <form method="post">
                                <input type="text" name="ctype" placeholder="Contact type">
                                <input type="text" name="cval" placeholder="Contact value">
                                <input type="hidden" name="pid" value="${j.key}">
                                <input type="submit" name="submit" value="addc" class="addc tooltip" title="Add contact">
                            </form>
                        </div>
                            
                        <br><br>
                        <div class="contacts">
                            <c:forEach items="${j.value.contacts}" var="cont">
                                <span class='contact' cid='${cont.key}'><span class='ckey'><c:out value="${cont.value[0]}"/></span>: <span class='cval'><c:out value="${cont.value[1]}"/></span>
                                &nbsp;&nbsp;
                                <a class='editbutton tooltip' title="Edit contact" cid='${cont.key}' href="javascript:editc(${cont.key})"><img src='./img/edit.png'></a>
                                &nbsp;&nbsp;
                                <form method="post">
                                    <input type="hidden" name="cid" value="${cont.key}">
                                    <input type="submit" name='submit' class="delete tooltip" value="delc" title="Delete contact">
                                </form></span><br>
                            </c:forEach>
                        </div>
                    </li>    
                </c:forEach>
                </ul>
            </div>
        </div>
    </body>
</html>
