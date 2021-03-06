<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<meta name="_csrf" content="${_csrf.token}"/>
    <!-- default header name is X-CSRF-TOKEN -->
<meta name="_csrf_header" content="${_csrf.headerName}"/>

<%@ include file="template/localHeader.jsp"%>

<link rel="stylesheet" href="/openmrs/moduleResources/PSI/css/magicsuggest-min.css">
<script type="text/javascript" src="/openmrs/moduleResources/PSI/js/jquery-1.10.2.js"></script>
<!-- <script type="text/javascript" src="/openmrs/moduleResources/PSI/js/magicsuggest-min.js"></script> -->
<c:url var="saveUrl" value="/module/PSI/addPSIClinicUser.form" />
<c:url var="cancelUrl" value="/module/PSI/PSIClinicUserList.form?id=${psiClinicManagementId }" />

<openmrs:require privilege="Add Clinic User" otherwise="/login.htm" />
<style>
.margin-top{
margin-top: 4px;
}
.form-content {
    padding: 2%;
    border: 1px solid #ced4da;
    margin-bottom: 1%;
}
.row {
    display: -ms-flexbox;
    display: flex;
    -ms-flex-wrap: wrap;
    flex-wrap: wrap;
    /* margin-right: -15px; */
    /* margin-left: -15px; */
}
.listItemBox {
    width: 1024px;
    padding: 2px;
    border: 1px solid lightgray;
    float: left;
    background-color: #EFEFEF;
}
.listItem {
    padding: 2px;
    float: left;
    margin: 2px;
    width: 250px;
    font-size: .9em;
    border: 1px solid lightgrey;
    background-color: white;
}
input[type="text"], input[type="password"] {
    
    border: 1px solid #c1c5c5;
    padding: 4px;
}
</style>
<%
String users = (String)session.getAttribute("users");
String userIds = (String)session.getAttribute("userIds");
//String message = (String)session.getAttribute("message");
%>
<form:form method="POST" id="userForm" action="${saveUrl}" modelAttribute="pSIClinicUser">


<div class="container register-form" style="max-width: 100%;padding: 0px; margin: 0px;">
	<div class="form">
    	<div class="note">
        	<p>Create New User</p>
       	</div>
       	 <div id="loading" style="display: none;position: absolute; z-index: 1000;margin-left:45%"> 
			<img width="50px" height="50px" src="<c:url value="/moduleResources/PSI/images/ajax-loading.gif"/>"></div>
							
		</div>
		<div style="clear: both;"></div>
		 <div id="mesage" style="color: red; font-weight: bold;display: none;position: absolute; z-index: 1000;margin-left:13%"> 
							
		</div>
		<div style="clear: both;"></div>

  		<div class="form-content">
        	<div class="row">
        		<input type="hidden" id="clinicId" name="clinicId" value="${psiClinicManagementId}">
        		<input type="hidden" id="cuid" name="cuid" value="0">
            	<table>
	            	<tr>
	            		<td>First Name<span class="required">*</span></td>
	            		<td><input type="text" name="firstName" id="firstName" class="form-control margin-top" required="required" autocomplete="off"></td>
	            	</tr>
	            	<tr>
	            		<td>Last Name<span class="required">*</span></td>
	            		<td><input type="text" name="lastName" id="lastName" class="form-control margin-top" required="required" autocomplete="off"></td>
	            	</tr>
	            	
	            	<tr>
					<td>Gender<span class="required">*</span></td>
						<td>
						
							<input type="radio" name="gender" id="M" value="M">
								<label for="M"> Male </label>
						
							<input type="radio" name="gender" id="F" value="F">
								<label for="F"> Female </label>
							<input type="radio" name="gender" id="O" value="O">
								<label for="F"> Other </label>
						</td>
					</tr>
					
					<tr>
	            		<td>Email<span class="required">*</span></td>
	            		<td><input type="email" name="email" id="email" class="form-control margin-top" required="required" autocomplete="off"></td>
	            	</tr>
	            	
	            	<tr>
	            		<td>Mobile<span class="required">*</span></td>
	            		<td><input type="text" name="mobile" id="mobile" class="form-control margin-top" required="required" autocomplete="off" pattern="01[3-9]{1}[0-9]{8}"></td>
	            	</tr>	            	
	            	
	            	
	            	<tr>
	            		<td>UserName<span class="required">*</span></td>
	            		<td><input type="text" name="userName" id="userName" class="form-control margin-top" required="required" autocomplete="off"></td>
	            	</tr>
	            	
	            	<tr>
	            		<td>Password<span class="required">*</span> </td>
	            		<td><input type="password" name="password" id="password" class="form-control margin-top" required="required" autocomplete="off"></td>
	            	</tr>
	            	
	            	<tr>
	            		<td>Retype Password<span class="required">*</span> </td>
	            		<td><input type="password" name="reTypePassword" id="reTypePassword" class="form-control margin-top" required="required" autocomplete="off"></td>
	            	</tr>
					<tr>
						<td valign="top">Roles <span class="required">*</span> </td>
						<td valign="top">							
						<div id="roleStrings" class="listItemBox">
							<c:forEach items="${roles}" var="role">	
							<span class="listItem"><input type="checkbox" name="roleStrings" class="roles" id="roleStrings.${role.role }" value="${role.role }">
							<label for="roleStrings.${role.role }">${role.role }</label></span>
							</c:forEach>
						</div>
						</td>
					</tr>
					
            	</table>
          	</div>
          	<div class="row" style="    margin-left: 558px; margin-top: 5px;">
          	
          	<input type="submit" class="btnSubmit" onclick="return Validate()" value="Submit"/> <a href="${cancelUrl}">Back</a>
          	
          	</div>
      	</div>
   	</div>
</div>       
	

</form:form>

<%-- <script type="text/javascript">  
	var $jq = jQuery.noConflict();
	 $jq('#userIds').magicSuggest({ 		 	
		 	allowFreeEntries: false,
		 	required: true,
			//placeholder: 'Type Locations',
     		data: <%=users%>,
	        valueField: 'username',
	        displayField: 'display',
	        name: 'usernames',
	        inputCfg: {"class":"magicInput"},
	        value: <%=userIds%>,
	        useCommaKey: true,	        
	        maxSelection: 1,	        
	        maxEntryLength: 100,
	 		maxEntryRenderer: function(v) {
	 			return '<div style="color:red">Typed Word TOO LONG </div>';
	 		}	       
	  });
  </script> --%>
<script type="text/javascript" src="/openmrs/moduleResources/PSI/js/user.js"></script>
<%@ include file="/WEB-INF/template/footer.jsp"%>