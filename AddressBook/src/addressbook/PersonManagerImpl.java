/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package addressbook;

import common.*;
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
public class PersonManagerImpl implements PersonManager{
    
    private static final Logger logger = Logger.getLogger(PersonManagerImpl.class.getName());    
    
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void addPerson(Person p) throws ServiceFailureException {
        checkDataSource();
        validate(p);
        if (p.getId() != null) {
            throw new IllegalEntityException("Person id is already set.");
        }       
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Person (firstname, lastname, nickname) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, p.getFirstname());
            st.setString(2, p.getLastname());
            st.setString(3, p.getNickname());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, p, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            p.setId(id);//set generated id for person
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting person into database.";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void editPerson(Person p) throws ServiceFailureException {
        checkDataSource();
        validate(p);
        
        if (p.getId() == null) {
            throw new IllegalEntityException("Person id is null.");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);            
            st = conn.prepareStatement(
                    "UPDATE Person SET firstname = ?, lastname = ?, nickname = ? WHERE id = ?");
            st.setString(1, p.getFirstname());
            st.setString(2, p.getLastname());
            st.setString(3, p.getNickname());
            st.setLong(4, p.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, p, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when editing person in the database";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }        
    }


    @Override
    public void removePerson(Person p) throws ServiceFailureException {
        checkDataSource();
        if (p == null) {
            throw new IllegalArgumentException("Person is null");
        }        
        if (p.getId() == null) {
            throw new IllegalEntityException("Person id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Person WHERE id = ?");
            st.setLong(1, p.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, p, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting person from the database.";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Person> getAllPersons() throws ServiceFailureException {    
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Person");
            return executeQueryForMultiplePersons(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all persons from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }          
    }
   


    @Override
    public Person getPersonById(Long id)  throws ServiceFailureException {
        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT * FROM Person WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSinglePerson(st);
        } catch (SQLException ex) {
            String msg = "Error when getting person with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Person> findPersonsByName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void validate(Person p) {
        if (p == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (p.getFirstname() == null) {
            throw new ValidationException("Firstname is null");
        }
        if (p.getLastname() == null) {
            throw new ValidationException("Lastname is null");
        }
    }

    private static Person executeQueryForSinglePerson(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Person result = rowToPerson(rs);                
            if (rs.next()) {
                throw new ServiceFailureException("Internal integrity error: more persons with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    private static List<Person> executeQueryForMultiplePersons(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Person> result = new ArrayList<Person>();
        while (rs.next()) {
            result.add(rowToPerson(rs));
        }
        
        return result;
    }
    
    private static Person rowToPerson(ResultSet rs) throws SQLException {
        Person result = new Person();
        result.setId(rs.getLong("id"));
        result.setFirstname(rs.getString("firstname"));
        result.setLastname(rs.getString("lastname"));
        result.setNickname(rs.getString("nickname"));
        
        return result;
    }
}