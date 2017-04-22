/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package addressbook;

import java.util.List;

/**
 *
 * @author Tomáš
 */
public interface PersonManager {
    void addPerson(Person p);
    void editPerson(Person p);
    void removePerson(Person p);
    
    List<Person> getAllPersons();
    Person getPersonById(Long id);
        
    List<Person> findPersonsByName(String name); //unused
}
