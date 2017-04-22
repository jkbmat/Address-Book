/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import addressbook.Person;
import addressbook.PersonManager;
import addressbook.PersonManagerImpl;
import common.DBUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.dbcp.BasicDataSource;
import static org.junit.Assert.*;

/**
 *
 * @author Tomáš
 */
public class PersonManagerImplTest {

    private PersonManagerImpl manager;
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

    public PersonManagerImplTest() {
    }

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds, PersonManager.class.getResource("CreateTables.sql"));
        manager = new PersonManagerImpl();
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds, PersonManager.class.getResource("DropTables.sql"));
    }

    @Test
    public void addPerson() {
        Person person = newPerson("Firstname", "Lastname", "Nickname");
        manager.addPerson(person);

        Long personId = person.getId();
        assertNotNull(personId);

        Person result = manager.getPersonById(person.getId());
        assertEquals(person, result);
        assertNotSame(person, result);
        assertPersonDeepEquals(person, result);
        
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

    @Test
    //TODO editPersonWithWrongAttributes
    public void editPerson() {
        Person person = newPerson("Firstname", "Lastname", "Nickname");
        manager.addPerson(person);
        Long personId = person.getId();
        assertNotNull(personId);
        
        person.setFirstname("Firstname 11");
        person.setFirstname("Lastname 11");
        person.setFirstname("Nickname 11");
        manager.editPerson(person);

        Person result;
        result = manager.getPersonById(person.getId());
        assertPersonDeepEquals(person, result);

        person.setFirstname("Firstname");
        person.setFirstname("Lastname");
        person.setFirstname("Nickname");
        manager.editPerson(person);

        result = manager.getPersonById(person.getId());
        assertPersonDeepEquals(person, result);
    }

    private static Person newPerson(String firstname, String lastname, String nickname) {
        Person person = new Person();
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setNickname(nickname);

        return person;
    }

    private void assertPersonDeepEquals(Person expected, Person actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstname(), actual.getFirstname());
        assertEquals(expected.getLastname(), actual.getLastname());
        assertEquals(expected.getNickname(), actual.getNickname());
    }

    @Test
    public void getPersonById() {
        assertNull(manager.getPersonById(1L));

        Person person = newPerson("Firstname 1", "Lastname 1", "Nickname 1");
        manager.addPerson(person);
        Long personId = person.getId();

        Person result = manager.getPersonById(personId);
        assertEquals(person, result);
        assertPersonDeepEquals(person, result);
    }

    @Test
    //TODO removePersonWithWrongAttributes
    public void removePerson() {
        Person p1 = newPerson("Firstname 1", "Lastname 1", "Nickname 1");
        Person p2 = newPerson("Firstname 2", "Lastname 2", "Nickname 2");
        manager.addPerson(p1);
        manager.addPerson(p2);

        assertNotNull(manager.getPersonById(p1.getId()));
        assertNotNull(manager.getPersonById(p2.getId()));

        manager.removePerson(p1);

        assertNull(manager.getPersonById(p1.getId()));
        assertNotNull(manager.getPersonById(p2.getId()));
    }

    @Test
    public void getAllPersons() {
        assertTrue(manager.getAllPersons().isEmpty());

        Person p1 = newPerson("Firstname 1", "Lastname 1", "Nickname 1");
        Person p2 = newPerson("Firstname 2", "Lastname 2", "Nickname 2");
        Person p3 = newPerson("Firstname 3", "Lastname 3", "Nickname 3");

        manager.addPerson(p1);
        manager.addPerson(p2);
        manager.addPerson(p3);

        List<Person> expected = Arrays.asList(p1, p2, p3);
        List<Person> actual = manager.getAllPersons();

        assertPersonCollectionDeepEquals(expected, actual);
    }

    private void assertPersonCollectionDeepEquals(List<Person> expected, List<Person> actual) {
        assertEquals(expected.size(), actual.size());

        List<Person> expectedSortedList = new ArrayList<>(expected);
        List<Person> actualSortedList = new ArrayList<>(actual);
        Collections.sort(expectedSortedList, personKeyComparator);
        Collections.sort(actualSortedList, personKeyComparator);
        for (int i = 0; i < expectedSortedList.size(); i++) {
            assertPersonDeepEquals(expectedSortedList.get(i), actualSortedList.get(i));
        }
    }
    private static Comparator<Person> personKeyComparator = new Comparator<Person>() {
        @Override
        public int compare(Person p1, Person p2) {
            Long p1Id = p1.getId();
            Long p2Id = p2.getId();

            if (p1Id == null && p2Id == null) {
                return 0;
            } else if (p1Id == null && p2Id != null) {
                return -1;
            } else if (p1Id != null && p2Id == null) {
                return 1;
            } else {
                return p1Id.compareTo(p2Id);
            }
        }
    };
}
