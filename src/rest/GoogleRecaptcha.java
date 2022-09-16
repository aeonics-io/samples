import aeonics.rest.*;
import aeonics.data.*;
import aeonics.util.*;
import aeonics.memory.*;
import aeonics.system.*;
import java.util.function.Supplier;

/**
 * Sample rest endpoint to receive a contact form and
 * validate the Google Recaptcha v3 token to avoid spam.
 *
 * How to deploy: from the Aeonics admin interface
 *   1) click on upload REST endpoint.
 *   2) review the code then click on deploy.
 *
 * @see https://developers.google.com/recaptcha/docs/v3
 *
 * Sample HTML contact form:

<script type="text/javascript">

	const GOOGLE_RECAPTCHA_KEY = 'your_key_here';
	
	// inject the Google Recaptcha script
	
	let s = document.createElement('script');
	s.id='recaptcha';
	s.async=true;
	s.defer=true;
	s.src='https://www.google.com/recaptcha/api.js?render=' + GOOGLE_RECAPTCHA_KEY;
	document.head.append(s);

	// handler for the form submit action in the background
	
	submitForm(form) {
		grecaptcha.ready(function() {
			grecaptcha.execute(GOOGLE_RECAPTCHA_KEY, { action: 'contact' })
			.then(function(token) { 
				form.elements.gtoken.value = token;
				fetch('//'+location.host+'/api/contact', {method:'post', body: new FormData(form)})
					.then(r=>showSuccess())
					.catch(e=>showError());
			});
		});
	}
	showSuccess() { }
	showError() { }
</script>

<form onsubmit="event.preventDefault(); submitForm(this);">
	<input type="text"   name="name" />
	<input type="text"   name="company" />
	<input type="email"  name="email" />
	<input type="tel"    name="phone" />
	<textarea            name="message"></textarea>
	<input type="hidden" name="gtoken" />
	
	<button type="submit">Send</button>
</form>

 */
public class Microservice implements Supplier<RestEndpoint>
{
	public RestEndpoint get()
	{
		return new RestEndpoint("/contact", "POST")
		{
			private String GOOGLE_RECAPTCHA_SECRET = "your_google_recaptcha_secret";
			
			/**
			 * Checks the Google Recaptcha validity and proceed.
			 *
			 * @param parameters the validated input parameters of your rest endpoint (see below)
			 * @return the response to send to the web page
			 */
			public Data handle(Data parameters) throws RestException
			{
				// check the validity
				checkRecaptcha(parameters.asString("gtoken"));
				parameters.remove("gtoken");
				
				// forward the form content to the next handler
				Topic.publish(new Message("contact", parameters), "mail");
				
				// return an empty response
				return null;
			}
		
			/**
			 * Validates the Google Recaptcha token.
			 * @see https://developers.google.com/recaptcha/docs/verify
			 *
			 * @param value the recaptcha token
			 * @throws RestException if the validation fails
			 */
			private void checkRecaptcha(String value) throws RestException
			{
				try
				{
					Data response = Http.post(
						"https://www.google.com/recaptcha/api/siteverify", 
						Data.map()
							.put("secret", GOOGLE_RECAPTCHA_SECRET)
							.put("response", value)
						);
					
					if( !response.asBool("success") )
						throw new RuntimeException("Invalid Recaptcha token");
					if( !response.asString("action").equals("contact") )
						throw new RuntimeException("Invalid Recaptcha action");
					if( response.asDouble("score") < 0.5 )
						throw new RuntimeException("Invalid recaptcha score");
				}
				catch(Exception e)
				{
					Logger.log(Logger.FINE, "Recaptcha", e);
					// do not provide details to the user, just say it failed
					throw new RestException(400, "Recaptcha validation failed");
				}
			}
		}
		
		// Those are the input parameters that this endpoint accepts.
		// It should match the form fields from the HTML
		
		.add(new Parameter("name").optional(false).min(1).max(50))
		.add(new Parameter("company").optional(true).min(0).max(50))
		.add(new Parameter("email").optional(false).min(6).max(50).rule(Parameter.EMAIL))
		.add(new Parameter("phone").optional(true).min(0).max(20))
		.add(new Parameter("message").optional(false).min(5).max(5000))
		.add(new Parameter("gtoken").optional(false).min(1).max(1000));
	}
}