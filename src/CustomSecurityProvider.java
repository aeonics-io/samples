
import aeonics.data.*;
import aeonics.memory.*;
import aeonics.security.*;
import aeonics.sql.*;
import aeonics.system.Logger;
import aeonics.util.*;

/**
 * This class provides a simple implementation of a security provider
 * based on a DataSource (SQL).
 */
public class CustomSecurityProvider extends Item.Abstract implements Provider
{
	/**
	 * Fetch the database connection from the registry
	 */
	private DataSource db = Registry.of(DataSource.class).get("my_db");
	
	public User getUser(String username, String password)
	{
		try
		{
			// select the user in the custom database using the password hash
			String hash = Crypto.hash(MemoryView.from(password)).get().toString();
			Data u = db.query("SELECT * FROM users WHERE user_mail = ? AND user_password = ?", username, hash);
			if( u.size() != 1 ) return null;
			
			// check if this user already exists in the registry
			User user = Registry.of(User.class).get(username);
			
			if( user == null )
			{
				// register the user in the system
				user = Registry.of(User.class).register(Factory.of(User.class).produce(User.class, u.get(0)));
			}
			else if( !user.origin().equals(id()) )
			{
				// the same user exists but belongs to another security provider
				throw new SecurityException("Duplicate user with another origin: " + user.name() + " from " + user.origin());
			}
			
			return user;
		}
		catch(Throwable t)
		{
			Logger.log(Logger.WARNING, getClass(), t);
			return null;
		}
	}
	
	public boolean isExplicitlyDenied(User user, String topic, Data context)
	{
		// To use the default behavior based on role-based policies, use:
		// return Provider.super.isExplicitlyDenied(user, topic, context);
		
		// Custom logic : restrict the user access to the specified URL
		
		if( topic.equals("http") )
		{
			String p = context.get("content").asString("path");
			p = p.substring(p.indexOf('/'));
			if( p.startsWith("/my_app") || p.startsWith("/api/my_api") )
				return !user.origin().equals(id());
		}
		
		return false;
	}

	public boolean isExplicitlyAllowed(User user, String topic, Data context)
	{
		// To use the default behavior based on role-based policies, use:
		// return Provider.super.isExplicitlyAllowed(user, topic, context);
		
		// Custom logic : restrict the user access to the specified URL
		
		if( topic.equals("http") )
		{
			String p = context.get("content").asString("path");
			p = p.substring(p.indexOf('/'));
			if( p.startsWith("/my_app") || p.startsWith("/api/my_api") )
				return user.origin().equals(id());
		}
		
		return false;
	}
}
