/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package addressbook;

import common.ServiceFailureException;
import java.util.List;

/**
 *
 * @author Tomáš
 */
public interface AddressBookManager {
    void addPerson(Person p);
    void editPerson(Person p);
    Person getPersonById(Long id) throws ServiceFailureException;
    void removePerson(Person p);
    
    void addContact(Person p, Contact c);
    void editContact(Contact c);
    void removeContact(Contact c);
    Contact getContactById(Long id);
    
    List<Contact> getContactsOfPerson(Person p);
    List<Person> getAllPersons();
    void removeAllContactsOfPerson(Person p);
}