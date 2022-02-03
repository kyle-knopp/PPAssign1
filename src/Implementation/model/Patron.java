package model;

import exception.InvalidPrimaryKeyException;
import exception.PasswordMismatchException;
import impresario.IView;
import org.w3c.dom.ls.LSOutput;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;


public class Patron extends EntityBase {
    private static final String myTableName = "Patron";

    protected Properties dependencies;

    private String updateStatusMessage = "";

    //Constructor for if we know the id number which is a private key so there should only be one unique id of that number
    public Patron(String id) throws InvalidPrimaryKeyException
    {
        super(myTableName);
        String query = "SELECT * FROM " + myTableName + " WHERE (AccountNumber = " + id + ")";

        Vector allDataRetrieved = getSelectQueryResult(query);

        if (allDataRetrieved != null){
            int size = allDataRetrieved.size();

            // There should be EXACTLY one account. More than that is an error
            if (size != 1)
            {
                throw new InvalidPrimaryKeyException("Multiple accounts matching id : "
                        + id + " found.");
            }
            else
            {
                // copy all the retrieved data into persistent state
                Properties retrievedAccountData = (Properties)allDataRetrieved.elementAt(0);
                persistentState = new Properties();

                Enumeration allKeys = retrievedAccountData.propertyNames();
                while (allKeys.hasMoreElements() == true)
                {
                    String nextKey = (String)allKeys.nextElement();
                    String nextValue = retrievedAccountData.getProperty(nextKey);

                    if (nextValue != null)
                    {
                        persistentState.setProperty(nextKey, nextValue);
                    }
                }

            }
        }
        // If no account found for this user name, throw an exception
        else
        {
            throw new InvalidPrimaryKeyException("No account matching id : "
                    + id + " found.");
        }
    }

    //Constructor for it we do not know the specific id number to look up
    public Patron(Properties props) throws InvalidPrimaryKeyException, PasswordMismatchException
    {
        super(myTableName);

        persistentState = new Properties();
        Enumeration allKeys = props.propertyNames();
        while(allKeys.hasMoreElements()==true)
        {
            String nextKey = (String)allKeys.nextElement();
            String nextValue = props.getProperty(nextKey);

            if(nextValue!=null)
            {
                persistentState.setProperty(nextKey,nextValue);
            }
        }
    }

    public void save(){
        updateStateInDatabase();
    }

    public void updateStateInDatabase(){
        try
        {
            if(persistentState.getProperty("patronId") != null)
            {
                Properties whereClause = new Properties();
                whereClause.setProperty("patronId",
                        persistentState.getProperty("patronID"));
                updatePersistentState(mySchema, persistentState, whereClause);
                updateStatusMessage = "Account data for Patron Id : " + persistentState.getProperty("patronId") + " updated successfully in database!";
            }
            else
            {
                Integer patronId =
                        insertAutoIncrementalPersistentState(mySchema, persistentState);
                persistentState.setProperty("AccountNumber", "" + patronId.intValue());
                updateStatusMessage = "Account data for new account : " +  persistentState.getProperty("AccountNumber")
                        + "installed successfully in database!";
            }
        }
        catch (SQLException ex)
        {
            updateStatusMessage = "Error in installing account data in database!";
        }
        //DEBUG System.out.println("updateStateInDatabase " + updateStatusMessage);
    }

    @Override
    public String toString() {
        return "Patron Name: " + persistentState.getProperty("name") + "; Address: " +
                persistentState.getProperty("address")  + "; City: " +
                persistentState.getProperty("city") + "; Zip: "+
                persistentState.getProperty("zip");
    }

    public void display(){
        System.out.println(toString());
    }

    //-----------------------------------------------------------------------------------
    protected void initializeSchema(String tableName)
    {
        if (mySchema == null)
        {
            mySchema = getSchemaInfo(tableName);
        }
    }
}
