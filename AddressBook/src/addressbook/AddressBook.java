/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package addressbook;

import common.DBUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Tomáš
 */
public class AddressBook {

    /**
     * @param args the command line arguments
     */
    static AddressBookManagerImpl mng;

    public static void main(String[] args) {
        try {
            mng = new AddressBookManagerImpl();
            DataSource ds = prepareDataSource();
            mng.setDataSource(prepareDataSource());
            //DBUtils.executeSqlScript(ds, PersonManager.class.getResource("DropTables.sql"));
            //DBUtils.executeSqlScript(ds, PersonManager.class.getResource("CreateTables.sql"));
            DBUtils.executeSqlScript(ds, AddressBookManagerImpl.class.getResource("TestData.sql"));
//            Person p = new Person();
//                p.setFirstname("Nick");
//                p.setLastname("Slaughter");
//                p.setNickname("TropicalHeat");
//            mng.addPerson(p);
//
//            Contact c = new Contact();
//                c.setContactType(ContactType.EMAIL);
//                c.setContent("darkside@youtube.com");
//            mng.addContact(p, c);
            
            displayPersonsWithContacts(mng.getAllPersons());    
        } catch (SQLException ex) {
            Logger.getLogger(AddressBook.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void displayPersons(List<Person> persons) {
        for (Person person : persons) {
            System.out.println(person);
        }
    }

    private static void displayContacts(List<Contact> contacts) {
        for (Contact contact : contacts) {
            System.out.print("  ");
            System.out.print(contact);
            System.out.println();
        }
    }

    private static void displayPersonsWithContacts(List<Person> persons) {
        for (Person person : persons) {

            System.out.println(person);
            displayContacts(mng.getContactsOfPerson(person));
            System.out.println();
        }
    }

    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        //ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
        //ds.setUsername("SA");
        ds.setUsername("Test");
        ds.setPassword("password");
        //ds.setUrl("jdbc:hsqldb:MyDB");
        ds.setUrl("jdbc:derby://localhost:1527/AddressbookDB");
        
        //ds.setUrl("jdbc:derby:memory:PersonManager-test;create=true");
        return ds;
    }
}
