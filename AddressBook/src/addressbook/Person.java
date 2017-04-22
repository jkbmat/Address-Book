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
public class Person {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    private String firstname;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    private String lastname;
    private String nickname; 

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    @Override
    public String toString() {
        return "Person{"+ id +"} = " + firstname + " " + lastname + " (" + nickname + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        return Objects.equals(this.id, other.getId())
                && Objects.equals(this.firstname, other.getFirstname())
                && Objects.equals(this.lastname, other.getLastname())
                && Objects.equals(this.nickname, other.getNickname());
    }

    @Override //JAVA 7 - bit.ly/1d9boTN
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, nickname);
    }
}
