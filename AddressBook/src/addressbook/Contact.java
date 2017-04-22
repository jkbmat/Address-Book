/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package addressbook;

import java.util.Objects;

/**
 *
 * @author Tomáš
 */
public class Contact {

    private Long id;
    //private Person person; //probably unnecessary
    private String title;
    private String content;
    private ContactType contactType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public Person getPerson() {
//        return person;
//    }
//
//    public void setPerson(Person person) {
//        this.person = person;
//    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public void setContactType(ContactType contactType) {
        this.contactType = contactType;
        setDefaultTitle();
    }

    private void setDefaultTitle() {
        if (title == null) {
            if (contactType != ContactType.NONE) {
                this.title = this.contactType.toString();
            }
        }
    }

    public void setContactType(String stringContactType) {
        try {
            this.contactType = ContactType.valueOf(stringContactType);
            setDefaultTitle();
        } catch (IllegalArgumentException ex) {
            contactType = ContactType.NONE;
            throw ex;
        }
    }

    @Override
    public String toString() {
        return "Contact{" + id + "} = " + contactType + ": " + content + " (" + title + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Contact other = (Contact) obj;
        return Objects.equals(this.id, other.getId())
                && Objects.equals(this.content, other.getContent())
                && Objects.equals(this.title, other.getTitle())
                && Objects.equals(this.contactType, other.getContactType());
    }

    @Override //JAVA 7 - bit.ly/1d9boTN
    public int hashCode() {
        return Objects.hash(id, contactType, content, title);
    }
}
