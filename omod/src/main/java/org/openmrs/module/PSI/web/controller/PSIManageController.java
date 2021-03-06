/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.PSI.web.controller;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.PSI.PSIClinicManagement;
import org.openmrs.module.PSI.PSIDHISMarker;
import org.openmrs.module.PSI.api.PSIDHISMarkerService;
import org.openmrs.module.PSI.dhis.service.PSIAPIServiceFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The main controller.
 */
@Controller
public class PSIManageController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/PSI/manage", method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
		PSIClinicManagement psiClinic = new PSIClinicManagement();
		psiClinic.setDateCreated(new Date());
		psiClinic.setCreator(Context.getAuthenticatedUser());
		psiClinic.setUuid(UUID.randomUUID().toString());
		psiClinic.setVoided(false);
		//Context.getService(PSIClinicManagementService.class).saveOrUpdateClinic(psiClinic);
		/*PSIDHISMarker psidhisMarker = new PSIDHISMarker();
		psidhisMarker.setType("Patient");
		//psidhisMarker.setMarkerDate(new Date());
		psidhisMarker.setTimestamp(0l);
		psidhisMarker.setDateCreated(new Date());
		//psidhisMarker.setCreator(Context.getAuthenticatedUser());
		psidhisMarker.setUuid(UUID.randomUUID().toString());
		psidhisMarker.setVoided(false);
		Context.getService(PSIDHISMarkerService.class).saveOrUpdate(psidhisMarker);*/
		try {
			JSONObject patient = new PSIAPIServiceFactory().getAPIType("openmrs").get("", "",
			    "/openmrs/ws/rest/v1/patient/6d4fdfef-84ab-4112-b76e-4cc7687ac96b?v=full");
			PSIDHISMarker psidhisMarker = new PSIDHISMarker();
			psidhisMarker.setType(patient.getString("uuid"));
			//psidhisMarker.setMarkerDate(new Date());
			psidhisMarker.setTimestamp(0l);
			psidhisMarker.setDateCreated(new Date());
			//psidhisMarker.setCreator(Context.getAuthenticatedUser());
			psidhisMarker.setUuid(UUID.randomUUID().toString());
			psidhisMarker.setVoided(false);
			Context.getService(PSIDHISMarkerService.class).saveOrUpdate(psidhisMarker);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
