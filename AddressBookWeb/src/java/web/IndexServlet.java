package web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import addressbook.*;
import common.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 *
 * @author Admin
 */
@WebServlet(name = "IndexServlet", urlPatterns = {"/IndexServlet/*"})
public class IndexServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String message = new String();
        
        try {
            AddressBookManagerImpl mng;
            mng = new AddressBookManagerImpl();
            DataSource ds = prepareDataSource();
            mng.setDataSource(prepareDataSource());
            
            if(request.getMethod().equals("POST"))
            {
                // <editor-fold defaultstate="collapsed" desc="Add Person">
                if("add".equals(request.getParameter("submit")))
                {
                    if(request.getParameter("firstname").equals(""))
                    {
                        message = "Error creating new person: First name is required.";
                    }
                    else
                    {
                    
                        Person p = new Person();
                        p.setFirstname(request.getParameter("firstname"));
                        p.setNickname(request.getParameter("nickname"));
                        p.setLastname(request.getParameter("lastname"));

                        try
                        {
                            mng.addPerson(p);
                            message = "Person "+p.getFirstname()+" "+p.getNickname()+" "+p.getLastname()+" succesfully added.";
                        }
                        catch(ServiceFailureException ex)
                        {
                            message = "Error creating new person.";
                        }
                    }
                }
                //</editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Add Contact">
                if("addc".equals(request.getParameter("submit")))
                {
                    if("".equals(request.getParameter("ctype")) || "".equals(request.getParameter("cval")))
                    {
                        message = "Error adding a new contact: Both fields are required";
                    }
                    else
                    {
                        try
                        {
                            Contact c = new Contact();
                            c.setContactType(ContactType.NONE);
                            c.setTitle(request.getParameter("ctype"));
                            c.setContent(request.getParameter("cval"));

                            mng.addContact(mng.getPersonById(Long.parseLong(request.getParameter("pid"))), c);
                            message = "New contact added successfully.";
                        }
                        catch(ServiceFailureException ex)
                        {
                            message = "Error adding a new contact.";
                        }
                        catch(ValidationException ex)
                        {
                            message = "Error adding a new contact: "+ex.getMessage();
                        }
                    }
                }
                //</editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Edit Person">
                if("editp".equals(request.getParameter("submit")))
                {
                    if("".equals(request.getParameter("firstname")))
                    {
                        message = "Error editing a person: First name is required";
                    }
                    else
                    {
                        Person p = new Person();
                        p.setId(Long.parseLong(request.getParameter("pid")));
                        p.setFirstname(request.getParameter("firstname"));
                        p.setNickname(request.getParameter("nickname"));
                        p.setLastname(request.getParameter("lastname"));

                        try
                        {
                            mng.editPerson(p);
                            message = "Person edited succesfully";
                        }
                        catch(Exception ex)
                        {
                            message = "Error editing a person.";
                        }
                    }
                }
                //</editor-fold>
                
                // <editor-fold defaultstate="collapsed" desc="Edit Contact">
                if("editc".equals(request.getParameter("submit")))
                {
                    if("".equals(request.getParameter("ckey")) || "".equals(request.getParameter("cval")))
                    {
                        message = "Error editing a contact: Both fields are required";
                    }
                    else
                    {
                        Contact c = new Contact();
                        c.setId(Long.parseLong(request.getParameter("cid")));
                        c.setContactType(ContactType.NONE);
                        c.setTitle(request.getParameter("ckey"));
                        c.setContent(request.getParameter("cval"));

                        try
                        {
                            mng.editContact(c);
                            message = "Contact edited successfully.";
                        }
                        catch(Exception ex)
                        {
                            message = "Error editing a contact.";
                        }
                    }
                }
                //</editor-fold>
                
                // <editor-fold defaultstate="collapsed" desc="Delete Person">
                if("delete".equals(request.getParameter("submit")))
                {
                    Person p = mng.getPersonById(Long.parseLong(request.getParameter("pid")));
                    
                    try
                    {
                        mng.removeAllContactsOfPerson(p);
                        mng.removePerson(p);
                        message = "Person "+p.getFirstname() + " " + p.getNickname() + " " + p.getLastname()+" successfully deleted";
                    }
                    catch(ServiceFailureException ex)
                    {
                        message = "Error: Couldn't remove person"; 
                    }
                    catch(IllegalArgumentException ex)
                    {
                        message = "Error: This person does not exist";
                    }
                }
                //</editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Delete Contact">
                if("delc".equals(request.getParameter("submit")))
                {
                    if(request.getParameter("cid") == null)
                    {
                        message = "Error removing a contact";
                    }
                    else
                    {
                        try
                        {
                            mng.removeContact(mng.getContactById(Long.parseLong(request.getParameter("cid"))));
                            message = "Succesfully removed a contact.";
                        }
                        catch(Exception e)
                        {
                            message = "Error removing a contact";
                        }
                    }
                }
                //</editor-fold>               
            }
            
            
            // Set up data for displaying the page
            
            Map<String, Object> data = new TreeMap<String, Object>();
            
            for(Person p: mng.getAllPersons())
            {
                // Person                
                Map<String, Object> pdata = new HashMap<String, Object>();
                data.put(p.getId().toString(), pdata);
                
                pdata.put("firstname", p.getFirstname());
                pdata.put("nickname", p.getNickname());
                pdata.put("lastname", p.getLastname());
                
                // Person's contacts
                Map<Long, ArrayList<String>> cdata = new HashMap<Long, ArrayList<String>>();
                pdata.put("contacts", cdata);
                
                for(Contact c: mng.getContactsOfPerson(p))
                {
                    ArrayList<String> d = new ArrayList<String>();
                    d.add(c.getTitle());
                    d.add(c.getContent());
                    cdata.put(c.getId(), d);
                }
            }
            request.setAttribute("contacts", data);
            
        } catch (SQLException ex) {
            Logger.getLogger(IndexServlet.class.getName()).log(Level.SEVERE, "Error.", ex);
            message = "Something went wrong.";
        }
        
        request.setAttribute("message", message);
        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher("/list.jsp").forward(request, response);
    }
    
    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
        ds.setUsername("Test");
        ds.setPassword("password");
        ds.setUrl("jdbc:derby://localhost:1527/AddressbookDB");
        return ds;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
