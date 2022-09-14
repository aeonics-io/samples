import aeonics.data.Data;
import aeonics.data.Json;
import aeonics.system.Unique.ID;
import aeonics.util.Factory;
import aeonics.util.Item;
import aeonics.util.Registry;

/**
 * <h2>Reusable Items</h2>
<p>A major feature builtin the core of Aeonics is the ability to offer configurable components that can be (de)serialized from
a simple JSON structure, and even modified at runtime.</p>

<p>An {@link aeonics.util.Item} is a {@link aeonics.system.Unique} and optionally {@link aeonics.data.Encodable}, {@link aeonics.data.Decodable}, {@link aeonics.data.Updatable}, {@link aeonics.util.Disposable} element with a public parameterless constructor.
This means that we can build a new instance with the {@link aeonics.util.Factory} and unambiguously retrieve an instance from the {@link aeonics.util.Registry} to update it.</p>

<h3>Factory</h3>
<p>In order to make your items accessible to the entire system, you need to register them in a {@link aeonics.util.Factory}. 
Since there can be numerous types of items, it is advisable to register them into a dedicated group of that type.</p>

<p>Once the items are registered, they can be constructed anywhere in the code using a simple JSON representation. 
The {@link #decode(Data)} method will be called with the provided initialization parameters where the <em>__class</em> 
property specifies the final item class name.</p>

<h3>Registry</h3>
<p>If you want your item instances to persist and be available anywhere in the code, you can register them in the {@link aeonics.util.Registry}.
Since there can be numerous types of items, it is advisable to register them into a dedicated group of that type.</p>

<p>Once the items are registered, they can be retrieved anywhere in the code using their {@link Item#name()} or {@link Item#id()}.
Note that an auto-cast is performed, but it does not prevent a <em>ClassCastException</em> exception if the type does not match.</p>

<p>When an item is no longer needed, simply unregister it so that it can be cleared from memory.</p>

<p>Together, the {@link aeonics.util.Factory} and {@link aeonics.util.Registry} allow to dynamically build items and update them at runtime from anywhere in the system.</p>

<p>Note that Aeonics never ever uses reflection to infer types, names or else. 
The {@link #encode()}, {@link #decode(Data)} and {@link #update(Data)} methods provide an explicit way to perform operations without
untraceable black magic. The behavior is therefore yours to own and extend.</p>
 */
public class ItemSample
{
	public static abstract class Animal extends Item.Abstract
	{
		String color = null;
		public Data encode() { return super.encode().put("color", color); }
	    public void decode(Data value) { super.decode(value); color = value.asString("color"); }
	    public void update(Data value) { super.decode(value); color = value.asString("color"); }
	}

	public static class Cat extends Animal { }
	public static class Dog extends Animal { }
	
	public static void main(String[] args)
	{
		System.out.println("Start");
		
		factory();
		registry();
		
		System.out.println("End");
	}
	
	public static void factory()
	{
		// register items in the factory to be able to build them from anywhere
		Factory.of(Animal.class).put(Cat.class);
		Factory.of(Animal.class).put(Dog.class);

		// build using plain JSON
		Cat cat = Factory.of(Animal.class)
            .produce("{'__class': 'ItemSample$Cat', 'color': 'Black'}");
		
		// build using Data
		Dog dog = Factory.of(Animal.class)
            .produce(Dog.class, Data.map().put("color", "Brown"));
	}
	
	public static void registry()
	{
		Cat cat = new Cat();
		cat.name("Bob");
		
		// register instances in the registry to make them accessible from anywhere
		Registry.of(Animal.class).register(cat);
		
		Dog dog = new Dog();
		ID id = dog.id();
		
		Registry.of(Animal.class).register(dog);
		
		// retrieve items using their name or id
		Cat cat2 = Registry.of(Animal.class).get("Bob"); // auto-cast to Cat
		Dog dog2 = Registry.of(Animal.class).get(id); // auto-cast to Dog
		
		// remove items from the registry
		Registry.of(Animal.class).unregister("Bob");
	}
	
	public static void allTogether()
	{
		// create an item and register it
		String config = "{'__class': 'ItemSample$Dog', 'color': 'Brown', 'name': 'Joe'}";
		Animal animal = Registry.of(Animal.class)
	        .register(Factory.of(Animal.class)
                .produce(config));

		// modify an existing item
		String update = "{'color': 'Purple'}";
		Registry.of(Animal.class)
			.get("Joe")
		    .update(Json.decode(update));
	}
}
