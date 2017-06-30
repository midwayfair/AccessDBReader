/*
Jon S. Patton
(cc) Creative Commons, 6/17/17

A console-based Java program that reads an Access file and also prints to text
with sensible output. Done as an extra excercise for my intro to comp sci class.

Please see the readme for notes about shortcomings and compiling/running
instructions.

*/

import java.sql.*;
import java.util.*;
import java.io.*;

public class DatabaseReader
{
  public static void main(String args[])
  {
    AccessDatabaseReader dbread = new AccessDatabaseReader();

    dbread.accessDatabaseReader();
  }
}

//This class is designed to read Microsoft Access files.
class AccessDatabaseReader
{
  public static void accessDatabaseReader()
  {
    try
    {
      //Get a filename from the user, else exit.
      String fileName = getFile();
      if (fileName.equals("0"))
      {
        throw new Exception("Goodbye"); //will exit the program
      }

      //Take the file name and connect with the database via the driver.
      //Excepts on its own and will exit if it fails.
      Connection con = DriverManager.getConnection("jdbc:ucanaccess://" + fileName);
      System.out.println("Connected\n");

      //Examine the database for the available tables -- we can only work with one table at a time.
      String table = getTable(con);
      if (table.equals("0"))
      {
        throw new Exception("Goodbye");
      }

      //Provides the metadata for the user.
      String[] metaData = getMetadata(table, con);
      printMetadata(metaData);

      //Do stuff until they quit.
      while(usage(metaData, con, table))
      {}

    }
    catch(Exception e)
    {
      // if the error message is "out of memory",
      // it probably means no database file is found
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  //Menu. Recurses until they chose 0.
  private static boolean usage(String[] metaData, Connection con, String table)
  {
    //Scanner console = new Scanner(System.in);
    System.out.println("Enter column number or one of these options:");
    System.out.println("0 quits");
    System.out.println("'metadata' to print the column headings again.");
    System.out.println("'tofile' to print to a plain text file.");
    System.out.print("'table' will let you select a new table. ->");
    String use = UserInput.get();
    System.out.println();

    if (use.equals("0"))
    {
      System.out.println("Goodbye.");
      return false;
    }
    else if (use.equalsIgnoreCase("metadata"))
    {
      printMetadata(metaData);
      return usage(metaData, con, table);
    }
    else if (use.equalsIgnoreCase("tofile"))
    {
      printToFile(metaData, con, table);
      return usage(metaData, con, table);
    }
    else if (use.equalsIgnoreCase("table"))
    {
      table = getTable(con);
      return usage(metaData, con, table);
    }
    else
    {
      queryDb(Integer.valueOf(use), con, table); //If it wasn't quit or metadata, convert it to an int and query.
      return usage(metaData, con, table);
    }
  }

  //This method obtains from the user the database filename on which queries will be run.
  private static String getFile()
  {
    String fileName;

    //Scanner console = new Scanner(System.in);
    System.out.print("Please enter the database filename (0 quits) ->");
    fileName = UserInput.get();
    File inputFile = new File(fileName);
    System.out.println();
    if (!inputFile.canRead())
    {
      System.out.println("Error: " + fileName + " is not readable. Please enter a new file name (0 quits) ->");
      return getFile(); //recurse if they entered something wrong.
    }
    return fileName;
  }

  private static String getTable(Connection con)
  {
    String table = ""; //initialized to nothing to stop the compiler from complaining

    //Scanner console = new Scanner(System.in);

    try
    {
      //Bless you, stackoverflow https://stackoverflow.com/questions/19062069/get-list-of-table-names-from-ms-access-2-0-using-java
      //Go through the db file and locate all table titles.
      System.out.println("Available tables are:");
      ResultSet rs = con.getMetaData().getTables(null, null, "%", null);
      String temp;
      while (rs.next())
      {
        temp = rs.getString(3);
        if(!temp.contains("MSys"))
        {
          System.out.println(temp);
        }
      }
      System.out.println();

      //Prompt the user for the table to examime.
      do
      {
        System.out.print("Enter table name (warning: they are case sensitive!) (0 quits): ");
        table = UserInput.get();
        System.out.println();
        if (table.equals("0"))
          return "0";
        else
          return table;
      } while(true);
    }
    catch (Exception e)
    {
      //The only reasonable exception is that the table doesn't exist, like they
      //typed it wrong. It makes more sense to recurse the function than to dump them out.
      System.err.print(e.getMessage());
      return getTable(con);
    }
  }

  //This method is for returning a SINGLE column for the user.
  private static void queryDb(int columnName, Connection con, String table)
  {
    try
    {
      Statement stmt = con.createStatement();
      stmt.execute("select * from " + table); // execute query in tables

      ResultSet rs = stmt.getResultSet(); // get any Result that came from our query

      if (rs != null)
      {
        while ( rs.next() )
        {
          System.out.println(rs.getString(columnName));
        }
      }
      stmt.close();
      //return "that was column #" + columnName;
    }
    catch (Exception e)
    {
      return;
    }
  }

  //This method extracts the column headings from the db
  private static String[] getMetadata (String table, Connection con)
  {
    //Adapted from https://stackoverflow.com/questions/22962667/reading-mdb-files-with-ucanaccess-returns-column-names-in-all-uppercase
    String[] metaData = new String[0]; //we have to initialize it to something so it can be returned on an error.

    try
    {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * FROM [" + table + "] WHERE False");
      ResultSetMetaData rsmd = rs.getMetaData();
      int rsmdLen = rsmd.getColumnCount();
      metaData = new String[rsmdLen];

      for (int i = 1; i < rsmdLen; i++) //SQL columns start numbering from 1.
      {
        if (rsmd.getColumnName(i) != null)
          metaData[i] = rsmd.getColumnName(i);
      }
    }
    catch (Exception e)
    {
      System.err.println(e);
    }
    return metaData;
  }

  //This method extracts the column headers from the db
  private static void printMetadata(String[] metaData)
  {
    System.out.println("Column names from metadata: ");
    for (int i = 1; i < metaData.length; i++)
    {
      System.out.println(i + ": " + metaData[i]);
    }
    System.out.println();
  }

  //This method extracts all row data from the file and prints it to paragraphs
  //in sections headed by the column headers. So each row gets its own section,
  //and inside each section is a paragraph preceded by the column heading.
  private static void printToFile(String[] metaData, Connection con, String table)
  {
    String[] allTogetherNow = new String[10000]; //The final data structure. I don't feel like coding a dynamic resizer for this right now.
    int arrayIndex = 1; //need an iterator for the final output array.
    String data; //for the actual data field reads at each point in the db (not strictly necessary)

    File ofOutput = null; //the output file object
    String fileName; //the output file name
    //Scanner console = new Scanner(System.in);
    System.out.print("Enter output file name (.txt will be added automatically) ->");
    fileName = UserInput.get();

    try
    {
      //Assign the output file
      ofOutput = new File(fileName + ".txt");
      ofOutput.createNewFile();

      //Hook up the firehose.
      PrintStream ofsOutFile = new PrintStream(ofOutput);

      //Go through every item in each column of the db
      for (int i = 1; i < metaData.length; i++)
      {
        arrayIndex = 1; //reset

        Statement stmt = con.createStatement();
        stmt.execute("select * from " + table); // selects everything from the chosen table
        ResultSet rs = stmt.getResultSet(); // Just what it says -- the result of the query

        if (rs != null) //No point cataloging an empty dataset.
        {
          while (rs.next())
          {
            data = rs.getString(i); //we don't do anything else with this, but just in case.

            //This is to avoid "null" appearing in the final PrintStream
            if (allTogetherNow[arrayIndex] == null)
            {
              allTogetherNow[arrayIndex] = "";
            }

            //Now that we've handled that, we reconstruct the string at the index
            //from all previous data, plus the column header (now a paragraph title)
            //plus the next data read
            allTogetherNow[arrayIndex] = allTogetherNow[arrayIndex] + metaData[i] + ": " + data + "\n\n";
            arrayIndex++;
          }
        }
      }
      //There are two ways to break this loop:
      //1. You could simply delete the i <= arrayIndex and it will terminate
      //on a null (this makes for a pseudo-while but more compact)
      //2. Or you can take advantage of the assumption that something didn't
      //go wrong while creating the array in the first place and arrayIndex SHOULD
      //be the highest number of rows encountered.
      for (int i = 1; i <= arrayIndex; i++)
      {
        if (allTogetherNow[i] != null)
          ofsOutFile.println(allTogetherNow[i] + "\n============================");
      }
      System.out.println("File " + fileName + " sucessfully created.\n");
    }

    catch (Exception e)
    {
      System.err.println("Error creating or writing to output file");
    }
  }
}

//Singleton class for user input to prevent too many instances when recursing input methods.
final class UserInput
{

  private static final Scanner CONSOLE = new Scanner(System.in);

  public static String get()
  {
    return CONSOLE.nextLine();
  }
}
