package org.openmrs.module.PSI.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.module.PSI.PSIServiceProvision;
import org.openmrs.module.PSI.dto.AUHCComprehensiveReport;
import org.openmrs.module.PSI.dto.AUHCDashboardCard;
import org.openmrs.module.PSI.dto.AUHCDraftTrackingReport;
import org.openmrs.module.PSI.dto.AUHCRegistrationReport;
import org.openmrs.module.PSI.dto.AUHCVisitReport;
import org.openmrs.module.PSI.dto.DashboardDTO;
import org.openmrs.module.PSI.dto.PSIReport;
import org.openmrs.module.PSI.dto.PSIReportSlipTracking;
import org.openmrs.module.PSI.dto.SearchFilterDraftTracking;
import org.openmrs.module.PSI.dto.SearchFilterRegistrationReport;
import org.openmrs.module.PSI.dto.SearchFilterReport;
import org.openmrs.module.PSI.dto.SearchFilterSlipTracking;
import org.openmrs.module.PSI.dto.SearchFilterVisitReport;

public interface PSIServiceProvisionDAO {
	
	public PSIServiceProvision saveOrUpdate(PSIServiceProvision psiServiceProvision);
	
	public List<PSIServiceProvision> getAll();
	
	public List<PSIServiceProvision> getAllByPatient(String patientUuid);
	
	public PSIServiceProvision findById(int id);
	
	public List<PSIServiceProvision> getAllBetweenDateAndPatient(Date start, Date end, String patientUuid);
	
	public List<PSIServiceProvision> getAllByDateAndPatient(Date date, String patientUuid);
	
	public List<PSIServiceProvision> findAllByTimestamp(long timestamp);
	
	public List<PSIReport> servicePointWiseReport(String startDate, String endDate, String code);
	
	public String servicePointWiseRepor(String startDate, String endDate, String code);
	
	public List<PSIReport> serviceProviderWiseReport(String startDate, String endDate, String code, String dataCollector);
	
	public DashboardDTO dashboardReport(String start, String end, String code, String dataCollector);
	
	public List<PSIServiceProvision> findAllByTimestampNotSending(long timestamp);
	
	public void delete(int id);
	
	public List<PSIServiceProvision> findAllResend();
	
	public List<PSIServiceProvision> findAllByMoneyReceiptId(int moneyReceiptId);
	
	public void deleteByPatientUuidAndMoneyReceiptIdNull(String patientUuid);
	
	public String getTotalDiscount(String startDate,String endDate);
	
	public String getTotalDiscount(SearchFilterSlipTracking filter);
	
	public String getTotalServiceContact(String startDate,String endDate);
	
	public String getTotalServiceContact(SearchFilterSlipTracking filter);
	
	public List<PSIReportSlipTracking> getSlipTrackingReport(SearchFilterSlipTracking filter);
	
	public List<Object[]> getSlip(SearchFilterSlipTracking filter);
	
	public List<AUHCDraftTrackingReport> getDraftTrackingReport(SearchFilterDraftTracking filter);
	
	public String getNoOfDraft(String startDate,String endDate);
	
	
	public String getTotalPayableDraft(String startDate,String endDate);
	
	public String getTotalPayableDraft(SearchFilterDraftTracking filter);
	
	public String getDashboardNewReg(String startDate,String endDate);
	
	public String getDashboardOldClients(String startDate,String endDate);
	public String getDashboardOldClients(String startDate, String endDate,
			String clinicCode, String gender);
	
	public String getDashboardNewClients(String startDate,String endDate);
	
	public List<AUHCComprehensiveReport> getComprehensiveReport(SearchFilterReport filter);	
	
	public List<AUHCRegistrationReport> getRegistrationReport(SearchFilterReport filter);
	
	public String getPatientsServed(SearchFilterSlipTracking filter);
	
	public String getRevenueEarned(SearchFilterSlipTracking filter);
	
	public List<AUHCRegistrationReport> getRegistrationReport
		(String startDate,String endDate,String gender,String code);
	
	public List<AUHCVisitReport> getVisitReport(String startDate,String endDate,String code);
	
	public AUHCDashboardCard getComprehensiveDashboardCard(List<AUHCComprehensiveReport> report,
			SearchFilterReport filter);
	
	public String oldClientCount(String startDate,String endDate,String code);
	
	public String newClientCount(String startDate,String endDate,String code);
	public String getDashboardNewClients(String startDate, String endDate,String code,String gender);
	
	public String patientsServed(String startDate,String endDate,String code,String category);
	
	public String newRegistration(String startDate,String endDate,String code);
	
	public AUHCDashboardCard getProviderDashboardCard(List<PSIReport> report,
			SearchFilterReport filter);
	
	public String totalServiceContact(String startDate,String endDate,String clinicCode,
			String provider);
	
	public String patientsServedProvider(String startDate,String endDate,String code,String collector);
	public String discountOnProvider(String startDate,String endDate,String code,String collector);
	
	public String totalServiceContact(SearchFilterSlipTracking filter);
	
	public String totalServiceContact(String startDate,String endDate, String code);
	
	public List<AUHCVisitReport> getVisitReport(SearchFilterVisitReport filter);
	
	public String newRegistration(SearchFilterVisitReport filter);
	
	public String oldClientCount(SearchFilterVisitReport filter);
	
	public String newClientCount(SearchFilterVisitReport filter);
	
	public String totalServiceContact(SearchFilterVisitReport filter);
	
	public List<AUHCRegistrationReport> getRegistrationReport
		(SearchFilterRegistrationReport filter);
	
	public String getDashboardOldClients(SearchFilterRegistrationReport filter);
	
	public String getDashboardNewClients(SearchFilterRegistrationReport filter);
	
	 public String oldClientCount(String startDate,String endDate,String code,String collector);
	 
	 public String newClientCount(String startDate,String endDate,String code,String collector);
	 
//	 public String newRegistration(String startDate,String endDate,String code,String collector);
	 
	
	
}
