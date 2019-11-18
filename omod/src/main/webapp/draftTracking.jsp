<%@ include file="/WEB-INF/template/include.jsp"%>
<div class="form-content" id="draftTracking"> </div>
<div class="form-content">
	<div class="row">
		<div class="col-md-6">
			<div class="form-group">
                   <label> ${ no_slip_draft} </label> &nbsp;&nbsp; No of Slips in Draft
               </div>
		</div>
		<div class="col-md-6">
			<div class="form-group">
                   <label> ${ total_payable_draft} </label> &nbsp;&nbsp; Total Payable in Draft
               </div>
		</div>
	</div>
</div>
<div style="overflow:auto;">
<br/>
	<table id="draft_tracking" class="display">
		<thead>
            <tr>
                 <th>SL</th> 
                 <th>Slip No.</th>
                 <th>Date</th>
                 <th>Patient Name</th>
                <th>Phone</th>
                <th>Wealth Class</th>
                <th>Service Point</th>
                <th>Total Amount</th>
                <th>Discount</th>
                <th>Payable Amount</th>
               <!--  <th>Action</th>  -->
            </tr>
        </thead>
        <tbody>
          <% int sl_d = 0; %>
        	<c:if test="${not empty draftReport }">
				<c:forEach var="report" items="${ draftReport }">
			        <tr>
			        	
			        	<td><%=++sl_d%></td>
			              <td>${ report.slip_no }</td>	             
			        	 <td>${ report.slip_date }</td> 
			        	 <td>${ report.patient_name }</td>
			            <td>${ report.phone }</td>
			            <td>${ report.wealth_classification }</td>
			            <td>${ report.service_point }</td>
			            <td>${ report.total_amount }</td>
			            <td>${ report.discount }</td>
			            <td>${ report.net_payable }</td>
			            <%-- <td>${ report.slip_link }</td> --%>  
			        </tr>
		       </c:forEach>
		    </c:if>
        </tbody>
	</table>
</div>