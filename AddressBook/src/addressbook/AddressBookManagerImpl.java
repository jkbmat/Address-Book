/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package addressbook;

import common.DBUtils;
import common.IllegalEntityException;
import common.ServiceFailureException;
import common.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Tomáš
 */
public class AddressBookManagerImpl implements AddressBookManager {

    private PersonManagerImpl personManager;
    private DataSource dataSource;
    private static final Logger logger = Logger.getLogger(PersonManagerImpl.class.getName());

    public AddressBookManagerImpl() {
        personManager = new PersonManagerImpl();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        personManager.setDataSource(dataSource);
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void addPerson(Person p) throws ServiceFailureException {
        personManager.addPerson(p);
    }

    @Override
    public void editPerson(Person p) throws ServiceFailureException{
        personManager.editPerson(p);
    }

    @Override
    public void removePerson(Person p) throws ServiceFailureException{
        personManager.removePerson(p);
        removeAllContactsOfPerson(p); //or use ON DELETE CASCADE?   
    }

    @Override
    public void addContact(Person p, Contact c) {
        checkDataSource();
        validate(p, c);
        if (c.getId() != null) {
            throw new IllegalEntityException("Contact id is already set.");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            st = conn.prepareStatement("INSERT INTO Contact (PersonId, Title, Content, Type) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            st.setLong(1, p.getId());
            st.setString(2, c.getTitle());
            st.setString(3, c.getContent());
            st.setString(4, c.getContactType().toString());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, p, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            c.setId(id);//set generated id for person

            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting contact into database.";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn); //rollback can be here or in catch block - http://bit.ly/1phTkXS
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void editContact(Contact c) {
        checkDataSource();
        validate(c);
        if (c.getId() == null) {
            throw new IllegalEntityException("Contact id is null.");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            st = conn.prepareStatement("UPDATE Contact SET Title=?, Content=?, Type=? WHERE Id = ?");
            st.setString(1, c.getTitle());
            st.setString(2, c.getContent());
            st.setString(3, c.getContactType().toString());
            st.setLong(4, c.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, c, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating contact in the database";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void removeContact(Contact c) throws IllegalEntityException, ServiceFailureException{
        checkDataSource();
        validate(c);
        if (c.getId() == null) {
            throw new IllegalEntityException("Contact id is null.");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            st = conn.prepareStatement("DELETE FROM Contact WHERE Id = ?");
            st.setLong(1, c.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, c, false);
            
            conn.commit();

        } catch (SQLException ex) {
            String msg = "Error when deleting contact in the database";
            logger.log(Level.SEVERE, msg, ex);
            DBUtils.doRollbackQuietly(conn);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Contact> getContactsOfPerson(Person p) {
        checkDataSource();
        if (p == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (p.getId() == null) {
            throw new IllegalArgumentException("Person id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT Id, Title, Content, Type FROM Contact WHERE PersonId = ?");
            st.setLong(1, p.getId());

            return executeQueryForMultipleContacts(st.executeQuery());
        } catch (SQLException ex) {
            String msg = "Error when trying to find contacts of person";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void removeAllContactsOfPerson(Person p) {
        checkDataSource();
        if (p == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (p.getId() == null) {
            throw new IllegalArgumentException("Person id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            st = conn.prepareStatement("DELETE FROM Contact WHERE PersonId = ?");
            st.setLong(1, p.getId());
            
            int count = st.executeUpdate();
            //DBUtils.checkUpdatesCount(count, p, false);

            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when trying to delete contacts of person";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    private static Contact rowToContact(ResultSet rs) throws SQLException {
        Contact result = new Contact();

        result.setId(rs.getLong("Id"));
        result.setTitle(rs.getString("Title"));
        result.setContent(rs.getString("Content"));
        try {
            result.setContactType(rs.getString("Type"));
        } catch (IllegalArgumentException ex) {
            String msg = "Error when trying to set ContactType from string";
            logger.log(Level.SEVERE, msg, ex);
            throw new IllegalArgumentException(msg, ex);
        }
        return result;
    }

    private void validate(Person p, Contact c) {
        if (p == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (p.getId() == null) {
            throw new ValidationException("Person id is null");
        }
        validate(c);
    }

    private void validate(Contact c) {
        if (c == null) {
            throw new IllegalArgumentException("Contact is null");
        }
        if (c.getContent() == null) {
            throw new ValidationException("Content is null");
        }
        if (c.getContactType() == null) {
            throw new ValidationException("ContactType is null");
        }
        if (c.getTitle() == null) {
            throw new ValidationException("Title is null");
        }
    }

    private List<Contact> executeQueryForMultipleContacts(ResultSet rs) throws SQLException {
        List<Contact> result = new ArrayList<Contact>();
        while (rs.next()) {
            result.add(rowToContact(rs));
        }
        return result;
    }

    private static Contact executeQueryForSingleContact(ResultSet rs) throws SQLException, ServiceFailureException {
        if (rs.next()) {
            Contact result = rowToContact(rs);
            if (rs.next()) {
                throw new ServiceFailureException("Internal integrity error: more contacts with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    @Override
    public Person getPersonById(Long id)  throws ServiceFailureException {
        return personManager.getPersonById(id);
    }

    @Override
    public List<Person> getAllPersons() {
        return personManager.getAllPersons();
    }

    @Override
    public Contact getContactById(Long id) {
        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Contact WHERE id = ?");
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            return executeQueryForSingleContact(rs);
        } catch (SQLException ex) {
            String msg = "Error when getting contact with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }
}
