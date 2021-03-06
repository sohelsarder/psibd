package org.openmrs.module.PSI.api.db.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.runtime.directive.Foreach;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.PSI.PSIServiceProvision;
import org.openmrs.module.PSI.api.db.PSIServiceProvisionDAO;
import org.openmrs.module.PSI.dto.AUHCComprehensiveReport;
import org.openmrs.module.PSI.dto.AUHCDashboardCard;
import org.openmrs.module.PSI.dto.AUHCDraftTrackingReport;
import org.openmrs.module.PSI.dto.AUHCRegistrationReport;
import org.openmrs.module.PSI.dto.AUHCVisitReport;
import org.openmrs.module.PSI.dto.DashboardDTO;
import org.openmrs.module.PSI.dto.PSILocationTag;
import org.openmrs.module.PSI.dto.PSIReport;
import org.openmrs.module.PSI.dto.PSIReportSlipTracking;
import org.openmrs.module.PSI.dto.SearchFilterDraftTracking;
import org.openmrs.module.PSI.dto.SearchFilterRegistrationReport;
import org.openmrs.module.PSI.dto.SearchFilterReport;
import org.openmrs.module.PSI.dto.SearchFilterSlipTracking;
import org.openmrs.module.PSI.dto.SearchFilterVisitReport;
import org.openmrs.module.PSI.utils.PSIConstants;

public class HibernatePSIServiceProvisionDAO implements PSIServiceProvisionDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	@Override
	public PSIServiceProvision saveOrUpdate(PSIServiceProvision psiServiceProvision) {
		sessionFactory.getCurrentSession().saveOrUpdate(psiServiceProvision);
		return psiServiceProvision;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIServiceProvision> getAll() {
		List<PSIServiceProvision> lists = new ArrayList<PSIServiceProvision>();
		lists = sessionFactory.getCurrentSession().createQuery("from PSIServiceProvision ").list();
		log.error(sessionFactory.getCurrentSession().createQuery(" from PSIServiceProvision ").
					getQueryString());
		return lists;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIServiceProvision> getAllByPatient(String patientUuid) {
		List<PSIServiceProvision> lists = new ArrayList<PSIServiceProvision>();
		lists = sessionFactory.getCurrentSession()
		        .createQuery("from PSIServiceProvision where patientUuid = :id  order by psiMoneyReceiptId desc")
		        .setString("id", patientUuid).list();
		
		log.error(sessionFactory.getCurrentSession()
		        .createQuery("from PSIServiceProvision where patientUuid = :id  order by spid desc")
		        .setString("id", patientUuid).getQueryString());
		
		return lists;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PSIServiceProvision findById(int id) {
		List<PSIServiceProvision> lists = sessionFactory.getCurrentSession()
		        .createQuery("from PSIServiceProvision where spid = :id").setInteger("id", id).list();
		
		log.error(sessionFactory.getCurrentSession()
				.createQuery("from PSIServiceProvision where spid = :id").setInteger("id", id)
				.getQueryString());
		
		if (lists.size() != 0) {
			return lists.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<PSIServiceProvision> getAllBetweenDateAndPatient(Date start, Date end, String patientUuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<PSIServiceProvision> getAllByDateAndPatient(Date date, String patientUuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void delete(int id) {
		PSIServiceProvision psiServiceProvision = findById(id);
		if (psiServiceProvision != null) {
			sessionFactory.getCurrentSession().delete(psiServiceProvision);
		} else {
			log.error("psiServiceProvision is null with id" + id);
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<PSIReport> servicePointWiseReport(String startDate, String endDate, String code) {
		List<Object[]> data = null;
		List<PSIReport> reportDTOs = new ArrayList<PSIReport>();
		String clinicCondition = "";
		if (!code.equalsIgnoreCase("0")) {
			clinicCondition = " and clinic_code = :code ";
		}
		String sql = "select code, item, category, sum(Static) as Static, sum(Satellite) as Satellite,  sum(CSP) as CSP , sum(Static)+sum(Satellite)+sum(CSP) as total from ( select code,item ,  category,service_point, sum(net_payable) as ttt,count(*), CASE WHEN service_point = 'Static' THEN sum(net_payable) ELSE 0 END Static,  CASE WHEN service_point = 'Satellite' THEN sum(net_payable) ELSE 0 END Satellite, CASE WHEN service_point = 'CSP' THEN sum(net_payable)  ELSE 0 END CSP from openmrs.psi_service_provision as sp  left join  openmrs.psi_money_receipt as mr on  sp.psi_money_receipt_id =mr.mid  where sp.is_complete = 1 and DATE(sp.money_receipt_date)  between  '"
		        + startDate
		        + "'  and  '"
		        + endDate
		        + "'"
		        + clinicCondition
		        + "  group by code ,item,service_point,category order  by code) as Report  group by code, item ";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		log.error(sql);
		if (!code.equalsIgnoreCase("0")) {
			data = query.setString("code", code).list();
		} else {
			data = query.list();
		}
		
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			PSIReport report = new PSIReport();
			Object[] objects = (Object[]) iterator.next();
			report.setCode(objects[0] + "");
			report.setItem(objects[1] + "");
			report.setCategory(objects[2] + "");
			report.setClinic(Float.parseFloat(objects[3].toString()));
			report.setSatelite(Float.parseFloat(objects[4].toString()));
			report.setCsp(Float.parseFloat(objects[5].toString()));
			report.setTotal(Float.parseFloat(objects[6].toString()));
			reportDTOs.add(report);
		}
		return reportDTOs;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIReport> serviceProviderWiseReport(String startDate, String endDate, String code, String dataCollector) {
		List<Object[]> data = null;
		List<PSIReport> reportDTOs = new ArrayList<PSIReport>();
		String clinicCondition = "";
		String dataCollectorCondition = "";
		if (!code.equalsIgnoreCase("0")) {
			clinicCondition = " and clinic_code = :code ";
		}
		
		if (!"".equalsIgnoreCase(dataCollector)) {
			dataCollectorCondition = " and data_collector = :dataCollector";
		}
		String sql = "select code,item ,category, count(*) as serviceCount ,ROUND(sum(net_payable),2) as total from openmrs.psi_service_provision as sp left join openmrs.psi_money_receipt as mr on  sp.psi_money_receipt_id =mr.mid where sp.is_complete = 1 and DATE(sp.money_receipt_date)  between '"
		        + startDate
		        + "' and '"
		        + endDate
		        + "' "
		        + clinicCondition
		        + dataCollectorCondition
		        + "  group by code ,item,category order  by code";
		
		log.error(sql);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		
		if (!code.equalsIgnoreCase("0")) {
			query = (SQLQuery) query.setString("code", code);
		}
		if (!"".equalsIgnoreCase(dataCollector)) {
			query = (SQLQuery) query.setString("dataCollector", dataCollector);
		}
		
		data = query.list();
		
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			PSIReport report = new PSIReport();
			Object[] objects = (Object[]) iterator.next();
			report.setCode(objects[0] + "");
			report.setItem(objects[1] + "");
			report.setCategory(objects[2] + "");
			report.setServiceCount(Integer.parseInt(objects[3].toString()));
			report.setTotal_(objects[4].toString()+"0");
			reportDTOs.add(report);
		}
		
		return reportDTOs;
	}
	
	@Override
	public String servicePointWiseRepor(String startDate, String endDate, String code) {
		List<Object[]> data = null;
		List<PSIReport> reportDTOs = new ArrayList<PSIReport>();
		/*String sql = "select code, item, category, sum(Clilic) as Clilic, sum(Satellite) as Satellite, "
		        + " sum(CSP) as CSP , sum(Clilic)+sum(Satellite)+sum(CSP) as total from ( select code,item , "
		        + " category,service_point, sum(net_payable) as ttt,count(*), CASE WHEN service_point = 'Clinic' THEN sum(net_payable) ELSE 0 END Clilic, "
		        + " CASE WHEN service_point = 'Satellite' THEN sum(net_payable) ELSE 0 END Satellite, "
		        + " CASE WHEN service_point = 'CSP' THEN sum(net_payable)  ELSE 0 END CSP from openmrs.psi_service_provision as sp "
		        + " left join  openmrs.psi_money_receipt as mr on  sp.psi_money_receipt_id =mr.mid "
		        + " where DATE(sp.money_receipt_date)  between  '" + startDate + "'  and  '" + endDate
		        + "'  and clinic_code = '" + code + "'"
		        + "  group by code ,item,service_point,category order  by code) as Report  group by code,item ";
		*/
		String sql = "select code, item, category, sum(Clilic) as Clilic, sum(Satellite) as Satellite,  sum(CSP) as CSP , sum(Clilic)+sum(Satellite)+sum(CSP) as total from ( select code,item ,  category,service_point, sum(net_payable) as ttt,count(*), CASE WHEN service_point = 'Clinic' THEN sum(net_payable) ELSE 0 END Clilic,  CASE WHEN service_point = 'Satellite' THEN sum(net_payable) ELSE 0 END Satellite, CASE WHEN service_point = 'CSP' THEN sum(net_payable)  ELSE 0 END CSP from openmrs.psi_service_provision as sp  left join  openmrs.psi_money_receipt as mr on  sp.psi_money_receipt_id =mr.mid  where DATE(sp.money_receipt_date)  between  '2019-02-06'  and  '2019-06-04'  and clinic_code = 'mouha84s'   group by code ,item,service_point,category order  by code) as Report  group by code, item ";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		log.error(sql);
		data = query.list();
		String s = "";
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			Object[] objects = (Object[]) iterator.next();
			s = objects[0].toString();
		}
		return data.toString() + data.size() + s;
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public DashboardDTO dashboardReport(String start, String end, String code, String dataCollector) {
		String servedPatienClinicCondition = "";
		String servedPatienDataCollectorCondition = "";
		if (!"0".equalsIgnoreCase(code)) {
			servedPatienClinicCondition = " clinic_code = :code and";
		}
		
		if (!"".equalsIgnoreCase(dataCollector)) {
			servedPatienDataCollectorCondition = " data_collector = :dataCollector and";
		}
		
		DashboardDTO dashboardDTO = new DashboardDTO();
		String servedPatientSql = "SELECT count(distinct(patient_uuid)) as count FROM openmrs.psi_money_receipt where "
		        + servedPatienClinicCondition + servedPatienDataCollectorCondition
		        + " is_complete = 1 and DATE(money_receipt_date) between '" + start + "' and '" + end + "'";
		
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(servedPatientSql);
		
		List<BigInteger> servedPatientData = new ArrayList<BigInteger>();
		if (!"0".equalsIgnoreCase(code)) {
			query = (SQLQuery) query.setString("code", code);
			//servedPatientData = query.setString("code", code).list();
		}
		
		if (!"".equalsIgnoreCase(dataCollector)) {
			query = (SQLQuery) query.setString("dataCollector", dataCollector);
		}
		
		servedPatientData = query.list();
		
		for (BigInteger servedValue : servedPatientData) {
			dashboardDTO.setServedPatient(servedValue.intValue());
		}
		
		String earnedPatientClinicCondition = "";
		String earnedPatientDataCollectorCondition = "";
		if (!"0".equalsIgnoreCase(code)) {
			earnedPatientClinicCondition = " and mr.clinic_code = :code ";
		}
		
		if (!"".equalsIgnoreCase(dataCollector)) {
			earnedPatientDataCollectorCondition = " and data_collector = :dataCollector ";
		}
		log.error(query.getQueryString());
		
		String earnedPatientSql = "SELECT sum(net_payable) FROM openmrs.psi_service_provision as sp left join openmrs.psi_money_receipt as mr  on sp.psi_money_receipt_id = mr.mid where sp.is_complete = 1 and DATE(sp.money_receipt_date) between '"
		        + start + "' and '" + end + "'" + earnedPatientClinicCondition + earnedPatientDataCollectorCondition + "";
		SQLQuery earnedQuery = sessionFactory.getCurrentSession().createSQLQuery(earnedPatientSql);
		List<Double> earnedData = new ArrayList<Double>();
		if (!"0".equalsIgnoreCase(code)) {
			earnedQuery = (SQLQuery) earnedQuery.setString("code", code);
		}
		if (!"".equalsIgnoreCase(dataCollector)) {
			earnedQuery = (SQLQuery) earnedQuery.setString("dataCollector", dataCollector);
		}
		earnedData = earnedQuery.list();
		
		if (earnedData.size() != 0) {
			for (Double double1 : earnedData) {
				if (double1 != null) {
					dashboardDTO.setEarned(double1.intValue());
				}
			}
		} else {
			dashboardDTO.setEarned(0);
		}
		
		log.error(earnedQuery.getQueryString());
		
		String newPatienClinicCondition = "";
		String newPatienDataCollectorCondition = "";
		if (!"0".equalsIgnoreCase(code)) {
			newPatienClinicCondition = " where PAT.person_attribute_type_id = :typeId  and PAT.value = :code ";
		}
		int creator = 0;
		if (!"".equalsIgnoreCase(dataCollector)) {
			User findUser = Context.getService(UserService.class).getUserByUsername(dataCollector);
			if (findUser != null) {
				creator = findUser.getId();
				newPatienDataCollectorCondition = " pa.creator = :creator   and ";
			} else {
				Query queryforRetired = sessionFactory.getCurrentSession().createQuery(
				    "from User u where u.retired = '1' and (u.username = ? or u.systemId = ?)");
				queryforRetired.setString(0, dataCollector);
				queryforRetired.setString(1, dataCollector);
				List<User> users = queryforRetired.list();
				User retiredUser = users.get(0);
				creator = retiredUser.getId();
				newPatienDataCollectorCondition = " pa.creator = :creator   and ";
			}
			
		}
		
		String newPatientSql = " select count(*) from (" +
				" select distinct(pa.person_id) personId from person_attribute pa where "
		        + " pa.person_attribute_type_id = "
		        + PSIConstants.attributeTypeRegDate
		        + " and  "
		        + newPatienDataCollectorCondition
		        + " DATE(pa.value) between '"
		        + start
		        + "' and '"
		        + end
		        + "' "
		        + " ) as a  "
		        + " join  ( SELECT distinct (PAT.person_id) personId "
		        + " FROM person_attribute as PAT "
		        + newPatienClinicCondition + " ) as b  " + "  on a.personId = b.personId ";
		
	
		SQLQuery newPatientQuery = sessionFactory.getCurrentSession().createSQLQuery(newPatientSql);
		List<BigInteger> newPatientData = new ArrayList<BigInteger>();
		if (!"0".equalsIgnoreCase(code)) {
			newPatientQuery = (SQLQuery) newPatientQuery.setString("code", code).setInteger("typeId",
			    PSIConstants.attributeTypeClinicCode);
			
		}
		if (!"".equalsIgnoreCase(dataCollector)) {
			newPatientQuery = (SQLQuery) newPatientQuery.setInteger("creator", creator);
		}
		newPatientData = newPatientQuery.list();
		
		log.error(newPatientQuery.getQueryString());
		
		for (BigInteger newPatient : newPatientData) {
			dashboardDTO.setNewPatient(newPatient.intValue());
		}
		
		return dashboardDTO;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIServiceProvision> findAllByTimestampNotSending(long timestamp) {
		List<PSIServiceProvision> lists = new ArrayList<PSIServiceProvision>();
		lists = sessionFactory
		        .getCurrentSession()
		        .createQuery("from PSIServiceProvision where  dhisId IS NULL and is_complete = :complete  order by spid asc")
		        .setLong("timestamp", timestamp).setInteger("complete", 1).list();
		
		log.error(sessionFactory
		        .getCurrentSession()
		        .createQuery("from PSIServiceProvision where  dhisId IS NULL and is_complete = :complete  order by spid asc")
		        .setLong("timestamp", timestamp).setInteger("complete", 1).getQueryString());
		
		return lists;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIServiceProvision> findAllByTimestamp(long timestamp) {
		List<PSIServiceProvision> lists = new ArrayList<PSIServiceProvision>();
		lists = sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from PSIServiceProvision where timestamp > :timestamp and  is_complete = :complete  order by timestamp asc ")
		        .setLong("timestamp", timestamp).setInteger("complete", 1).setMaxResults(3000).list();
		
		log.error(sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from PSIServiceProvision where timestamp > :timestamp and  is_complete = :complete  order by timestamp asc ")
		        .setLong("timestamp", timestamp).setInteger("complete", 1).setMaxResults(3000)
		        	.getQueryString());
		
		return lists;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIServiceProvision> findAllResend() {
		List<PSIServiceProvision> lists = new ArrayList<PSIServiceProvision>();
		lists = sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from PSIServiceProvision where is_send_to_dhis= :isSendToDHIS3 and is_complete = :complete  order by spid asc")
		        .setInteger("isSendToDHIS3", PSIConstants.CONNECTIONTIMEOUTSTATUS).setInteger("complete", 1)
		        .setMaxResults(2000).list();
		
		log.error(sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from PSIServiceProvision where is_send_to_dhis= :isSendToDHIS3 and is_complete = :complete  order by spid asc")
		        .setInteger("isSendToDHIS3", PSIConstants.CONNECTIONTIMEOUTSTATUS).setInteger("complete", 1)
		        .setMaxResults(2000).getQueryString());
		
		return lists;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIServiceProvision> findAllByMoneyReceiptId(int moneyReceiptId) {
		List<PSIServiceProvision> lists = new ArrayList<PSIServiceProvision>();
		lists = sessionFactory.getCurrentSession()
		        .createQuery("from PSIServiceProvision where  psiMoneyReceiptId = :moneyReceiptId")
		        .setInteger("moneyReceiptId", moneyReceiptId).list();
		
		log.error(sessionFactory.getCurrentSession()
		        .createQuery("from PSIServiceProvision where  psiMoneyReceiptId = :moneyReceiptId")
		        .setInteger("moneyReceiptId", moneyReceiptId)
		        	.getQueryString());
		return lists;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void deleteByPatientUuidAndMoneyReceiptIdNull(String patientUuid) {
		
		List<PSIServiceProvision> lists = sessionFactory.getCurrentSession()
		        .createQuery("from PSIServiceProvision where psi_money_receipt_id is null and patient_uuid = :id")
		        .setString("id", patientUuid).list();
		if (lists.size() != 0) {
			for (PSIServiceProvision serviceProvision : lists) {
				int spid = serviceProvision.getSpid();
				delete(spid);
			}
		}
	}

	@Override
	public String getTotalDiscount(String startDate, String endDate) {
		// TODO Auto-generated method stub
		String hql = "SELECT ROUND(SUM(discount),2) FROM PSIServiceProvision " +
					" WHERE DATE(moneyReceiptDate) BETWEEN '"+startDate+"' AND '"+endDate+"'"+
				" AND isComplete=1";
		Double ret;
		 try{ 
			  ret = (Double)sessionFactory.getCurrentSession().createQuery(hql).
					 list().get(0);
			  log.error(sessionFactory.getCurrentSession().createQuery(hql).getQueryString());
			  
			 return  ret != null ? String.valueOf(ret) : "0";
		 }catch (Exception e){
			 log.error(sessionFactory.getCurrentSession().createQuery(hql).getQueryString());
			 log.error(e.toString());
			 return "0";
//			 return e.toString();
		 }
		 

	}

	@Override
	public String getTotalServiceContact(String startDate, String endDate) {
		// TODO Auto-generated method stub
		String hql = "SELECT count(*) FROM PSIServiceProvision"+
				" WHERE moneyReceiptDate BETWEEN '"+startDate+"' AND '"+endDate+"'";
		 try{ 
			 Long ret = (Long)sessionFactory.getCurrentSession().createQuery(hql).list().get(0);
			 log.error(sessionFactory.getCurrentSession().createQuery(hql).toString());
			 
			 return  ret != null ? String.valueOf(ret) : "0";
		 }catch (Exception e){
			 log.error(sessionFactory.getCurrentSession().createQuery(hql).toString());
			 log.error(e.toString());
			 return "0";
		 }
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PSIReportSlipTracking> getSlipTrackingReport(
			SearchFilterSlipTracking filter) {
		List<PSIReportSlipTracking> slipTrackingList = new ArrayList<PSIReportSlipTracking>();
		// TODO Auto-generated method stub
		List<Object[]> resultSet = new ArrayList<Object[]>();

		
		String wh = " where m.is_complete= 1 ";
		if(filter.getStartDateSlip() != null && filter.getEndDateSlip() != null){
			wh += " and Date(p.money_receipt_date) between '" + filter.getStartDateSlip() + "' and '"
					+ filter.getEndDateSlip()+"' ";
		}
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
		
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( m.wealth = '" + filter.getWlthAbleToPay() + 
					"' or m.wealth = '"+filter.getWlthPoor()+
					"' or m.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}

		if(spFlag == true){
			wh += " and ( m.service_point = '" + filter.getSpCsp()+
					"' or m.service_point = '" + filter.getSpSatelite()+
					"' or m.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}

		if (!"".equalsIgnoreCase(filter.getCollector())){
			wh += " and m.data_collector = '" + filter.getCollector()+"' ";
		}
		
		if(filter.getClinicCode().equals("0")){
			
		}else {
			wh += " and m.clinic_code='"+filter.getClinicCode()+"' ";
		}
		String dateFormat = "%Y-%m-%d %h:%i";
		String sql = "select p.spid as sl,m.eslip_no as eslipNo,m.slip_no as slip_no,date_format(p.money_receipt_date,'"+dateFormat+"') as slip_date, "+
				"m.patient_name as patient_name, "+
				"m.contact as phone,m.wealth as wealth_classification,m.service_point as service_point, "+
				"sum(p.total_amount) as total_amount,ROUND(sum((p.discount)),2) as discount,ROUND(sum(p.net_payable),2) as net_payable, "+
				" p.patient_uuid as patient_uuid "+
				"from openmrs.psi_service_provision p join openmrs.psi_money_receipt m "+
				"on p.psi_money_receipt_id = m.mid "+
				wh+
				" group by p.psi_money_receipt_id ";
		
//		sql += " LIMIT 10";
		List<PSIReportSlipTracking> psiList = new ArrayList<PSIReportSlipTracking>();
		try{
			 psiList = sessionFactory.getCurrentSession().createSQLQuery(sql).
					addScalar("sl",StandardBasicTypes.LONG).
					addScalar("eslipNo",StandardBasicTypes.STRING).
					addScalar("slip_no",StandardBasicTypes.STRING).
					addScalar("slip_date",StandardBasicTypes.STRING).
					addScalar("patient_name",StandardBasicTypes.STRING).
					addScalar("phone",StandardBasicTypes.STRING).
					addScalar("wealth_classification",StandardBasicTypes.STRING).
					addScalar("service_point",StandardBasicTypes.STRING).
					addScalar("total_amount",StandardBasicTypes.LONG).
					addScalar("discount",StandardBasicTypes.STRING).
					addScalar("net_payable",StandardBasicTypes.STRING).
					addScalar("patient_uuid",StandardBasicTypes.STRING).
					setResultTransformer(new AliasToBeanResultTransformer(PSIReportSlipTracking.class)).
				list();
			log.error(sql);	
			return psiList;
		}catch(Exception e){
//			return null;
			log.error(sql);
			log.error(e.toString());
			return psiList;
		}
	
		
//		return psiList;
	}
	
	public List<Object[]> getSlip(	SearchFilterSlipTracking filter){
		List<Object[]> ret = new ArrayList<Object[]>();
		String sql = "select p.spid as sL,m.slip_no as slipNo,p.money_receipt_date as slipDate," +
				"m.patient_name as patientName,"
					+"m.contact as phone,m.wealth as wealthClassification,m.service_point as servicePoint," +
					"p.total_amount as totalAmount,p.discount as discount,p.net_payable as netPayable"+
					" from openmrs.psi_service_provision p join openmrs.psi_money_receipt m"+
					" on p.patient_uuid = m.patient_uuid";
		ret = sessionFactory.getCurrentSession().createSQLQuery(sql).list();
		log.error(sql);
		return ret;
	}
	
	
	@Override
	public List<AUHCDraftTrackingReport> getDraftTrackingReport(
			SearchFilterDraftTracking filter) {
		// TODO Auto-generated method stub
		String wh = "";
		if(filter.getStartDateSlip() != null && filter.getEndDateSlip() != null){
			wh += " and Date(p.money_receipt_date) between '" + filter.getStartDateSlip() + "' and '"
					+ filter.getEndDateSlip()+"' ";
		}
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
		
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( m.wealth = '" + filter.getWlthAbleToPay() + 
					"' or m.wealth = '"+filter.getWlthPoor()+
					"' or m.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}

		if(spFlag == true){
			wh += " and ( m.service_point = '" + filter.getSpCsp()+
					"' or m.service_point = '" + filter.getSpSatelite()+
					"' or m.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}
		if(!"0".equalsIgnoreCase(filter.getClinicCode()))
			wh += " and m.clinic_code = '"+filter.getClinicCode()+"' ";
		
		if (!"".equalsIgnoreCase(filter.getCollector())){
			wh += " and m.data_collector = '" + filter.getCollector()+"' ";
		}
		String dateFormat = "%Y-%m-%d %h:%i";
		String sql = "select p.psi_money_receipt_id as mid,m.eslip_no as eslipNo,COUNT(p.psi_money_receipt_id) as total_service_contact,p.spid as sl,m.slip_no as slip_no,date_format(p.money_receipt_date,'"+dateFormat+"') as slip_date, "+
				"m.patient_name as patient_name, "+
				"m.contact as phone,m.wealth as wealth_classification,m.service_point as service_point, "+
				"sum(p.total_amount) as total_amount,ROUND(sum(p.discount),2) as discount,ROUND(sum(p.net_payable),2) as net_payable, "+
				" p.patient_uuid as patient_uuid "+
				"from openmrs.psi_service_provision p join openmrs.psi_money_receipt m "+
				"on p.psi_money_receipt_id = m.mid "+
				"where m.is_complete = 0 ";
		
		sql += wh;
		sql += " group by p.psi_money_receipt_id";
		List<AUHCDraftTrackingReport> draftList = new ArrayList<AUHCDraftTrackingReport>();
		try{
			draftList = sessionFactory.getCurrentSession().createSQLQuery(sql).
				addScalar("mid",StandardBasicTypes.INTEGER).
				addScalar("eslipNo",StandardBasicTypes.STRING).
				addScalar("total_service_contact",StandardBasicTypes.STRING).
				addScalar("sl",StandardBasicTypes.LONG).
				addScalar("slip_no",StandardBasicTypes.STRING).
				addScalar("slip_date",StandardBasicTypes.STRING).
				addScalar("patient_name",StandardBasicTypes.STRING).
				addScalar("phone",StandardBasicTypes.STRING).
				addScalar("wealth_classification",StandardBasicTypes.STRING).
				addScalar("service_point",StandardBasicTypes.STRING).
				addScalar("total_amount",StandardBasicTypes.LONG).
				addScalar("discount",StandardBasicTypes.STRING).
				addScalar("net_payable",StandardBasicTypes.STRING).
				addScalar("patient_uuid",StandardBasicTypes.STRING).
				setResultTransformer(new AliasToBeanResultTransformer(AUHCDraftTrackingReport.class)).
				list();
			log.error(sql);
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return null;
		}
		return draftList;
	}

	@Override
	public String getNoOfDraft(String startDate, String endDate) {
		// TODO Auto-generated method stub
		String sql = " select count(*) "+
				"from openmrs.psi_service_provision p join openmrs.psi_money_receipt m "+
				"on p.psi_money_receipt_id = m.mid "+
				"where p.money_receipt_date BETWEEN '"+startDate+"' AND '"+endDate+"'"+
				" and m.is_complete = 0 ";
		String ret = "0";
		try{
		 ret = sessionFactory.getCurrentSession().createSQLQuery(sql).list().
				get(0).toString();
		 log.error(sql);
		}
		catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
		return ret;
	}

	@Override
	public String getTotalPayableDraft(String startDate, String endDate) {
		// TODO Auto-generated method stub
		String sql = "select sum(p.net_payable) "+
				" from openmrs.psi_service_provision p join openmrs.psi_money_receipt m "+
				"	on p.psi_money_receipt_id = m.mid " +
				"  where p.money_receipt_date BETWEEN '"+startDate+"' AND  '"+endDate+"' "+
				" and m.is_complete = 0";
		String ret = "";
		try{
			 ret = sessionFactory.getCurrentSession().createSQLQuery(sql).list().
					get(0).toString();
			 log.error(sql);
		}
		catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
		
		return ret;
	}

	@Override
	public String getDashboardNewReg(String startDate, String endDate) {
		// TODO Auto-generated method stub
		String sql = "";
		return "0";
	}

	@Override
	public String getDashboardOldClients(String startDate, String endDate) {
		// TODO Auto-generated method stub
		String sql = "SELECT Count(*) "+
					" FROM   (SELECT pmr.patient_uuid "+ 
					" FROM   psi_money_receipt pmr "+ 
					" JOIN (SELECT patient_uuid, "+ 
                     "  Count(patient_uuid) AS total "+ 
                     " FROM   psi_money_receipt "+
                     " WHERE  money_receipt_date BETWEEN "+
                        "'"+startDate+"' AND '"+endDate+"' "+ 
                     " GROUP  BY patient_uuid) AS newclient "+ 
                 " ON pmr.patient_uuid = newclient.patient_uuid "+ 
                 " WHERE  pmr.money_receipt_date < '"+startDate+"' "+ 
                 " GROUP  BY pmr.patient_uuid) AS tbl ";
		try{
			String ret = sessionFactory.getCurrentSession().createSQLQuery(sql).list().
					get(0).toString();
			log.error(sql);
			
			return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
		
	}
	@Override
	public String getDashboardOldClients(String startDate, String endDate,
			String clinicCode, String gender) {
		// TODO Auto-generated method stub
		String wh="";
		
		if(gender.equals("F")) wh += " and pmr.gender = 'F' ";
		else if(gender.equals("M")) wh += " and pmr.gender = 'M' ";
		else if(gender.equals("O")) wh += " and pmr.gender = 'O' ";
		else if(gender.length() == 2){
			if(gender.equals("MO"))
				wh += " and pmr.gender != 'F' ";
			else if(gender.equals("FO"))
				wh += "and pmr.gender != 'M' ";
			else if(gender.equals("MF")){
				
			}
		}
		
		if(!"0".equalsIgnoreCase(clinicCode))
			wh += " and pmr.clinic_code = "+clinicCode;
		
		String sql = "SELECT Count(*) "+
				" FROM   (SELECT pmr.patient_uuid "+ 
				" FROM   psi_money_receipt pmr "+ 
				" JOIN (SELECT patient_uuid, "+ 
                 "  Count(patient_uuid) AS total "+ 
                 " FROM   psi_money_receipt "+
                 " WHERE  money_receipt_date BETWEEN "+
                    "'"+startDate+"' AND '"+endDate+"' "+ 
                 " GROUP  BY patient_uuid) AS newclient "+ 
             " ON pmr.patient_uuid = newclient.patient_uuid "+ 
             " WHERE  pmr.money_receipt_date < '"+startDate+"' "+
             	wh+
             " GROUP  BY pmr.patient_uuid) AS tbl ";
		try{
			String ret = sessionFactory.getCurrentSession().createSQLQuery(sql).list().
					get(0).toString();
			log.error(sql);
			return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
	}

	@Override
	public String getDashboardNewClients(String startDate, String endDate) {
		// TODO Auto-generated method stub
		String sql = "SELECT Count(*) "+
					" FROM "+
					" (SELECT patient_uuid "+
					" FROM   psi_money_receipt p "+ 
                    " WHERE  p.money_receipt_date BETWEEN "+ 
                    "'"+startDate+"' AND '"+endDate+"' GROUP BY p.patient_uuid) as p_tbl";
		try{
			String res = sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
			Long ret =(Long.parseLong(res) -  Long.parseLong(getDashboardOldClients(startDate,endDate)));
			log.error(sql);
			return ret.toString();
//			return res;
		}
		catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
	}

	@Override
	public List<AUHCComprehensiveReport> getComprehensiveReport(SearchFilterReport filter) {
		// TODO Auto-generated method stub
		String wh = "";
		if(filter.getService_category() != "")
			wh += " AND category = '"+filter.getService_category()+"'";
		String clinicWh = "";
		if(filter.getClinic_code().equals("0"))
			clinicWh +="";
		else	clinicWh+= " AND mr.clinic_code='"+filter.getClinic_code()+"' ";
		String sql = "SELECT code as service_code," +
				" item as service_name," +
				" category as category," +
				" ROUND(Sum(static),2)                             AS revenue_static," +
				" ROUND(Sum(satellite),2)                          AS revenue_satellite," +
				" ROUND(Sum(csp),2)                                AS revenue_csp," +
				" ROUND(Sum(static) + Sum(satellite) + Sum(csp),2) AS revenue_total," +
				" ROUND(Sum(countstatic),2)                        AS service_contact_static," +
				" ROUND(Sum(countsatellite),2)                     AS service_contact_satellite," +
				" ROUND(Sum(countcsp),2)                           AS service_contact_csp," +
				" ROUND(Sum(countstatic)+ Sum(countsatellite)+Sum(countcsp),2) AS service_total," +
				" ROUND(Sum(discountstatic),2)                     AS discount_static," +
				" ROUND(Sum(discountsatellite),2)                  AS discount_satellite," +
				" ROUND(Sum(discountcsp),2)                        AS discount_csp," +
				" ROUND(Sum(discountstatic) + Sum(discountsatellite) + Sum(discountcsp),2) AS discount_total" +
				" FROM   (SELECT code," +
				" item," +
				" category," +
				" service_point," +
				" Sum(net_payable) AS ttt," +
				" Count(*)," +
				" CASE" +
				" WHEN service_point = 'Static' THEN Sum(net_payable)" +
				" ELSE 0 " +
				" END              Static, " +
				" CASE" +
				" WHEN service_point = 'Static' THEN Sum(discount)" +
				" ELSE 0" +
				" END              DiscountStatic," +
				" CASE" +
				" WHEN service_point = 'Static' THEN Count(*)" +
				" ELSE 0" +
				" END              CountStatic," +
				" CASE" +
				" WHEN service_point = 'Satellite' THEN Sum(net_payable)" +
				" ELSE 0" +
				" END              Satellite," +
				" CASE " +
				" WHEN service_point = 'Satellite' THEN Sum(discount)" +
				" ELSE 0 " +
				" END              DiscountSatellite," +
				" CASE " +
				" WHEN service_point = 'Satellite' THEN Count(*)" +
				" ELSE 0" +
				" END              CountSatellite," +
				" CASE " +
				" WHEN service_point = 'CSP' THEN Sum(net_payable)" +
				" ELSE 0 " +
				" END              CSP," +
				" CASE" +
				" WHEN service_point = 'CSP' THEN Sum(discount) " +
				" ELSE 0 " +
				" END              DiscountCSP, " +
				" CASE " +
				" WHEN service_point = 'CSP' THEN Count(*) " +
				" ELSE 0 " +
				" END              CountCSP" +
				" FROM   openmrs.psi_service_provision AS sp" +
				" LEFT JOIN openmrs.psi_money_receipt AS mr" +
				"                      ON sp.psi_money_receipt_id = mr.mid" +
				" WHERE  sp.is_complete = 1" +
				clinicWh+
				" AND Date(sp.money_receipt_date) BETWEEN" +
				"'"+filter.getStart_date()+"' AND '"+filter.getEnd_date()+"'"+
               wh+
        " GROUP  BY code," +
        	" item,      service_point, category" +
        	" ORDER  BY code) AS Report" +
        	" GROUP  BY code, item";

		
		List<AUHCComprehensiveReport> report = new ArrayList<AUHCComprehensiveReport>();
		try{
			report = sessionFactory.getCurrentSession().createSQLQuery(sql).
					addScalar("service_code",StandardBasicTypes.STRING).
					addScalar("service_name",StandardBasicTypes.STRING).
					addScalar("category",StandardBasicTypes.STRING).
					addScalar("revenue_static",StandardBasicTypes.STRING).
					addScalar("revenue_satellite",StandardBasicTypes.STRING).
					addScalar("revenue_csp",StandardBasicTypes.STRING).
					addScalar("revenue_total",StandardBasicTypes.STRING).
					addScalar("service_contact_static",StandardBasicTypes.STRING).
					addScalar("service_contact_satellite",StandardBasicTypes.STRING).
					addScalar("service_contact_csp",StandardBasicTypes.STRING).
					addScalar("service_total",StandardBasicTypes.STRING).
					addScalar("discount_static",StandardBasicTypes.STRING).
					addScalar("discount_satellite",StandardBasicTypes.STRING).
					addScalar("discount_csp",StandardBasicTypes.STRING).
					addScalar("discount_total",StandardBasicTypes.STRING).
					setResultTransformer(new AliasToBeanResultTransformer(AUHCComprehensiveReport.class)).
					list();
			
			
			
//			AUHCComprehensiveReport rep = new AUHCComprehensiveReport();
//			rep.setService_name(sql);
//			report.add(rep);
			log.error(sql);
			return report;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return report;
		}
	
	}

	@Override
	public List<AUHCRegistrationReport> getRegistrationReport(
			SearchFilterReport filter) {
		// TODO Auto-generated method stub
		String sql = "select code, item, category, sum(Static) as Static, sum(Satellite) as Satellite,"+  
					"sum(CSP) as CSP , sum(Static)+sum(Satellite)+sum(CSP) as total,sum(CountStatic) countStatic,"+
					"sum(CountSatellite) as CountAtSatellite,"+
					"sum(CountCSP) as CountAtCSP,"+
					"sum(DiscountStatic) as DiscountAtStatic,"+
					"sum(DiscountSatellite) as DiscountAtSatellite,"+
					"sum(DiscountCSP) as DiscountAtCSP "+
					" from ( "+
					" select code,item , category,service_point, sum(net_payable) as ttt, "+
					" count(*), "+
					" CASE WHEN service_point = 'Static' THEN sum(net_payable) ELSE 0 END Static, "+
					" CASE WHEN service_point = 'Static' THEN sum(discount) ELSE 0 END DiscountStatic, "+
                    " CASE WHEN service_point = 'Static' THEN count(*) ELSE 0 END CountStatic, "+
                    " CASE WHEN service_point = 'Satellite' THEN sum(net_payable) ELSE 0 END Satellite, "+
                    " CASE WHEN service_point = 'Satellite' THEN sum(discount) ELSE 0 END DiscountSatellite,"+
                    " CASE WHEN service_point = 'Satellite' THEN count(*) ELSE 0 END CountSatellite, "+
                    " CASE WHEN service_point = 'CSP' THEN sum(net_payable)  ELSE 0 END CSP ,"+
                    " CASE WHEN service_point = 'CSP' THEN sum(discount)  ELSE 0 END DiscountCSP ,"+
                    " CASE WHEN service_point = 'CSP' THEN count(*)  ELSE 0 END CountCSP "+
                    " from openmrs.psi_service_provision as sp  left join "+  
                    " openmrs.psi_money_receipt as mr on  sp.psi_money_receipt_id =mr.mid "+  
                    " where sp.is_complete = 1 and DATE(sp.money_receipt_date) "+
                    " between '"+filter.getStart_date()+"' and '"+filter.getEnd_date()+"' "+
                    " group by code ,item,service_point,category order  by code) as Report "+         
         			" group by code, item";
       
		log.error(sql); 		
		List<AUHCRegistrationReport> ret = new ArrayList<AUHCRegistrationReport>();
		return ret;
	}

	@Override
	public String getTotalPayableDraft(SearchFilterDraftTracking filter) {
		// TODO Auto-generated method stub
			try{
				List<AUHCDraftTrackingReport> drafts =  getDraftTrackingReport(filter);
				Double ret = 0.00;
				for(int i = 0; i < drafts.size();i++)
					ret += Double.parseDouble(drafts.get(i).getNet_payable());
				
				return ret.toString();
			}catch(Exception e){
				return "0.00";
			}
	}

	@Override
	public String getTotalDiscount(SearchFilterSlipTracking filter) {
		// TODO Auto-generated method stub
		String wh = "";
		if(filter.getStartDateSlip() != null && filter.getEndDateSlip() != null){
			wh += " and Date(p.money_receipt_date) between '" + filter.getStartDateSlip() + "' and '"
					+ filter.getEndDateSlip()+"' ";
		}
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
		
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( m.wealth = '" + filter.getWlthAbleToPay() + 
					"' or m.wealth = '"+filter.getWlthPoor()+
					"' or m.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}

		if(spFlag == true){
			wh += " and ( m.service_point = '" + filter.getSpCsp()+
					"' or m.service_point = '" + filter.getSpSatelite()+
					"' or m.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}
		if(filter.getClinicCode().equals("0")){
			
		}
		else {
			wh += " and m.clinic_code = '"+filter.getClinicCode()+"' ";
		}
		
		if (!"".equalsIgnoreCase(filter.getCollector())){
			wh += " and m.data_collector = '" + filter.getCollector()+"' ";
		}
		
		String sql = " select ROUND(sum(p.discount),2) "+
				" from openmrs.psi_service_provision p join openmrs.psi_money_receipt m "+
				" on p.psi_money_receipt_id = m.mid "+
				" where m.is_complete=1";
		
		sql += wh;
//		sql += " group by p.psi_money_receipt_id ";
		
		
		String ret = "0";
		try{
			
			List<Object> sz = sessionFactory.getCurrentSession().createSQLQuery(sql).list(); 
			ret = sz.size() != 0 ?
					sz.get(0).toString() : "0";
					
			log.error(sql);
			return ret;
			
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
		
	}

	@Override
	public String getTotalServiceContact(SearchFilterSlipTracking filter) {
		// TODO Auto-generated method stub
		String hql = "SELECT count(*) FROM PSIServiceProvision"+
				" WHERE moneyReceiptDate BETWEEN '"+filter.getStartDateSlip()+"' AND '"
					+filter.getEndDateSlip()+"'";
		if(filter.getCollector() != "") {
			hql += " AND provider='"+filter.getCollector()+"' ";
		}
		
		 try{ 
			 Long ret = (Long)sessionFactory.getCurrentSession().createQuery(hql).list().get(0);
			 log.error(sessionFactory.getCurrentSession().createQuery(hql).getQueryString());
			 return ret.toString();
		 }catch (Exception e){
			 log.error(sessionFactory.getCurrentSession().createQuery(hql).getQueryString());
			 log.error(e.toString());
			 return "0";
		 }
	}

	@Override
	public String getPatientsServed(SearchFilterSlipTracking filter) {
		// TODO Auto-generated method stub
		String wh = "";
		if(filter.getStartDateSlip() != null && filter.getEndDateSlip() != null){
			wh += " and Date(p.money_receipt_date) between '" + filter.getStartDateSlip() + "' and '"
					+ filter.getEndDateSlip()+"' ";
		}
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
		
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( m.wealth = '" + filter.getWlthAbleToPay() + 
					"' or m.wealth = '"+filter.getWlthPoor()+
					"' or m.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}

		if(spFlag == true){
			wh += " and ( m.service_point = '" + filter.getSpCsp()+
					"' or m.service_point = '" + filter.getSpSatelite()+
					"' or m.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}
		
		if(!"0".equalsIgnoreCase(filter.getClinicCode()))
			wh += " and m.clinic_code = '"+filter.getClinicCode()+"' ";
		
		if (!"".equalsIgnoreCase(filter.getCollector())){
			wh += " and m.data_collector = '" + filter.getCollector()+"' ";
		}
		String sql = " select count(distinct(p.patient_uuid)) "+
				" from openmrs.psi_service_provision p join openmrs.psi_money_receipt m "+
				" on p.psi_money_receipt_id = m.mid " +
				" where m.is_complete = 1 ";
		
		sql += wh;
//		sql += " group by p.psi_money_receipt_id ";
		
		String ret = "0";
		try{
			ret = sessionFactory.getCurrentSession().createSQLQuery(sql).list().
					get(0).toString();
//			return "0";
			log.error(sql);
			return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return ret;
		}
	}

	@Override
	public String getRevenueEarned(SearchFilterSlipTracking filter) {
		// TODO Auto-generated method stub
		String wh = "";
		if(filter.getStartDateSlip() != null && filter.getEndDateSlip() != null){
			wh += " and Date(p.money_receipt_date) between '" + filter.getStartDateSlip() + "' and '"
					+ filter.getEndDateSlip()+"' ";
		}
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
		
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( m.wealth = '" + filter.getWlthAbleToPay() + 
					"' or m.wealth = '"+filter.getWlthPoor()+
					"' or m.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}

		if(spFlag == true){
			wh += " and ( m.service_point = '" + filter.getSpCsp()+
					"' or m.service_point = '" + filter.getSpSatelite()+
					"' or m.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}
		if(!"0".equalsIgnoreCase(filter.getClinicCode()))
			wh += " and m.clinic_code = '"+filter.getClinicCode()+"' ";
		
		if (!"".equalsIgnoreCase(filter.getCollector())){
			wh += " and m.data_collector = '" + filter.getCollector()+"' ";
		}
		String sql = " select ROUND(sum(p.net_payable),2) "+
				" from openmrs.psi_service_provision p join openmrs.psi_money_receipt m "+
				" on p.psi_money_receipt_id = m.mid"+
				" where m.is_complete=1 ";
		
		sql += wh;
//		sql += " group by p.psi_money_receipt_id ";
		
		String ret = "0";
		try{
			ret = sessionFactory.getCurrentSession().createSQLQuery(sql).list().
					get(0).toString();
//			ret = ret != "0" ? prepareDoubleFloat(ret): "0";
			log.error(sql);
			
			return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AUHCRegistrationReport> getRegistrationReport(String startDate,
			String endDate, String gender,String code) {
		// TODO Auto-generated method stub
		
		String sql = ""
				+ "SELECT "
				+ "	   CONCAT(pname.given_name,\" \",pname.family_name) AS patient_name, "
				+ "         temp4.UIC as uic, "
				+ "       pi.identifier         AS health_id, "
				+ "        temp1.phoneno         AS mobile_no, "
				+ "       p.gender              AS gender, "
				+ "       temp5.registeredDate  AS register_date, "
				+ "        IFNULL(TIMESTAMPDIFF(YEAR, p.birthdate, CURDATE()),0) AS age, "
				+ "        paddress.address3 as cc, "
				+ "       p.uuid              AS patient_uuid "
				+ "       FROM   person_name pname "
				+ "	   left JOIN patient_identifier pi "
				+ "              ON pname.person_id = pi.patient_id "
				+ "        JOIN person p "
				+ "              ON p.person_id = pi.patient_id "
				+ " "
				+ "        JOIN (SELECT pat.person_attribute_type_id, "
				+ "                         pat.value AS phoneNo, "
				+ "                         pat.person_id "
				+ "                  FROM   person_attribute pat "
				+ "                  WHERE  pat.person_attribute_type_id = 42) AS temp1 "
				+ "              ON pi.patient_id = temp1.person_id "
				+ "        JOIN (SELECT pa.person_attribute_type_id, "
				+ "                         pa.value AS UIC, "
				+ "                         pa.person_id "
				+ "                  FROM   person_attribute pa "
				+ "                  WHERE  pa.person_attribute_type_id = 34) AS temp4 "
				+ "              ON pi.patient_id = temp4.person_id "
				+ "        JOIN (SELECT person_attribute_temp5.person_attribute_type_id, "
				+ "                         person_attribute_temp5.value AS registeredDate , "
				+ "                         person_attribute_temp5.person_id "
				+ "                  FROM   person_attribute person_attribute_temp5 "
				+ "                  WHERE  person_attribute_temp5.person_attribute_type_id = 40) AS temp5 "
				+ "              ON pi.patient_id = temp5.person_id "
				+ "        JOIN (SELECT person_attribute_temp6.person_attribute_type_id, "
				+ "                         person_attribute_temp6.value AS clinicCode , "
				+ "                         person_attribute_temp6.person_id "
				+ "                  FROM   person_attribute person_attribute_temp6 "
				+ "                  WHERE  person_attribute_temp6.person_attribute_type_id = 32) AS temp6 "
				+ "              ON pi.patient_id = temp6.person_id "
				+ "        left JOIN person_address paddress "
				+ "              ON paddress.person_id = pi.patient_id "
				+ "              where temp5.registeredDate BETWEEN '"+startDate+"' and " +
						"	'"+endDate+"' and "
				+ "              pname.preferred = 1 ";
				
		
//		if(gender == "F") sql += " and p.gender='F' ";
//		else if(gender == "M") sql += " and p.gender='M' ";
//		else if(gender.equals("O"))
//			sql += " and (p.gender!='M' and p.gender !='F')";
//		else if(gender.contains("M") && gender.contains("O"))
//			sql += " and p.gender != 'F' ";
//		else if(gender.contains("F") && gender.contains("O"))
//			sql += " and p.gender != 'M' ";
		
		if(gender.equals("F")) sql += " and p.gender = 'F' ";
		else if(gender.equals("M")) sql += " and p.gender = 'M' ";
		else if(gender.equals("O")) sql += " and p.gender = 'O' ";
		else if(gender.length() == 2){
			if(gender.equals("MO"))
				sql += " and p.gender != 'F' ";
			else if(gender.equals("FO"))
				sql += "and p.gender != 'M' ";
			else if(gender.equals("MF")){
				
			}
		}
		if(!"0".equalsIgnoreCase(code))
			sql += " and temp6.clinicCode = '"+code+"' ";
		sql +=  " GROUP by p.uuid";
		
		List<AUHCRegistrationReport> report = new ArrayList<AUHCRegistrationReport>();
		try{
			report = sessionFactory.getCurrentSession().createSQLQuery(sql).
					addScalar("patient_name",StandardBasicTypes.STRING).
					addScalar("uic",StandardBasicTypes.STRING).
					addScalar("health_id",StandardBasicTypes.STRING).
					addScalar("mobile_no",StandardBasicTypes.STRING).
					addScalar("gender",StandardBasicTypes.STRING).
					addScalar("register_date",StandardBasicTypes.STRING).
					addScalar("age",StandardBasicTypes.LONG).
					addScalar("cc",StandardBasicTypes.STRING).
					addScalar("patient_uuid",StandardBasicTypes.STRING).
					setResultTransformer(new AliasToBeanResultTransformer(AUHCRegistrationReport.class)).
					list();
			
			log.error(sql);
			return report;
			
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return report;
		}
//		return report;
	}

	@Override
	public List<AUHCVisitReport> getVisitReport(String startDate, String endDate,String code) {
		// TODO Auto-generated method stub
		List<AUHCVisitReport> report = new ArrayList<AUHCVisitReport>();
		String wh = "";
		if(code.equals("0")){
			
		}
		else wh += " AND m.clinic_code = '"+code+"' ";
		String sql = " SELECT m.patient_name as patient_name,pi.patient_identifier_id as hid, " +
				" m.contact as mobile_number, m.gender as gender, " +
				" TIMESTAMPDIFF(YEAR, p.birthdate, CURDATE()) as age, " +
				" DATE_FORMAT(m.patient_registered_date, '%d.%m.%Y') as reg_date, " +
				" MAX(DATE_FORMAT(m.money_receipt_date, '%d.%m.%Y')) as last_visit_date, " +
				" COUNT(m.patient_uuid) as visit_count" +
				" FROM openmrs.psi_money_receipt  m " +
				" JOIN openmrs.person p " +
				" ON m.patient_uuid = p.uuid " +
				" JOIN openmrs.patient_identifier pi " +
				" ON p.person_id = pi.patient_id  " +
				" LEFT JOIN openmrs.person_address pa " +
				" ON pa.person_id = p.person_id " +
				" WHERE m.money_receipt_date BETWEEN '"+startDate+"' AND '"+endDate+"'" +
					wh +
				" GROUP BY m.patient_uuid";
		try{
			report = sessionFactory.getCurrentSession()
					.createSQLQuery(sql).
					addScalar("patient_name",StandardBasicTypes.STRING).
					addScalar("hid",StandardBasicTypes.STRING).
					addScalar("mobile_number",StandardBasicTypes.STRING).
					addScalar("gender",StandardBasicTypes.STRING).
					addScalar("age",StandardBasicTypes.LONG).
					addScalar("reg_date",StandardBasicTypes.STRING).
					addScalar("last_visit_date",StandardBasicTypes.STRING).
					addScalar("visit_count",StandardBasicTypes.LONG).
					setResultTransformer(new AliasToBeanResultTransformer(AUHCVisitReport.class)).
					list();
			
//			AUHCVisitReport r = new AUHCVisitReport();
//			r.setPatient_name(sql);
//			report.add(r);
			log.error(sql);
			return report;
		}catch(Exception e){
			log.error(sql);
			log.error(e);
//			return new ArrayList<AUHCVisitReport>();
		}
		return null;
		
	}
	
	@Override
	 public String oldClientCount(String startDate,String endDate,String code){
		String ret = "";
//    Previous Version on SQL		
//		String wh = "";
//		if(!"0".equalsIgnoreCase(code))
//			wh += "       AND clinic_code='"+code+"' ";
//		String sql = ""
//				+ "SELECT Count(*) "
//				+ " FROM   (SELECT pmr.patient_uuid "
//				+ "	 FROM   psi_money_receipt pmr "
//				+ "	 JOIN (SELECT patient_uuid, "
//				+ "       Count(patient_uuid) AS total "
//				+ "      FROM   psi_money_receipt "
//				+ "      WHERE  money_receipt_date BETWEEN "
//				+ "        '"+startDate+"' AND '"+endDate+"' "
//				+ wh
//				+ "      GROUP  BY patient_uuid) AS newclient "
//				+ "  ON pmr.patient_uuid = newclient.patient_uuid "
//				+ "   "
//				+ "  WHERE  pmr.money_receipt_date < '"+startDate+"' "
//				+ "   "
//				+ "  GROUP  BY pmr.patient_uuid) AS tbl";
		String wh1 = "";
		String wh2 = "";
		if(!"0".equalsIgnoreCase(code)){
			wh1 += "	and clinic_code = '"+code+"' ";
			wh2 += "  and pmr.clinic_code = '"+code+"' ";
		}
		String sql1 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS oldPatientID, "
				+ "       table1.patient_uuid AS oldpatient, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+startDate+"' " +
						wh1
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+startDate+"' AND '"+endDate+"' "
				+ "       AND table1.patient_uuid IS NOT NULL " +
				wh2
				+ "  GROUP  BY pmr.patient_uuid) as tbl";
		String sql2 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS newpatientId, "
				+ "       table1.patient_uuid AS oldpatientid, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+startDate+"' " +
						wh1
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+startDate+"' AND '"+endDate+"' " +
						" and table1.patient_uuid is NULL" +
						wh2
				+ " GROUP  BY pmr.patient_uuid HAVING Count(pmr.patient_uuid) > 1) "
				+ " as tbl";
			
		try{
//			ret = sessionFactory.getCurrentSession()
//				.createSQLQuery(sql1).list().get(0).toString();
			String res1,res2;
			res1 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql1).list().get(0).toString();
			res2 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql2).list().get(0).toString();
			Long res = Long.parseLong(res1)+ Long.parseLong(res2);
			log.error(sql1);
			log.error(sql2);
			return res.toString();
			
		}catch(Exception e){
//			return e.toString();
			log.error(sql1);
			log.error(sql2);
			log.error(e.toString());
			return "0";
		}
		
//		return ret;
	}
	
	@Override
	 public String newClientCount(String startDate,String endDate,String code){
		 String ret = "";
		 /* String wh = "";
			if(!"0".equalsIgnoreCase(code))
				wh += "       AND p.clinic_code='"+code+"' ";
		 String sql = ""
				 + "SELECT Count(*) "
				 + "	 FROM "
				 + "	 (SELECT patient_uuid "
				 + "	 FROM   psi_money_receipt p "
				 + "     WHERE  p.money_receipt_date BETWEEN "
				 + "    '"+startDate+"' AND '"+endDate+"' "
				 + wh
				 + "    GROUP BY p.patient_uuid) "
				 + "    as p_tbl";*/
		 String wh1 = "";
			String wh2 = "";
			if(!"0".equalsIgnoreCase(code)){
				wh1 += "	and clinic_code = '"+code+"' ";
				wh2 += "  and pmr.clinic_code = '"+code+"' ";
			}
		 String sql = ""
				 + " SELECT COUNT(*) FROM "
				 + " (SELECT pmr.patient_uuid    AS newpatientId, "
				 + "       table1.patient_uuid AS oldpatientid, "
				 + "       Count(pmr.patient_uuid) as visitTaken "
				 + " FROM   psi_money_receipt pmr "
				 + "       LEFT JOIN (SELECT patient_uuid, "
				 + "                         Count(patient_uuid) AS total "
				 + "                  FROM   psi_money_receipt "
				 + "                  WHERE  DATE(money_receipt_date) < '"+startDate+"' " +
				 		wh1
				 + "                  GROUP  BY patient_uuid) AS table1 "
				 + "              ON pmr.patient_uuid = table1.patient_uuid "
				 + " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+startDate+"' AND '"+endDate+"' and " +
				 		"	table1.patient_uuid is NULL" +
				 		wh2
				 + " GROUP  BY pmr.patient_uuid) as tbl";
		 try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
//			 Long res = Long.parseLong(ret) 
//					 - Long.parseLong(oldClientCount(startDate,endDate,code));
			 log.error(sql);
			 return ret;
		 }catch(Exception e){
			 log.error(sql);
			 log.error(e.toString());
			 return "0";
		 }
		 
		
	 }
	@Override
	public String getDashboardNewClients(String startDate, String endDate,
			String code, String gender) {
		// TODO Auto-generated method stub
		String ret = "";
		 String wh = "";
		 if(!"0".equalsIgnoreCase(code))
			 wh += " and p.clinic_code = '"+code+"' ";
		 if(gender.equals("F")) wh += " and p.gender = 'F' ";
			else if(gender.equals("M")) wh += " and p.gender = 'M' ";
			else if(gender.equals("O")) wh += " and p.gender = 'O' ";
			else if(gender.length() == 2){
				if(gender.equals("MO"))
					wh += " and p.gender != 'F' ";
				else if(gender.equals("FO"))
					wh += "and p.gender != 'M' ";
				else if(gender.equals("MF")){
					
				}
			}
		 String sql = ""
				 + "SELECT Count(*) "
				 + "	 FROM "
				 + "	 (SELECT p.patient_uuid "
				 + "	 FROM   psi_money_receipt p "
				 + "     WHERE  p.money_receipt_date BETWEEN "
				 + "    '"+startDate+"' AND '"+endDate+"' "
				 + wh;
		 
				 sql +=  "    GROUP BY p.patient_uuid) "
						 + "    as p_tbl";
		 try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
			 Long res = Long.parseLong(ret) - Long.parseLong(getDashboardOldClients(startDate,endDate,code,gender));
			 log.error(sql);
			 return res.toString();
		 }catch(Exception e){
			 log.error(sql);
			 log.error(e.toString());
			 return "0";
		 }
	}
	@Override
	public String patientsServed(String startDate, String endDate, String code,String category) {
		// TODO Auto-generated method stub
		String ret = "";
		String sql = "";
		
		if(category == ""){
			String wh = "";
			if(!"0".equalsIgnoreCase(code))
				wh += " AND p.clinic_code = '"+code+"' ";
			sql = ""
				+ "SELECT count(distinct(p.patient_uuid)) as count "
				+ "FROM openmrs.psi_money_receipt p "
				+ "where DATE(p.money_receipt_date) BETWEEN "
				+ "'"+startDate+"' AND '"+endDate+"' "
				+ wh
				+ "AND p.is_complete = 1 "
				+ "";
		}
		else {
			String wh = "";
			if(!"0".equalsIgnoreCase(code))
				wh += " AND pmr.clinic_code = '"+code+"' ";
			sql = ""
					+ "SELECT COUNT(DISTINCT(pmr.patient_uuid)) as COUNT "
					+ "FROM openmrs.psi_money_receipt as pmr "
					+ "LEFT JOIN openmrs.psi_service_provision psp "
					+ "ON pmr.mid = psp.psi_money_receipt_id "
					+ "where DATE(pmr.money_receipt_date) BETWEEN "
					+ "'"+startDate+"' AND '"+endDate+"' "
					+ wh
					+ " AND psp.category = '"+category+"'"
					+" AND pmr.is_complete = 1 ";
		}
		try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
			 log.error(sql);
			 return ret;
			
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
	
	}
	
	@Override
	public String newRegistration(String startDate, String endDate, String code) {
		// TODO Auto-generated method stub
		String ret = "";
		String wh = "";
		if(!"0".equalsIgnoreCase(code))
			wh += " where PAT.person_attribute_type_id = 32  and PAT.value = '"+code+"'  ";
		String sql = ""
				+ "select count(*) from ( "
				+ "				 select distinct(pa.person_id) personId from person_attribute pa where "
				+ "		          pa.person_attribute_type_id = 40 and "
				+ "		          DATE(pa.value) between '"+startDate+"' and '"+endDate+"' "
				+ "		          ) as a "
				+ "		          join  ( SELECT distinct (PAT.person_id) personId "
				+ "		          FROM person_attribute as PAT "
				+ wh + "		          ) "
				+ "                 as b      on a.personId = b.personId";
		try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
			 log.error(sql);
			 return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
		
	}
	@Override
	public String totalServiceContact(String startDate, String endDate,
			String clinicCode, String provider) {
		// TODO Auto-generated method stub
		String ret = "";
		String sql = ""
				+ "SELECT COUNT(*) "
				+ "FROM openmrs.psi_service_provision p "
				+ "LEFT JOIN openmrs.psi_money_receipt mr "
				+ "ON p.psi_money_receipt_id = mr.mid "
				+ "WHERE DATE(p.money_receipt_date) BETWEEN "
				+ "'"+startDate+"' AND '"+endDate+"' "
				+ "AND mr.clinic_code = '"+clinicCode+"' "
				+ "AND mr.data_collector = '"+provider+"' "
				+" AND mr.is_complete=1 ";
		try{
			ret = sessionFactory.getCurrentSession()
					.createSQLQuery(sql).list().get(0).toString();
			log.error(sql);
		 return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
		
	}
	@Override
	public String patientsServedProvider(String startDate, String endDate,
			String code, String collector) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT COUNT(DISTINCT(pmr.patient_uuid)) as COUNT "
				+ "FROM openmrs.psi_money_receipt as pmr "
				+ "where DATE(pmr.money_receipt_date) BETWEEN "
				+ "'"+startDate+"' AND '"+endDate+"' "
				+ "AND pmr.clinic_code = '"+code+"' "
				+ "AND pmr.data_collector='"+collector+"' "
				+ "AND pmr.is_complete = 1";
		
		String ret = "";
		try{
			ret = sessionFactory.getCurrentSession()
					.createSQLQuery(sql).list().get(0).toString();
			log.error(sql);
			return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
		
	}
	@Override
	public String discountOnProvider(String startDate, String endDate,
			String code, String collector) {
		// TODO Auto-generated method stub
//		CONVERT(int,ROUND(SUM(Amount),0))
		String sql = ""
				+ "SELECT ROUND(SUM(m.total_discount),2) "
				+ "FROM openmrs.psi_money_receipt as m "
				+ "WHERE DATE(m.money_receipt_date) BETWEEN "
				+ "'"+startDate+"' AND '"+endDate+"' "
				+ "AND m.clinic_code='"+code+"' "
				+ "AND m.data_collector='"+collector+"' "
				+" AND m.is_complete=1";
		String ret = "";
		try{
			ret = sessionFactory.getCurrentSession()
					.createSQLQuery(sql).list().get(0).toString();
			log.error(sql);
			return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
	}
	@Override
	public String totalServiceContact(SearchFilterSlipTracking filter) {
		// TODO Auto-generated method stub
		String ret = "";
		String wh = "";
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
				(filter.getWlthPop() != "");
	
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( mr.wealth = '" + filter.getWlthAbleToPay() + 
					"' or mr.wealth = '"+filter.getWlthPoor()+
					"' or mr.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}
	
		if(spFlag == true){
			wh += " and ( mr.service_point = '" + filter.getSpCsp()+
					"' or mr.service_point = '" + filter.getSpSatelite()+
					"' or mr.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}
		if(!"0".equalsIgnoreCase(filter.getClinicCode()))
			wh += " and mr.clinic_code = '"+filter.getClinicCode()+"' ";
		
		if (!"".equalsIgnoreCase(filter.getCollector())){
			wh += " and mr.data_collector = '" + filter.getCollector()+"' ";
		}
		String sql = ""
				+ "SELECT COUNT(*) "
				+ "FROM openmrs.psi_service_provision p "
				+ "LEFT JOIN openmrs.psi_money_receipt mr "
				+ "ON p.psi_money_receipt_id = mr.mid "
				+ "WHERE DATE(p.money_receipt_date) BETWEEN "
				+ "'"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"' "
				+" AND mr.is_complete = 1 ";
				
//				+ " AND mr.clinic_code = '"+filter.getCollector()+"' "
//				+ " AND mr.data_collector = '"+filter.getCollector()+"' ";
		
		sql += wh;
		
		try{
			ret = sessionFactory.getCurrentSession()
					.createSQLQuery(sql).list().get(0).toString();
			log.error(sql);
		 return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
	}

	@Override
	public AUHCDashboardCard getComprehensiveDashboardCard(
			List<AUHCComprehensiveReport> report, SearchFilterReport filter) {
		// TODO Auto-generated method stub
		AUHCDashboardCard dashboardCard = new AUHCDashboardCard();
		
		// Total Revenue, Total Discount and Total Service Contact Calculation - Start
		 double revenue_earned = 0.0;
		 double discount = 0.0;
		 
		 float service_contact = 0;
		 
		 for(int i = 0; i < report.size(); i++){
			 
			 revenue_earned += Double.parseDouble(report.get(i).getRevenue_total());
			 
			 discount += Double.parseDouble(report.get(i).getDiscount_total());
			 
			 service_contact += Double.parseDouble(report.get(i).getService_total());
		 }
		 
		 dashboardCard.setRevenueEarned(String.valueOf((long)revenue_earned));
		 dashboardCard.setTotalDiscount(String.valueOf((long)discount));
		 dashboardCard.setTotalServiceContact(String.valueOf((long)service_contact));
		// Total Revenue, Total Discount and Total Service Contact Calculation - End
		 
		 //Old Client Calculation - Start
		 dashboardCard.setOldClients(oldClientCount(filter.getStart_date(),
				 		filter.getEnd_date(),filter.getClinic_code()));
		//Old Client Calculation - End
		
		 //New Client Calculation - Start
		 dashboardCard.setNewClients(newClientCount(filter.getStart_date(),
				 filter.getEnd_date(),filter.getClinic_code()));
		//New Client Calculation - End
		 
		// Patients Served Calculation - Start
		 dashboardCard.setPatientServed(patientsServed(filter.getStart_date(),
				 	filter.getEnd_date(),filter.getClinic_code(),filter.getService_category()));
		//Patients Served Calculation - End
		 
		 //New Registration Calculation - Start
		 dashboardCard.setNewRegistration(newRegistration(filter.getStart_date(),
				 filter.getEnd_date(),filter.getClinic_code()));
		//New Registration Calculation - Start
		return dashboardCard;
	}

	@Override
	public AUHCDashboardCard getProviderDashboardCard(List<PSIReport> report,
			SearchFilterReport filter) {
		// TODO Auto-generated method stub
		String sql = "";
		AUHCDashboardCard dashboardCard = new AUHCDashboardCard();
		
		//New Registration, OldClient, New Client Calculation - Start
		dashboardCard.setNewRegistration(newRegistration(filter.getStart_date(),
				filter.getEnd_date(),filter.getClinic_code()));
		
		dashboardCard.setOldClients(oldClientCount(filter.getStart_date(),
				filter.getEnd_date(),filter.getClinic_code(),filter.getData_collector()));
		
		dashboardCard.setNewClients(newClientCount(filter.getStart_date(),
				filter.getEnd_date(),filter.getClinic_code(),filter.getData_collector()));
		//New Registration, OldClient, New Client Calculation - End
		
		//Total Service Contact Calculation - Start
		dashboardCard.setTotalServiceContact(totalServiceContact(filter.getStart_date(),
				filter.getEnd_date(),filter.getClinic_code(),filter.getData_collector()));
		
		//Total Service Contact Calculation - End
		
		// Patients Served Calculation - Start
		dashboardCard.setPatientServed(patientsServedProvider(filter.getStart_date(),filter.getEnd_date(),
				filter.getClinic_code(),filter.getData_collector()));
		// Patients Served Calculation - End
		//Total Revenue - Start
		float revenue = 0;
		for(int i = 0; i < report.size();i++)
			revenue += Float.parseFloat(report.get(i).getTotal_());
		
		dashboardCard.setRevenueEarned(String.valueOf((long)revenue));
		//Total Revenue - End
		
		//Total Discount - Start
		 dashboardCard.setTotalDiscount(discountOnProvider(filter.getStart_date(),filter.getEnd_date(),filter.getClinic_code(),
				 filter.getData_collector()));	
		//Total Discount - End
		return dashboardCard;
	}

	@Override
	public String totalServiceContact(String startDate, String endDate,
			String code) {
		
			String ret = "";
			String wh = "";
			if(code.equals("0")){
				
			}
			else {
				wh += " AND mr.clinic_code = '"+code+"' ";
			}
			String sql = ""
					+ "SELECT COUNT(*) "
					+ "FROM openmrs.psi_service_provision p "
					+ "LEFT JOIN openmrs.psi_money_receipt mr "
					+ "ON p.psi_money_receipt_id = mr.mid "
					+ "WHERE p.money_receipt_date BETWEEN "
					+ "'"+startDate+"' AND '"+endDate+"' "
					+ " AND mr.is_complete=1 "
					+ wh;
			try{
				ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
				log.error(sql);
			 return ret;
			}catch(Exception e){
				log.error(sql);
				log.error(e.toString());
				return "0";
			}
	}

	@Override
	public List<AUHCVisitReport> getVisitReport(SearchFilterVisitReport filter) {
		// TODO Auto-generated method stub
		List<AUHCVisitReport> report = new ArrayList<AUHCVisitReport>();
		String wh = "";
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
				(filter.getWlthPop() != "");
	
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( m.wealth = '" + filter.getWlthAbleToPay() + 
					"' or m.wealth = '"+filter.getWlthPoor()+
					"' or m.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}
	
		if(spFlag == true){
			wh += " and ( m.service_point = '" + filter.getSpCsp()+
					"' or m.service_point = '" + filter.getSpSatelite()+
					"' or m.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}
		if(!"0".equalsIgnoreCase(filter.getClinicCode()))
			wh += " AND m.clinic_code = '"+filter.getClinicCode()+"' ";
		String sql = " SELECT m.patient_name as patient_name,pi.identifier as hid, " +
				" m.contact as mobile_number, m.gender as gender, " +
				" CONCAT(TIMESTAMPDIFF(YEAR, p.birthdate, CURDATE()),'Y '," +
				" TIMESTAMPDIFF(MONTH, p.birthdate, CURDATE())%12,'M '," +
				" FLOOR(TIMESTAMPDIFF(DAY, p.birthdate, CURDATE())% 30.4375),'D ') as age, " +
				" DATE_FORMAT(m.patient_registered_date, '%d.%m.%Y') as reg_date, " +
				" MAX(DATE_FORMAT(m.money_receipt_date, '%d.%m.%Y')) as last_visit_date, " +
				" COUNT(m.patient_uuid) as visit_count" +
				" FROM openmrs.psi_money_receipt  m " +
				" JOIN openmrs.person p " +
				" ON m.patient_uuid = p.uuid " +
				" JOIN openmrs.patient_identifier pi " +
				" ON p.person_id = pi.patient_id  " +
				" LEFT JOIN openmrs.person_address pa " +
				" ON pa.person_id = p.person_id " +
				" WHERE DATE(m.money_receipt_date) BETWEEN '"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"'" +
					wh +
				" GROUP BY m.patient_uuid";
		try{
			report = sessionFactory.getCurrentSession()
					.createSQLQuery(sql).
					addScalar("patient_name",StandardBasicTypes.STRING).
					addScalar("hid",StandardBasicTypes.STRING).
					addScalar("mobile_number",StandardBasicTypes.STRING).
					addScalar("gender",StandardBasicTypes.STRING).
					addScalar("age",StandardBasicTypes.STRING).
					addScalar("reg_date",StandardBasicTypes.STRING).
					addScalar("last_visit_date",StandardBasicTypes.STRING).
					addScalar("visit_count",StandardBasicTypes.LONG).
					setResultTransformer(new AliasToBeanResultTransformer(AUHCVisitReport.class)).
					list();
			log.error(sql);
			return report;
		}
		catch(Exception e){
			log.error(sql);
			log.error(e.toString());
		}
		return null;
		
	}

	@Override
	public String newRegistration(SearchFilterVisitReport filter) {
		String ret = "";
		String wh="";
		if(!"0".equalsIgnoreCase(filter.getClinicCode()))
			wh += "		         where PAT.person_attribute_type_id = 32  and PAT.value = '"+filter.getClinicCode()+"'   ) "
					+ "                 as b      on a.personId = b.personId";
		String sql = ""
				+ "select count(*) from ( "
				+ "				 select distinct(pa.person_id) personId from person_attribute pa where "
				+ "		          pa.person_attribute_type_id = 40 and "
				+ "		          DATE(pa.value) between '"+filter.getStartDateSlip()+"' and '"+filter.getEndDateSlip()+"' "
				+ "		          ) as a "
				+ "		          join  ( SELECT distinct (PAT.person_id) personId "
				+ "		          FROM person_attribute as PAT "
				+ wh;
		try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
			 log.error(sql);
		
			 return ret;
		}catch(Exception e){
			 log.error(sql);
			 log.error(e.toString());
			return "0";
		}
	}

	@Override
	public String oldClientCount(SearchFilterVisitReport filter) {
		// TODO Auto-generated method stub
		String ret = "";
		 String wh = "";
		 String wh1= "";
		  Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
		
			Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
					(filter.getSpStatic() != "");
			if(wealthFlag == true) {
				wh += " and ( wealth = '" + filter.getWlthAbleToPay() + 
						"' or wealth = '"+filter.getWlthPoor()+
						"' or wealth = '"+ filter.getWlthPop()+
						"' ) ";
				wh1 += " and ( pmr.wealth = '" + filter.getWlthAbleToPay() + 
						"' or pmr.wealth = '"+filter.getWlthPoor()+
						"' or pmr.wealth = '"+ filter.getWlthPop()+
						"' ) ";
			}
		
			if(spFlag == true){
				wh += " and ( service_point = '" + filter.getSpCsp()+
						"' or service_point = '" + filter.getSpSatelite()+
						"' or service_point = '"+ filter.getSpStatic()+
						"' ) ";
				wh1 += " and ( pmr.service_point = '" + filter.getSpCsp()+
						"' or pmr.service_point = '" + filter.getSpSatelite()+
						"' or pmr.service_point = '"+ filter.getSpStatic()+
						"' ) "; 
			}
			if(!"0".equalsIgnoreCase(filter.getClinicCode())) {
				wh += "       AND clinic_code='"+filter.getClinicCode()+"' ";
				wh1 += "       AND pmr.clinic_code='"+filter.getClinicCode()+"' ";
			}
		
//		String sql = ""
//				+ "SELECT Count(*) "
//				+ " FROM   (SELECT pmr.patient_uuid "
//				+ "	 FROM   psi_money_receipt pmr "
//				+ "	 JOIN (SELECT patient_uuid, "
//				+ "       Count(patient_uuid) AS total "
//				+ "      FROM   psi_money_receipt "
//				+ "      WHERE  money_receipt_date BETWEEN "
//				+ "        '"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"' "
//				+ wh
//				+ "      GROUP  BY patient_uuid) AS newclient "
//				+ "  ON pmr.patient_uuid = newclient.patient_uuid "
//				+ "   "
//				+ "  WHERE  pmr.money_receipt_date < '"+filter.getStartDateSlip()+"' "
//				+ "   "
//				+ "  GROUP  BY pmr.patient_uuid) AS tbl";
		
		String sql1 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS oldPatientID, "
				+ "       table1.patient_uuid AS oldpatient, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+filter.getStartDateSlip()+"'" +
						wh
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"' "
				+ "       AND table1.patient_uuid IS NOT NULL " +
				wh1
				+ " "
				+ " GROUP  BY pmr.patient_uuid ) as tbl";
		
		String sql2 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS newpatientId, "
				+ "       table1.patient_uuid AS oldpatientid, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+filter.getStartDateSlip()+"' " +
				"	 " + wh
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"' " +
						" and table1.patient_uuid is NULL" +
						"   " + wh1
				+ " GROUP  BY pmr.patient_uuid HAVING Count(pmr.patient_uuid) > 1 ) "
				+ " as tbl";
		try{
			String res1,res2;
			res1 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql1).list().get(0).toString();
			res2 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql2).list().get(0).toString();
			Long res = Long.parseLong(res1)+ Long.parseLong(res2);
			log.error(sql1);
			log.error(sql2);
			return res.toString();
			
		}catch(Exception e){
			log.error(sql1);
			log.error(sql2);
			log.error(e.toString());
			return "0";
		}
		
		
	}

	@Override
	public String newClientCount(SearchFilterVisitReport filter) {
		// TODO Auto-generated method stub
		 String ret = "";
		 String wh = "";
		 String wh1= "";
		  Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
		
			Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
					(filter.getSpStatic() != "");
			if(wealthFlag == true) {
				wh += " and ( wealth = '" + filter.getWlthAbleToPay() + 
						"' or wealth = '"+filter.getWlthPoor()+
						"' or wealth = '"+ filter.getWlthPop()+
						"' ) ";
				wh1 += " and ( pmr.wealth = '" + filter.getWlthAbleToPay() + 
						"' or pmr.wealth = '"+filter.getWlthPoor()+
						"' or pmr.wealth = '"+ filter.getWlthPop()+
						"' ) ";
			}
		
			if(spFlag == true){
				wh += " and ( service_point = '" + filter.getSpCsp()+
						"' or service_point = '" + filter.getSpSatelite()+
						"' or service_point = '"+ filter.getSpStatic()+
						"' ) ";
				wh1 += " and ( pmr.service_point = '" + filter.getSpCsp()+
						"' or pmr.service_point = '" + filter.getSpSatelite()+
						"' or pmr.service_point = '"+ filter.getSpStatic()+
						"' ) "; 
			}
			if(!"0".equalsIgnoreCase(filter.getClinicCode())) {
				wh += "       AND clinic_code='"+filter.getClinicCode()+"' ";
				wh1 += "       AND pmr.clinic_code='"+filter.getClinicCode()+"' ";
			}
//		 String sql = ""
//				 + "SELECT Count(*) "
//				 + "	 FROM "
//				 + "	 (SELECT patient_uuid "
//				 + "	 FROM   psi_money_receipt p "
//				 + "     WHERE  p.money_receipt_date BETWEEN "
//				 + "    '"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"' "
//				 + wh
//				 + "    GROUP BY p.patient_uuid) "
//				 + "    as p_tbl";
			String sql = ""
					+ " SELECT COUNT(*) FROM "
					+ " (SELECT pmr.patient_uuid    AS newpatientId, "
					+ "       table1.patient_uuid AS oldpatientid, "
					+ "       Count(pmr.patient_uuid) as visitTaken "
					+ " FROM   psi_money_receipt pmr "
					+ "       LEFT JOIN (SELECT patient_uuid, "
					+ "                         Count(patient_uuid) AS total "
					+ "                  FROM   psi_money_receipt "
					+ "                  WHERE  DATE(money_receipt_date) < '"+filter.getStartDateSlip()+"'" +
							wh
					+ "                  GROUP  BY patient_uuid) AS table1 "
					+ "              ON pmr.patient_uuid = table1.patient_uuid "
					+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"' and table1.patient_uuid is NULL " +
							wh1
					+ " GROUP  BY pmr.patient_uuid) as tbl ";
			
		 try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
			log.error(sql);
			 return ret;
		 }catch(Exception e){
			 log.error(sql);
			 log.error(e.toString());
			 return "0";
		 }
	}

	@Override
	public String totalServiceContact(SearchFilterVisitReport filter) {
		// TODO Auto-generated method stub
		String ret = "";
		String wh = "";
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
				(filter.getWlthPop() != "");
	
		Boolean spFlag = (filter.getSpCsp() != "") || (filter.getSpSatelite() != "")||
				(filter.getSpStatic() != "");
		if(wealthFlag == true) {
			wh += "and ( mr.wealth = '" + filter.getWlthAbleToPay() + 
					"' or mr.wealth = '"+filter.getWlthPoor()+
					"' or mr.wealth = '"+ filter.getWlthPop()+
					"' ) ";
		}
	
		if(spFlag == true){
			wh += " and ( mr.service_point = '" + filter.getSpCsp()+
					"' or mr.service_point = '" + filter.getSpSatelite()+
					"' or mr.service_point = '"+ filter.getSpStatic()+
					"' ) ";
		}
		if(!"0".equalsIgnoreCase(filter.getClinicCode())) {
			wh += " AND mr.clinic_code = '"+filter.getClinicCode()+"' ";
		}
		String sql = ""
				+ "SELECT COUNT(*) "
				+ "FROM openmrs.psi_service_provision p "
				+ "LEFT JOIN openmrs.psi_money_receipt mr "
				+ "ON p.psi_money_receipt_id = mr.mid "
				+ "WHERE DATE(p.money_receipt_date) BETWEEN "
				+ "'"+filter.getStartDateSlip()+"' AND '"+filter.getEndDateSlip()+"' "
				+ " AND mr.is_complete=1 "
				+ wh;
		try{
			ret = sessionFactory.getCurrentSession()
					.createSQLQuery(sql).list().get(0).toString();
			log.error(sql);
		 return ret;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return "0";
		}
	}

	@Override
	public List<AUHCRegistrationReport> getRegistrationReport(
			SearchFilterRegistrationReport filter) {
	// TODO Auto-generated method stub
		
		
		String sql = ""
				+ "SELECT "
				+ "	   CONCAT(pname.given_name,\" \",pname.family_name) AS patient_name, "
				+ "         temp4.UIC as uic, "
				+ "       pi.identifier         AS health_id, "
				+ "        temp1.phoneno         AS mobile_no, "
				+ "       p.gender              AS gender, "
				+ "       temp5.registeredDate  AS register_date, "
				+ "        CONCAT(TIMESTAMPDIFF(YEAR, p.birthdate, CURDATE()) ,'Y ' ,TIMESTAMPDIFF(MONTH, p.birthdate, CURDATE()) %12 ," +
				"					'M ',FLOOR(TIMESTAMPDIFF(DAY,p.birthdate,CURDATE())% 30.4375)  ,'D ') AS age, "
				+ "        paddress.address3 as cc, "
				+ "       p.uuid              AS patient_uuid "
				+ "       FROM   person_name pname "
				+ "	   left JOIN patient_identifier pi "
				+ "              ON pname.person_id = pi.patient_id "
				+ "        JOIN person p "
				+ "              ON p.person_id = pi.patient_id "
				+ " "
				+ "        JOIN (SELECT pat.person_attribute_type_id, "
				+ "                         pat.value AS phoneNo, "
				+ "                         pat.person_id "
				+ "                  FROM   person_attribute pat "
				+ "                  WHERE  pat.person_attribute_type_id = 42) AS temp1 "
				+ "              ON pi.patient_id = temp1.person_id "
				+ "        JOIN (SELECT pa.person_attribute_type_id, "
				+ "                         pa.value AS UIC, "
				+ "                         pa.person_id "
				+ "                  FROM   person_attribute pa "
				+ "                  WHERE  pa.person_attribute_type_id = 34) AS temp4 "
				+ "              ON pi.patient_id = temp4.person_id "
				+ "        JOIN (SELECT person_attribute_temp5.person_attribute_type_id, "
				+ "                         person_attribute_temp5.value AS registeredDate , "
				+ "                         person_attribute_temp5.person_id "
				+ "                  FROM   person_attribute person_attribute_temp5 "
				+ "                  WHERE  person_attribute_temp5.person_attribute_type_id = 40) AS temp5 "
				+ "              ON pi.patient_id = temp5.person_id "
				+ "        JOIN (SELECT person_attribute_temp6.person_attribute_type_id, "
				+ "                         person_attribute_temp6.value AS clinicCode , "
				+ "                         person_attribute_temp6.person_id "
				+ "                  FROM   person_attribute person_attribute_temp6 "
				+ "                  WHERE  person_attribute_temp6.person_attribute_type_id = 32) AS temp6 "
				+ "              ON pi.patient_id = temp6.person_id "
				+ " JOIN (SELECT person_attribute_temp7.person_attribute_type_id, "
				+ "                         person_attribute_temp7.value AS financialstatus , "
				+ "                         person_attribute_temp7.person_id "
				+ "                  FROM   person_attribute person_attribute_temp7 "
				+ "                  WHERE  person_attribute_temp7.person_attribute_type_id = 28) AS temp7 "
				+ "              ON pi.patient_id = temp7.person_id"
				+ "        left JOIN person_address paddress "
				+ "              ON paddress.person_id = pi.patient_id "
				+ "              where DATE(temp5.registeredDate) BETWEEN '"+filter.getStartDate()+"' and " +
						"	'"+filter.getEndDate()+"' and "
				+ "              pname.preferred = 1 ";
				
		
		
		if(filter.getGender().equals("F")) sql += " and p.gender = 'F' ";
		else if(filter.getGender().equals("M")) sql += " and p.gender = 'M' ";
		else if(filter.getGender().equals("O")) sql += " and p.gender = 'O' ";
		else if(filter.getGender().length() == 2){
			if(filter.getGender().equals("MO"))
				sql += " and p.gender != 'F' ";
			else if(filter.getGender().equals("FO"))
				sql += "and p.gender != 'M' ";
			else if(filter.getGender().equals("MF")){
				
			}
		}
		if(!"0".equalsIgnoreCase(filter.getClinicCode()))
			sql += " and temp6.clinicCode = '"+filter.getClinicCode()+"' ";
		Map<String,String> wealthMap = new HashMap<String,String>();
		wealthMap.put("Able to Pay", "3627");
		wealthMap.put("Poor","3626");
		wealthMap.put("PoP","3625");
		
		Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
				(filter.getWlthPop() != "");
		
		if(wealthFlag == true) {
			sql += "and ( temp7.financialstatus = '" + wealthMap.get(filter.getWlthAbleToPay()) + 
					"' or temp7.financialstatus = '"+ wealthMap.get(filter.getWlthPoor())+
					"' or temp7.financialstatus = '"+ wealthMap.get(filter.getWlthPop())+
					"' ) ";
		}
	
		
		sql +=  " GROUP by p.uuid";
		
		List<AUHCRegistrationReport> report = new ArrayList<AUHCRegistrationReport>();
		try{
			report = sessionFactory.getCurrentSession().createSQLQuery(sql).
					addScalar("patient_name",StandardBasicTypes.STRING).
					addScalar("uic",StandardBasicTypes.STRING).
					addScalar("health_id",StandardBasicTypes.STRING).
					addScalar("mobile_no",StandardBasicTypes.STRING).
					addScalar("gender",StandardBasicTypes.STRING).
					addScalar("register_date",StandardBasicTypes.STRING).
					addScalar("age",StandardBasicTypes.STRING).
					addScalar("cc",StandardBasicTypes.STRING).
					addScalar("patient_uuid",StandardBasicTypes.STRING).
					setResultTransformer(new AliasToBeanResultTransformer(AUHCRegistrationReport.class)).
					list();
			log.error(sql);
			return report;
		}catch(Exception e){
			log.error(sql);
			log.error(e.toString());
			return report;
		}
	}

	@Override
	public String getDashboardOldClients(SearchFilterRegistrationReport filter) {
		// TODO Auto-generated method stub
		String wh="";
		String wh1 = "";
		 if(!"0".equalsIgnoreCase(filter.getClinicCode())) {
			 wh += " and clinic_code = '"+filter.getClinicCode()+"' ";
			 wh1 += " and pmr.clinic_code = '"+filter.getClinicCode()+"' " ;
		 }
		 if(filter.getGender().equals("F")) {
			 wh += " and gender = 'F' ";
			 wh1 += " and pmr.gender = 'F' ";
		 }
			else if(filter.getGender().equals("M")) {
				wh += " and gender = 'M' ";
				wh1 += " and pmr.gender = 'M' ";
			}
			else if(filter.getGender().equals("O")){ 
				wh += " and gender = 'O' ";
				wh1 += " and pmr.gender = 'O' ";
			}
			else if(filter.getGender().length() == 2){
				if(filter.getGender().equals("MO")) {
					wh += " and gender != 'F' ";
					wh1 += " and pmr.gender != 'F' ";
				}
				else if(filter.getGender().equals("FO")){
					wh += " and gender != 'M' ";
					wh1 += " and pmr.gender != 'M' ";
				}
				else if(filter.getGender().equals("MF")){
					
				}
			}
		 Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
			
			if(wealthFlag == true) {
				wh += "and ( wealth = '" + filter.getWlthAbleToPay() + 
						"' or wealth = '"+filter.getWlthPoor()+
						"' or wealth = '"+ filter.getWlthPop()+
						"' ) ";
				wh1 += "and ( pmr.wealth = '" + filter.getWlthAbleToPay() + 
						"' or pmr.wealth = '"+filter.getWlthPoor()+
						"' or pmr.wealth = '"+ filter.getWlthPop()+
						"' ) ";
			}
		
//		String sql = "SELECT Count(*) "+
//				" FROM   (SELECT pmr.patient_uuid "+ 
//				" FROM   psi_money_receipt pmr "+ 
//				" JOIN (SELECT patient_uuid, "+ 
//                 "  Count(patient_uuid) AS total "+ 
//                 " FROM   psi_money_receipt "+
//                 " WHERE  money_receipt_date BETWEEN "+
//                    "'"+filter.getStartDate()+"' AND '"+filter.getEndDate()+"' "+
//                 wh+
//                 " GROUP  BY patient_uuid) AS newclient "+ 
//             " ON pmr.patient_uuid = newclient.patient_uuid "+ 
//             " WHERE  pmr.money_receipt_date < '"+filter.getStartDate()+"' "+
//             
//             " GROUP  BY pmr.patient_uuid) AS tbl ";
		String sql1 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS oldPatientID, "
				+ "       table1.patient_uuid AS oldpatient, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+filter.getStartDate()+"' " +
						wh
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+filter.getStartDate()+"' AND '"+filter.getEndDate()+"' "
				+ "       AND table1.patient_uuid IS NOT NULL " +
				 wh1
				
				+ " GROUP  BY pmr.patient_uuid ) "
				+ " as tbl";
		String sql2 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS newpatientId, "
				+ "       table1.patient_uuid AS oldpatientid, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+filter.getStartDate()+"' " +
				wh
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+filter.getStartDate()+"' AND '"+filter.getEndDate()+"' " +
						" and table1.patient_uuid is NULL" +
						wh1
				+ " GROUP  BY pmr.patient_uuid HAVING Count(pmr.patient_uuid) > 1) "
				+ " as tbl";
		try{
			String res1,res2;
			res1 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql1).list().get(0).toString();
			res2 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql2).list().get(0).toString();
			Long res = Long.parseLong(res1)+ Long.parseLong(res2);
			log.error(sql1);
			log.error(sql2);
			return res.toString();
		}catch(Exception e){
			log.error(sql1);
			log.error(sql2);
			log.error(e.toString());
		 return "0";
		}
	}

	@Override
	public String getDashboardNewClients(SearchFilterRegistrationReport filter) {
		// TODO Auto-generated method stub
		String ret = "";
		 String wh = "";
		 String wh1 = "";
		 if(!"0".equalsIgnoreCase(filter.getClinicCode())) {
			 wh += " and clinic_code = '"+filter.getClinicCode()+"' ";
			 wh1 += " and pmr.clinic_code = '"+filter.getClinicCode()+"' " ;
		 }
		 if(filter.getGender().equals("F")) {
			 wh += " and gender = 'F' ";
			 wh1 += " and pmr.gender = 'F' ";
		 }
			else if(filter.getGender().equals("M")) {
				wh += " and gender = 'M' ";
				wh1 += " and pmr.gender = 'M' ";
			}
			else if(filter.getGender().equals("O")){ 
				wh += " and gender = 'O' ";
				wh1 += " and pmr.gender = 'O' ";
			}
			else if(filter.getGender().length() == 2){
				if(filter.getGender().equals("MO")) {
					wh += " and gender != 'F' ";
					wh1 += " and pmr.gender != 'F' ";
				}
				else if(filter.getGender().equals("FO")){
					wh += " and gender != 'M' ";
					wh1 += " and pmr.gender != 'M' ";
				}
				else if(filter.getGender().equals("MF")){
					
				}
			}
		 Boolean wealthFlag = (filter.getWlthAbleToPay() != "") || (filter.getWlthPoor() != "") || 
					(filter.getWlthPop() != "");
			
			if(wealthFlag == true) {
				wh += "and ( wealth = '" + filter.getWlthAbleToPay() + 
						"' or wealth = '"+filter.getWlthPoor()+
						"' or wealth = '"+ filter.getWlthPop()+
						"' ) ";
				wh1 += "and ( pmr.wealth = '" + filter.getWlthAbleToPay() + 
						"' or pmr.wealth = '"+filter.getWlthPoor()+
						"' or pmr.wealth = '"+ filter.getWlthPop()+
						"' ) ";
			}
//		 String sql = ""
//				 + "SELECT Count(*) "
//				 + "	 FROM "
//				 + "	 (SELECT p.patient_uuid "
//				 + "	 FROM   psi_money_receipt p "
//				 + "     WHERE  p.money_receipt_date BETWEEN "
//				 + "    '"+filter.getStartDate()+"' AND '"+filter.getEndDate()+"' "
//				 + wh;
//		 
//				 sql +=  "    GROUP BY p.patient_uuid) "
//						 + "    as p_tbl";
			String sql = ""
					+ " SELECT COUNT(*) FROM "
					+ " (SELECT pmr.patient_uuid    AS newpatientId, "
					+ "       table1.patient_uuid AS oldpatientid, "
					+ "       Count(pmr.patient_uuid) as visitTaken "
					+ " FROM   psi_money_receipt pmr "
					+ "       LEFT JOIN (SELECT patient_uuid, "
					+ "                         Count(patient_uuid) AS total "
					+ "                  FROM   psi_money_receipt "
					+ "                  WHERE  DATE(money_receipt_date) < '"+filter.getStartDate()+"'" +
							 wh
					+ "                  GROUP  BY patient_uuid) AS table1 "
					+ "              ON pmr.patient_uuid = table1.patient_uuid "
					+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+filter.getStartDate()+"' AND '"+filter.getEndDate()+"' and table1.patient_uuid is NULL " +
							wh1
					+ " GROUP  BY pmr.patient_uuid) as tbl";
		 try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
//			 Long res = Long.parseLong(ret) - Long.parseLong(getDashboardOldClients(filter));
			 log.error(sql);
			 return ret;
		 }catch(Exception e){
			 log.error(sql);
			 log.error(e.toString());
			 return "0";
		 }
	}

	@Override
	public String oldClientCount(String startDate, String endDate, String code,
			String collector) {
		String ret = "";
		
// Previous Code		
//		String wh = "";
//		if(!"0".equalsIgnoreCase(code))
//			wh += "       AND clinic_code='"+code+"' ";
//		if(!"".equalsIgnoreCase(collector))
//			wh += "    AND data_collector='"+collector+"' ";
//		String sql = ""
//				+ "SELECT Count(*) "
//				+ " FROM   (SELECT pmr.patient_uuid "
//				+ "	 FROM   psi_money_receipt pmr "
//				+ "	 JOIN (SELECT patient_uuid, "
//				+ "       Count(patient_uuid) AS total "
//				+ "      FROM   psi_money_receipt "
//				+ "      WHERE  money_receipt_date BETWEEN "
//				+ "        '"+startDate+"' AND '"+endDate+"' "
//				+ wh
//				+ "      GROUP  BY patient_uuid) AS newclient "
//				+ "  ON pmr.patient_uuid = newclient.patient_uuid "
//				+ "   "
//				+ "  WHERE  pmr.money_receipt_date < '"+startDate+"' "
//				+ "   "
//				+ "  GROUP  BY pmr.patient_uuid) AS tbl";
		String wh1 = "";
		String wh2 = "";
		if(!"0".equalsIgnoreCase(code)){
			wh1 += "	and clinic_code = '"+code+"' ";
			wh2 += "  and pmr.clinic_code = '"+code+"' ";
		}
		String wh3 = "";
		String wh4 = "";
		if(!"".equalsIgnoreCase(collector)) {
			wh3 += "    AND data_collector='"+collector+"' ";
			wh4 += " and pmr.data_collector='"+collector+"' ";
		}
		String sql1 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS oldPatientID, "
				+ "       table1.patient_uuid AS oldpatient, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+startDate+"' " +
						wh1
						+ wh3
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+startDate+"' AND '"+endDate+"' "
				+ "       AND table1.patient_uuid IS NOT NULL " +
				wh2
				+ wh4
				+ "  GROUP  BY pmr.patient_uuid) as tbl";
		String sql2 = ""
				+ " SELECT COUNT(*) FROM "
				+ " (SELECT pmr.patient_uuid    AS newpatientId, "
				+ "       table1.patient_uuid AS oldpatientid, "
				+ "       Count(pmr.patient_uuid) as visitTaken "
				+ " FROM   psi_money_receipt pmr "
				+ "       LEFT JOIN (SELECT patient_uuid, "
				+ "                         Count(patient_uuid) AS total "
				+ "                  FROM   psi_money_receipt "
				+ "                  WHERE  DATE(money_receipt_date) < '"+startDate+"' " +
						wh1
						+ wh3
				+ "                  GROUP  BY patient_uuid) AS table1 "
				+ "              ON pmr.patient_uuid = table1.patient_uuid "
				+ " WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+startDate+"' AND '"+endDate+"' " +
						" and table1.patient_uuid is NULL" +
						wh2
						+ wh4
				+ "GROUP  BY pmr.patient_uuid HAVING Count(pmr.patient_uuid) > 1) "
				+ "as tbl";
			
		try{
//			ret = sessionFactory.getCurrentSession()
//				.createSQLQuery(sql1).list().get(0).toString();
			String res1,res2;
			res1 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql1).list().get(0).toString();
			res2 = sessionFactory.getCurrentSession()
					.createSQLQuery(sql2).list().get(0).toString();
			Long res = Long.parseLong(res1)+ Long.parseLong(res2);
			log.error(sql1);
			log.error(sql2);
			return res.toString();
			
		}catch(Exception e){
			log.error(sql1);
			log.error(sql2);
			log.error(e.toString());
			return "0";
		}
			
		
	}

	@Override
	public String newClientCount(String startDate, String endDate, String code,
			String collector) {
		String ret = "";
//		Prevoius Code
//		String wh = "";
//			if(!"0".equalsIgnoreCase(code))
//				wh += "       AND p.clinic_code='"+code+"' ";
//			if(!"".equalsIgnoreCase(collector))
//				wh += "   AND p.data_collector='"+collector+"' ";
//		 String sql = ""
//				 + "SELECT Count(*) "
//				 + "	 FROM "
//				 + "	 (SELECT patient_uuid "
//				 + "	 FROM   psi_money_receipt p "
//				 + "     WHERE  p.money_receipt_date BETWEEN "
//				 + "    '"+startDate+"' AND '"+endDate+"' "
//				 + wh
//				 + "    GROUP BY p.patient_uuid) "
//				 + "    as p_tbl";
//		 try{
//			 ret = sessionFactory.getCurrentSession()
//						.createSQLQuery(sql).list().get(0).toString();
//			 Long res = Long.parseLong(ret) 
//					 - Long.parseLong(oldClientCount(startDate,endDate,code,collector));
//			 return res.toString();
//		 }catch(Exception e){
//			 return "0";
//		 }
		 String wh1 = "";
			String wh2 = "";
			if(!"0".equalsIgnoreCase(code)){
				wh1 += "	and clinic_code = '"+code+"' ";
				wh2 += "  and pmr.clinic_code = '"+code+"' ";
			}
			String wh3 = "";
			String wh4 = "";
			if(!"".equalsIgnoreCase(collector)){
				wh3 += "    AND data_collector='"+collector+"' ";
				wh4 += " AND pmr.data_collector='"+collector+"' ";
			}
		 String sql = ""
				 + "SELECT COUNT(*) FROM "
				 + "(SELECT pmr.patient_uuid    AS newpatientId, "
				 + "       table1.patient_uuid AS oldpatientid, "
				 + "       Count(pmr.patient_uuid) as visitTaken "
				 + "FROM   psi_money_receipt pmr "
				 + "       LEFT JOIN (SELECT patient_uuid, "
				 + "                         Count(patient_uuid) AS total "
				 + "                  FROM   psi_money_receipt "
				 + "                  WHERE  DATE(money_receipt_date) < '"+startDate+"' " +
				 		wh1
				 		+ wh3
				 + "                  GROUP  BY patient_uuid) AS table1 "
				 + "              ON pmr.patient_uuid = table1.patient_uuid "
				 + "WHERE  DATE(pmr.money_receipt_date) BETWEEN '"+startDate+"' AND '"+endDate+"' and " +
				 		"	table1.patient_uuid is NULL" +
				 		wh2
				 		+ wh4
				 + "GROUP  BY pmr.patient_uuid) as tbl";
		 try{
			 ret = sessionFactory.getCurrentSession()
						.createSQLQuery(sql).list().get(0).toString();
//			 Long res = Long.parseLong(ret) 
//					 - Long.parseLong(oldClientCount(startDate,endDate,code));
			 log.error(sql);
			 return ret;
		 }catch(Exception e){
			 log.error(sql);
			 log.error(e.toString());
			 return "0";
		 }
		 
	}

	

	

	
}
