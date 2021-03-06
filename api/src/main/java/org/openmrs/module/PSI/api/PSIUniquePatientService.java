package org.openmrs.module.PSI.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.PSI.PSIClinicChild;
import org.openmrs.module.PSI.SHNFormPdfDetails;
import org.openmrs.module.PSI.SHnPrescriptionMetaData;

public interface PSIUniquePatientService  extends OpenmrsService {
	
	public Boolean findPatientByUicandMobileNo(String patientUic, String mobileNo);
	
	public List<SHnPrescriptionMetaData> getAllPrescriptionMetaData();
	
	public List <SHNFormPdfDetails> getDischargeInformationByVisit(String patientUuid, String visitUuid);
	
	public List <SHNFormPdfDetails> getbirthInformationByVisit(String patientUuid, String visitUuid);
	
	public String getLastProviderName(String visitUuid);
	
}
