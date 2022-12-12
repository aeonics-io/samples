import aeonics.rest.*;
import aeonics.data.*;
import aeonics.util.*;
import aeonics.memory.*;
import aeonics.system.*;

/**
 * Here are some examples of how to create REST endpoints.
 * Remember to register endpoints in the Registry using:
 * 		Registry.of(Endpoint.class).register(x);
 */
public class RestEndpointSyntax
{
	/**
	 * The most simple mapping from a URL with one method.
	 * Just return a Data object that will be transformed to JSON in the response.
	 */
	public Endpoint a = new RestEndpoint("/path/a", "GET")
	{
		public Data handle(Data parameters) { return Data.map().put("hello", "world"); }
	}
	
	/**
	 * You can use multiple methods for the same URL.
	 * (even custom methods, yes it is allowed by the HTTP standard!)
	 */
	public Endpoint b = new RestEndpoint("/path/b", "GET", "POST", "FOOBAR")
	{
		public Data handle(Data parameters) { return Data.map().put("hello", "world"); }
	}
	
	/**
	 * Add parameters to your endpoint, they will be validated automatically
	 */
	public Endpoint c = new RestEndpoint("/path/c", "GET")
	{
		public Data handle(Data parameters) { return Data.map().put("hello", parameters.get("name")); }
	}
	.add(new Parameter("name").optional(false).max(50));
	
	/**
	 * You can map the parameters in the URL if you want
	 */
	public Endpoint d = new RestEndpoint("/path/d/{name}", "GET")
	{
		public Data handle(Data parameters) { return Data.map().put("hello", parameters.get("name")); }
	}
	.add(new Parameter("name").optional(false).max(50));
	
	/**
	 * In case of errors, throw a RestException with the desired http response code.
	 * Other uncaught exceptions will be returned as code 500.
	 */
	public Endpoint e = new RestEndpoint("/path/e", "GET")
	{
		public Data handle(Data parameters) throws Exception { throw new RestException(400, "Some error happened"); }
	}
	


	
	
	/**
	 * If you want to validate the user and filter the request/response
	 * you can use the TriggerRestEndpoint instead.
	 */
	public Endpoint f = new TriggerRestEndpoint("/path/f/{name}", "GET")
	{
		public Data handle(Data parameters, User user) { 
			return Data.map()
				.put("hello", parameters.get("name"))
				.put("you are", parameters.get("login");
		}
	}
	/**
	 * Check the user here (or ignore this method)
	 */
	.security((request, user) -> { if( user == User.ANONYMOUS ) throw new SecurityException(); })
	/**
	 * Filter or modify the request parameters here (or ignore this method)
	 */
	.before((request, user) -> { request.put("login", user.name()); })
	/**
	 * Filter or modify the response here (or ignore this method)
	 */
	.after((request, response, user) -> { response.put("modified", true); })
	/**
	 * Finally add regular parameters
	 */
	.add(new Parameter("name").optional(false).max(50));
	


	
	
	/**
	 * If you just want a pass through to CRUD from a database,
	 * you can use the DataEndpoint which will expose the SQL columns as parameters
	 * and perform the SQL logic for you.
	 *
	 * The DataSource should be registered in the Registry to manage the connection to the database.
	 */
	public Endpoint g = new DataEndpoint("/path/g", "POST", "datasource_name", "db_name", "table_name", DataEndpoint.Operation.INSERT)
	/**
	 * You can use the same methods as the TriggerRestEndpoint
	 */
	.security((request, user) -> { })
	.before((request, user) -> { })
	.after((request, response, user) -> { });
}