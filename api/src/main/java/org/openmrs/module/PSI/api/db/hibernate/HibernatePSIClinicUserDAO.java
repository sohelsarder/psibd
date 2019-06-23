/**
 * 
 */
package org.openmrs.module.PSI.api.db.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.module.PSI.PSIClinicUser;
import org.openmrs.module.PSI.api.db.PSIClinicUserDAO;
import org.openmrs.module.PSI.dto.UserDTO;

/**
 * @author proshanto
 */
public class HibernatePSIClinicUserDAO implements PSIClinicUserDAO {
	
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
	public PSIClinicUser saveOrUpdate(PSIClinicUser psiClinicUser) {
		sessionFactory.getCurrentSession().saveOrUpdate(psiClinicUser);
		return psiClinicUser;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PSIClinicUser> getAll() {
		List<PSIClinicUser> psiClinicUsers = sessionFactory.getCurrentSession()
		        .createQuery("from PSIClinicUser  order by cuid desc").list();
		
		return psiClinicUsers;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PSIClinicUser findById(int id) {
		List<PSIClinicUser> lists = sessionFactory.getCurrentSession().createQuery("from PSIClinicUser where cuid = :id")
		        .setInteger("id", id).list();
		if (lists.size() != 0) {
			return lists.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public void delete(int id) {
		PSIClinicUser psiClinicUser = findById(id);
		if (psiClinicUser != null) {
			sessionFactory.getCurrentSession().delete(findById(id));
		} else {
			log.error("psiClinicUser is null with id" + id);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PSIClinicUser findByUserName(String username) {
		List<PSIClinicUser> lists = sessionFactory.getCurrentSession()
		        .createQuery("from PSIClinicUser where userName = :username order by cuid desc")
		        .setString("username", username).list();
		if (lists.size() != 0) {
			return lists.get(0);
		} else {
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PSIClinicUser findByUserNameAndClinicCode(String username, int clinicId) {
		List<PSIClinicUser> lists = sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from PSIClinicUser where userName = :username and psiClinicManagementId = :code  order by cuid desc")
		        .setString("username", username).setInteger("code", clinicId).list();
		if (lists.size() != 0) {
			return lists.get(0);
		} else {
			return null;
		}
		
	}
	
	@Override
	public UserDTO findByUserNameFromOpenmrs(String username) {
		List<Object[]> data = null;
		UserDTO userDTO = new UserDTO();
		
		String sql = "SELECT user_id,username FROM openmrs.users where username= :username  limit 1";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		data = query.setString("username", username).list();
		
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			Object[] objects = (Object[]) iterator.next();
			userDTO.setId(Integer.parseInt(objects[0].toString()));
			userDTO.setUsername(objects[1].toString());
			
		}
		return userDTO;
	}
	
	@Override
	public List<UserDTO> findUserByCode(String code) {
		List<Object[]> data = null;
		List<UserDTO> users = new ArrayList<UserDTO>();
		
		String sql = "SELECT U.user_id,pclu.user_uame, role "
		        + "FROM openmrs.psi_clinic as pcl left join openmrs.psi_clinic_user pclu "
		        + "on pcl.cid = pclu.psi_clinic_management_id left join openmrs.users as U "
		        + "on pclu.user_uame = U.username left join openmrs.user_role as UL on "
		        + "U.user_id = UL.user_id where  pcl.clinic_id= :code and role in('Doctor','Counselor','Paramedic(Static)','Lab Technician','Paramedic(Satellite)','CSP','Pharmacist')";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		data = query.setString("code", code).list();
		
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			Object[] objects = (Object[]) iterator.next();
			UserDTO userDTO = new UserDTO();
			userDTO.setId(Integer.parseInt(objects[0] + ""));
			userDTO.setUsername(objects[1] + "");
			userDTO.setUserRole(objects[1] + "-" + objects[2] + "");
			userDTO.setRole(objects[2] + "");
			users.add(userDTO);
		}
		return users;
		
	}
	
	@Override
	public List<UserDTO> findUsersNotInClinic(int clinicId) {
		List<Object[]> data = null;
		List<UserDTO> users = new ArrayList<UserDTO>();
		
		String sql = "select UN.user_id,UN.username,UN.diaplay from (SELECT U.user_id,U.username ,CONCAT( username,' - ',given_name, ' ', family_name ) AS diaplay "
		        + " FROM openmrs.users as U left join openmrs.person_name as PN on U.person_id = PN.person_id) as UN left join "
		        + " openmrs.psi_clinic_user as PU on UN.username = PU.user_uame  where psi_clinic_management_id is null) ";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		data = query.setInteger("clinicId", clinicId).list();
		
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			Object[] objects = (Object[]) iterator.next();
			UserDTO userDTO = new UserDTO();
			userDTO.setId(Integer.parseInt(objects[0] + ""));
			userDTO.setUsername(objects[1] + "");
			userDTO.setFullName(objects[2] + "");
			users.add(userDTO);
		}
		return users;
	}
	
}
