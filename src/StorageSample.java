import aeonics.data.*;
import aeonics.sql.*;
import aeonics.storage.*;
import aeonics.util.*;

/**
<h1>Storage</h1>
<p>
Fundamentally, in 80% of cases you will need to just store, update and fetch data.
Whether this data is located in a database, in a file, on a remote system or else 
does not change the type of operation that you need to perform: store / fetch.
</p>
<p>
The most common mistake is to hardwire the business logic with the technicality of managing data.
What if the storage location changes, switching from a file to a database for example ?
How much of your code will be impacted ? What if your dev environment does not have the final
storage location available, how do you test the system ?
</p>
<p>
This is why we've added an intermediate level that will focus on the logical operation to store / fetch data
and delegate the configuration of the actual storage location to a later point in time.
Using the Registry, we know, by design, that "some" storage will be available when we need it. Whichever it is
does not matter for the process we are currently designing.
</p>

<h1>Data Source</h1>
<p>
In a similar way, the data source is an abstraction on top of a database. If you need to perform a specific query
that meets your business logic, you can do it easily without having to manage the connection to the database,
the proper driver, the reconnection in case of errors, the username / password or the IP address,...  
</p>
<p>
The DataSource items that are registered in the registry will handle this for you.
This simplifies the development process and helps to stay focused on the high level task to perform
instead on the technicality.
This way, a change in the database connection settings will not have any impacts on your code.
</p>
<p>
The DataSource is very straightforward to use with a single <code>query()</code> method that will
return data as a Data item, how convenient...
</p>
 */
public class StorageSample
{
	public static void main(String[] args)
	{
		System.out.println("Start");
		
		try {
			storeData();
			querySql();
		} catch(Exception e) { e.printStackTrace(); }
		
		System.out.println("End");
	}
	
	/**
	 * We can use a named storage from the Registry
	 * and it does not matter where or how the data is actually stored.
	 */
	public static void storeData()
	{
		// access the storage from the registry
		Storage store = Registry.of(Storage.class).get("customers");
		
		// fetch data (as json)
		Data data = Json.decode(store.get("John Doe"));
		
		if( data.asBool("is_friendly") )
			data.put("rating", 5);
		else
			store.remove("John Doe");
		
		// overwrite the data in the storage
		store.put("John Doe", data.toString());
	}
	
	/**
	 * If needed, we can directly perform queries on a database pulled from the Registry.
	 * We dont need to manage the connection, driver,... we just use it.
	 * 
	 * Be careful though, SQL exceptions can still happen, there is no magic here!
	 */
	public static void querySql() throws Exception
	{
		// access the sql data source from the Registry
		DataSource db = Registry.of(DataSource.class).get("customers");
		
		// Always use parameterized queries using "?" to avoid SQL injections.
		Data data = db.query("SELECT * FROM customers WHERE name = ?", "John Doe");
		// Each call to the "query" method can possibly use a different physical connection to the database.
		// The current connection is then directly available for others to reuse it in between.
		// If all connections are already in use, your query will be enqueued until one is made available.
		
		if( data.asBool("is_friendly") )
			data.put("rating", 5);
		else
			db.query("DELETE FROM customers WHERE name = ?", "John Doe");
		
		// The "query" method fits for all types of queries
		db.query("UPDATE customers SET rating = ? WHERE name = ?", data.asInt("rating"), "John Doe");

		// To perform a series of queries on the same physical connection, or in a transaction,
		// you will need to hold on the connection using a try...with construct.
		// This will guarantee that no one can use the same connection until you are done with it.
		try(Queryable q = db.next())
		{
			q.query("START TRANSACTION");
			
			// delete everything, OMG!
			q.query("DELETE FROM customers");
			
			// phew, undo our change because we are in a transaction
			q.query("ROLLBACK"); 
		}
	}
}
