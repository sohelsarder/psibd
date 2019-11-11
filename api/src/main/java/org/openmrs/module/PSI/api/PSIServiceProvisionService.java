package org.openmrs.module.PSI.api;

import java.util.Date;
import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.PSI.PSIServiceProvision;
import org.openmrs.module.PSI.dto.AUHCDraftTrackingReport;
import org.openmrs.module.PSI.dto.DashboardDTO;
import org.openmrs.module.PSI.dto.PSIReport;
import org.openmrs.module.PSI.dto.PSIReportSlipTracking;
import org.openmrs.module.PSI.dto.SearchFilterDraftTracking;
import org.openmrs.module.PSI.dto.SearchFilterSlipTracking;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PSIServiceProvisionService extends OpenmrsService {
	
	public PSIServiceProvision saveOrUpdate(PSIServiceProvision psiServiceProvision);
	
	public List<PSIServiceProvision> getAll();
	
	public List<PSIServiceProvision> getAllByPatient(String patientUuid);
	
	public PSIServiceProvision findById(int id);
	
	public List<PSIServiceProvision> getAllBetweenDateAndPatient(Date start, Date end, String patientUuid);
	
	public List<PSIServiceProvision> getAllByDateAndPatient(Date date, String patientUuid);
	
	public List<PSIServiceProvision> findAllByTimestamp(long timestamp);
	
	public List<PSIServiceProvision> findAllByTimestampNotSending(long timestamp);
	
	public List<PSIReport> servicePointWiseReport(String startDate, String endDate, String code);
	
	public String servicePointWiseRepor(String startDate, String endDate, String code);
	
	public List<PSIReport> serviceProviderWiseReport(String startDate, String endDate, String code, String dataCollector);
	
	public DashboardDTO dashboardReport(String start, String end, String code, String dataCollector);
	
	public void delete(int id);
	
	public List<PSIServiceProvision> findAllResend();
	
	public List<PSIServiceProvision> findAllByMoneyReceiptId(int moneyReceiptId);
	
	public void deleteByPatientUuidAndMoneyReceiptIdNull(String patientUuid);
	
	public String getTotalDiscount(String startDate,String endDate);
	
	public String getTotalServiceContract(String startDate,String endDate);
	
	public List<PSIReportSlipTracking> getSlipTrackingReport
			(SearchFilterSlipTracking filter);
	
	public List<Object[]> getSlip(SearchFilterSlipTracking filter);
	
	public String getNoOfDraft(String startDate, String endDate);
	
	public String getTotalPayableDraft(String startDate, String endDate);
	
	public List<AUHCDraftTrackingReport> getDraft(SearchFilterDraftTracking filter);
	
}
