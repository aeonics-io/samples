import aeonics.data.Json;
import aeonics.data.Data;
import aeonics.data.Decodable;

/**
 * Flexible JSON decoder and typeless Data.
 *
 * <h2>JSON as data</h2>
<p>JSON is a powerful and simple way to share data between parties. It is widespread and available in many programming languages, platforms and frameworks.
Aeonics uses its own {@link aeonics.data.Json} implementation in order to provide a fast resilient parser without any dependency.</p>
<p>The first hassle about using JSON in Java is that it is difficult to fit a flexible data structure into a strongly typed object-oriented language.
The second burden is the ability to cope with dynamic data structures that may change over time.
To tackle both, Aeonics created the {@link aeonics.data.Data} type, which is able to wrap any native or custom data type, act as a {@link java.util.List} 
as well as a {@link java.util.Map}.
<p>The {@link aeonics.data.Data} class uses method chaining for faster and cleaner coding.</p>

<p>When using the {@link aeonics.data.Data} type, the classical mindset is reversed: "how can I fit data to my model" becomes "how can I handle my model as data".
Therefore, you have the ability to {@link aeonics.data.Data#wrap(Object)} any object and pass it along as a data component.
For convenience, wrapping is implicit when adding an object to a map or a list.</p>

<p>Conversion to-and-from JSON is always performed explicitly using the {@link aeonics.data.Encodable} and {@link aeonics.data.Decodable} interfaces.
There will never be any sort of obscure reflection mechanism to infer types or properties. Either work with data wrapped in {@link aeonics.data.Data}
or transform it back to plain old Java objects, but dont do neither when it is not necessary.</p>

<p>When working back and forth with the {@link aeonics.data.Data} type, auto-cast is performed implicitly. Meanwhile, it does not perform any type conversion.
In order to ease the process of working with data regardless of its type, conversion and test methods are available.
The <em>is*()</em> family of methods gives the ability to test if the wrapped object is an instance of an object.
The <em>as*()</em> family of methods always return a best estimate of the wrapped value.</p>

<p>When walking down hyerarchical data, you do not need to extract and expand every intermediate object. Instead, use method chaining and flexible
getters to fetch exactly what you need.</p>
 */
public class DataSample
{
	public static void main(String[] args)
	{
		System.out.println("Start");
		
		flexibleJson();
		flexibleData();
		
		System.out.println("End");
	}
	
	/**
	 * The intent of what you wrote is more important than the syntax.
	 * We try our best to make it work no matter what.
	 * And if it fails... it would have failed anyway.
	 */
	public static void flexibleJson()
	{
		// valid json
		String json = "{\"foo\": \"bar\", \"test\": true, \"answer\": 42, \"array\": [1, \"a\", null]}";
		Data data = Json.decode(json);
		
		// single quotes
		json = "{'foo': 'bar', 'test': true, 'answer': 42, 'array': [1, 'a', null]}";
		if( !data.equals(Json.decode(json)) ) System.out.println("Oops 1");
		
		// no quotes
		json = "{foo: bar, test: true, answer: 42, array: [1, a, null]}";
		if( !data.equals(Json.decode(json)) ) System.out.println("Oops 2");
		
		// all as string except null
		json = "{'foo': 'bar', 'test': 'true', 'answer': '42', 'array': ['1', 'a', null]}";
		if( !data.equals(Json.decode(json)) ) System.out.println("Oops 3");
		
		// missing comas
		json = "{foo: bar test: true answer: 42 array: [1 a null]}";
		if( !data.equals(Json.decode(json)) ) System.out.println("Oops 4");
		
		// no ending
		json = "{foo: bar, test: true, answer: 42, array: [1, a, null";
		if( !data.equals(Json.decode(json)) ) System.out.println("Oops 5");
		
		// wrong ending
		json = "{foo: bar, test: true, answer: 42, array: [1, a, null}]]}}";
		if( !data.equals(Json.decode(json)) ) System.out.println("Oops 6");
	}
	
	/**
	 * Data can store pretty much anything.
	 * You can then coerce to the final type as you like.
	 * 
	 * It works in par with {@link aeonics.data.Encodable}, {@link aeonics.data.Decodable} and
	 * {@link aeonics.data.Updatable}
	 */
	public static void flexibleData()
	{
		Data data = Data.map()
			.put("foo", "bar")
			.put("test", true)
			.put("answer", 42)
			.put("array", Data.list().add(1).add("a").add(null));
		
		// string representation is JSON
		String json = data.toString(); // {"foo": "bar", "test": true, "answer": 42, "array": [1, "a", null]}
		if( !data.equals(Json.decode(json)) ) System.out.println("Oops 7");
		
		// coerce string to int
		if( Data.of("42").asInt() != 42 ) System.out.println("Oops 8");
		
		// coerce string to boolean
		if( Data.of("true").asBool() != true ) System.out.println("Oops 9");
		
		// coerce boolean to int
		if( Data.of(false).asInt() != 0 ) System.out.println("Oops 10");
		
		// coerce boolean to string
		if( !Data.of(true).asString().equals("true") ) System.out.println("Oops 11");
		
		String answer1 = data.asString("answer"); // "42"
		int answer2 = data.asInt("answer"); // 42
		long answer3 = data.asLong("answer"); // 42L
		double answer4 = data.asDouble("answer"); // 42.0
		boolean answer5 = data.asBool("answer"); // true (because != 0)
		
		// decode to a public class
		// no black magic, no reflection, just an interface
		Answer answer6 = data.get("answer").as(Answer.class);
		if( answer6.value != 42 ) System.out.println("Oops 12");
		
		// overwrite a value with a different type
		data.put("test", answer6);
		
		// remove a value
		data.remove("answer");
		
		// get with a string or an int
		data.get("array").get(0);
		
		// check if an element exists
		data.containsKey("foo");
		
		// merge data
		Data data2 = Data.map().put("hero", "batman");
		data.merge(data2);
		if( !data.containsKey("hero") ) System.out.println("Oops 13");
		
		// check type
		if( data.isNull("foo") ) System.out.println("Oops 14");
		if( !data.isString("foo") ) System.out.println("Oops 15");
		if( data.isBool("foo") ) System.out.println("Oops 16");
		if( data.isNumber("foo") ) System.out.println("Oops 17");
		if( data.is("foo", Answer.class) ) System.out.println("Oops 18");
		if( data.isEmpty("foo") ) System.out.println("Oops 19");
	}
	
	public static class Answer implements Decodable
	{
		int value;
		public void decode(Data data) { value = data.asInt(); }
	}
}