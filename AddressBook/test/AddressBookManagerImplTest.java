/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import addressbook.AddressBookManagerImpl;
import addressbook.Contact;
import addressbook.ContactType;
import addressbook.Person;
import addressbook.PersonManager;
import common.DBUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tomáš
 */
public class AddressBookManagerImplTest {

    private AddressBookManagerImpl manager;
    private DataSource ds;

    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        //ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
        ds.setUsername("Test");
        ds.setPassword("password");
        ds.setUrl("jdbc:derby://localhost:1527/UnitTestDB");
        //ds.setUrl("jdbc:hsqldb:MyDB");
        //ds.setUrl("jdbc:derby:memory:PersonManager-test;create=true");
        return ds;
    }

    public AddressBookManagerImplTest() {
    }

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, PersonManager.class.getResource("CreateTables.sql"));
        manager = new AddressBookManagerImpl();
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds, PersonManager.class.getResource("DropTables.sql"));
    }

    @Test
    public void addContact() {
        Person person = newPerson("Firstname", "Lastname", "Nickname");
        manager.addPerson(person);
        Long personId = person.getId();
        assertNotNull(personId);

        Contact contact = newContact("Title", "Content", ContactType.PHONE);
        manager.addContact(person, contact);
        Long contactId = contact.getId();
        assertNotNull(contactId);

        Contact result = manager.getContactById(contact.getId());
        assertEquals(contact, result);
        assertNotSame(contact, result);
        assertContactDeepEquals(contact, result);
        
        try
        {
            manager.addPerson(null);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            // OK
        }
    }

    @Test //TODO editContactWithWrongAttributes
    public void editContact() {
        Person person = newPerson("Firstname", "Lastname", "Nickname");
        manager.addPerson(person);
        Long personId = person.getId();
        assertNotNull(personId);

        Contact contact = newContact("Title", "Content", ContactType.PHONE);
        manager.addContact(person, contact);
        Long contactId = contact.getId();
        assertNotNull(contactId);

        contact.setTitle("Title 11");
        contact.setContent("Content 11");
        contact.setContactType(ContactType.ICQ);
        manager.editContact(contact);

        Contact result = manager.getContactById(contact.getId());
        assertEquals(contact, result);

        contact.setTitle("Title");
        contact.setContent("Content");
        contact.setContactType(ContactType.PHONE);
        manager.editContact(contact);

        result = manager.getContactById(contact.getId());
        //assertContactDeepEquals(contact, result);
        assertEquals(contact, result);
    }

    @Test    //TODO removeContactWithWrongAttributes
    public void removeContact() {
        Person person = newPerson("Firstname", "Lastname", "Nickname");
        manager.addPerson(person);
        Long personId = person.getId();
        assertNotNull(personId);

        Contact c1 = newContact("Title", "Content", ContactType.PHONE);
        Contact c2 = newContact("Title", "Content", ContactType.PHONE);
        manager.addContact(person, c1);
        manager.addContact(person, c2);

        assertNotNull(manager.getContactById(c1.getId()));
        assertNotNull(manager.getContactById(c2.getId()));

        manager.removeContact(c1);
        assertNull(manager.getContactById(c1.getId()));
        assertNotNull(manager.getContactById(c2.getId()));
    }

    @Test
    public void getContactsOfPerson() {
        Person person = newPerson("Firstname", "Lastname", "Nickname");
        manager.addPerson(person);
        Long personId = person.getId();
        assertNotNull(personId);

        List<Contact> contacts = new ArrayList<>();
        
        Contact contact = newContact("Title", "Content", ContactType.PHONE);
        manager.addContact(person, contact);
        Long contactId = contact.getId();
        assertNotNull(contactId);
        contacts.add(contact);
        
        Contact contact2 = newContact("Title2", "Content2", ContactType.EMAIL);
        manager.addContact(person, contact2);
        Long contactId2 = contact2.getId();
        assertNotNull(contactId2);
        contacts.add(contact2);

        
        List<Contact> result = manager.getContactsOfPerson(person);
        assertContactCollectionDeepEquals(contacts, result);//overkill?
    }

    @Test
    public void removeAllContactsOfPerson() {
        Person person = newPerson("Firstname", "Lastname", "Nickname");
        manager.addPerson(person);
        Long personId = person.getId();
        assertNotNull(personId);
       
        Contact contact = newContact("Title", "Content", ContactType.PHONE);
        manager.addContact(person, contact);
        Long contactId = contact.getId();
        assertNotNull(contactId);
        
        Contact contact2 = newContact("Title2", "Content2", ContactType.EMAIL);
        manager.addContact(person, contact2);
        Long contactId2 = contact2.getId();
        assertNotNull(contactId2);
        
        manager.removeAllContactsOfPerson(person);
        List<Contact> result = manager.getContactsOfPerson(person);
        assertEquals(0, result.size());
    }

    private Contact newContact(String title, String content, ContactType contactType) {
        Contact c = new Contact();
        c.setContactType(contactType);
        c.setTitle(title);
        c.setContent(content);

        return c;
    }

    private static Person newPerson(String firstname, String lastname, String nickname) {
        Person person = new Person();
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setNickname(nickname);

        return person;
    }

    //after changes in equals method it is not necessary  
    private void assertContactDeepEquals(Contact expected, Contact actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getContent(), actual.getContent());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getContactType(), actual.getContactType());
    }
    
    
    
        private void assertContactCollectionDeepEquals(List<Contact> expected, List<Contact> actual) {
        assertEquals(expected.size(), actual.size());

        List<Contact> expectedSortedList = new ArrayList<>(expected);
        List<Contact> actualSortedList = new ArrayList<>(actual);
        Collections.sort(expectedSortedList, contactKeyComparator);
        Collections.sort(actualSortedList, contactKeyComparator);
        for (int i = 0; i < expectedSortedList.size(); i++) {
            assertContactDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
    private static Comparator<Contact> contactKeyComparator = new Comparator<Contact>() {
        @Override
        public int compare(Contact p1, Contact p2) {
            Long c1Id = p1.getId();
            Long c2Id = p2.getId();

            if (c1Id == null && c2Id == null) {
                return 0;
            } else if (c1Id == null && c2Id != null) {
                return -1;
            } else if (c1Id != null && c2Id == null) {
                return 1;
            } else {
                return c1Id.compareTo(c2Id);
            }
        }
    };
}
