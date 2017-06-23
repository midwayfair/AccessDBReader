Jon S. Patton
6/17/17

Access DataBase Reader:

This is a fairly simple program that I wrote to use the ucanaccess Java library
to extract information from an access database file. The program is not fast but
it does what it's supposed to. More notes for improvement are below.

It requires ucanaccess: http://ucanaccess.sourceforge.net/site.html

There are 5 files to add to your file path; be sure to follow the library's
instructions.

You provide the program
with the file location, and it will let you view a table of your choice;
from that point you can view all information in any column in the table, or
print a text file that outputs the entire table's contents to the following
format:

COLUMN 1 HEADER: Lorem ipsum dolor sit amet ..
========
COLUMN 2 HEADER: Lorem ipsum dolor sit amet ..
========
...
COLUMN n HEADER: Lorem ipsum dolor sit amet ..
========

This was given as a side project for my intro to computer science class because
we were hoping to find a project that could use arrays and file/input output.

===================================
Here are my original notes:

What this is:

You input a database filename on startup and it extracts the available tables
from the file, then prints the metadata that shows the column names.

From there, you can simply type in the number of a column and it will display
the contents of that column, OR you can print it to a plain text file that reworks the columns and rows as blocks of data.

---

What was broken:

The reference in the first line to anything related to Sun made me suspicious --
the driver called in the first line of the program is deprecated as of Java8 (maybe doesn't even exist anymore), and worse the driver seems to be for Windows.

I did some research into open source drivers and found these two:

 * http://ucanaccess.sourceforge.net/site.html <--This one works -- 25mb
 download if you follow the links.
 * http://jtds.sourceforge.net/doc/net/sourceforge/jtds/jdbc/package-summary.html
 <--This one probably works but I didn't get it to fire up yet. It has some
 interesting extra functionality.

They're both platform-independent, but they've got some dependencies (ucanaccess
is a mishmash of some other libraries).

---

Compiling instructions:
-You'll need to add 5 jars to the class path for the db reader driver. Explained
here (https://www.seas.upenn.edu/~cis1xx/resources/java/jar/jarindrjava.html)
-I thought there might be a way to program those in with a package or import,
but I can't find it. (Even C does that, so Java must have some way of doing it
without the IDE, right? Maybe with a manifest file?)
-The .java file will compile with three .class files (main, the class for the
access reader, and the singleton console input class).

Notes about problems or areas for improvement:
1. It's a slow program, even for something running in Dr. Java, and it's not
that much faster from the command line. The problems occur when establishing the
connection with the db and accessing the table, so I think the library or driver involved must be loading the entire db into memory and doing something to it before the queries. Once the connection is created it runs about as fast as I'd expect from a text-based program.

2. There are protection statements in that library to prevent too large a file
from being loaded into memory. I didn't implement them because the test file is
too small to do anything with them anyway.

3. I also didn't implement a dynamic array resizer. Honestly, I'm a little tired
at this point and it might take me another hour to get something satisfactory with it.

4. Please don't hate me for some of the style choices. Having the variables at
the top of the methods instead of right next to their uses made it much harder for me to keep track of the moving parts.

5. I could actually make a small change that would, if there is only one table
in the file, not bug the user for the table name to work with.

6. The most surprising thing to me is that the best solution for the file
printing didn't involve a 2D array despite handling row and column data.
Because the number of rows in each column can't necessarily be predicted for
each loop, you have to have a completely independent iterator inside a single
for loop that iterates over the columns. This was the biggest pain to figure out
(which ironically was solved on pencil and paper).

7. I'm not 100% certain that there's nothing unix-specific in my code and I
didn't have time to try this on a Windows machine. This line may need something
changed (maybe one of the colons? I'm not really sure.):
Connection con = DriverManager.getConnection("jdbc:ucanaccess://" + fileName);

I've attached a text file with the output to demonstrate that it works.

8. Finally, I've cited my sources for some pieces of code that I couldn't figure
out from the library readmes (I don't know sql commands).
