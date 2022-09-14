import aeonics.data.Data;
import aeonics.memory.MemoryView;

/**
 * Represents a view over bytes in memory.
 * 
 * <h2>Efficiency via low-copy</h2>
<p>Java has a reputation of being memory hungry. Even though the garbage collector (gc) is very efficient, the most efficient memory is the one you don't need to use at all.
When dealing with high load of repetitive tasks, a simple {@link java.lang.String} can have significant impacts, especially when it comes from underlying I/O.</p>

<p>The perfect situation is <em>zero-copy</em> but this is simply not possible due to the way the JVM handles things. When data needs to be tackled by your piece of code,
it needs to be monitored by the JVM to ensure memory allocation and reclamation is done properly. Hence, we speak about <em>low-copy</em> instead. 
A common misbelief is to resort to the {@link java.nio.ByteBuffer} hoping it will solve everything, at the cost of exponential complexity.</p>
<p>When dealing with protocol parsing, JSON handling and payload decoding, Aeonics optimizes the memory allocations by using its low-copy {@link aeonics.memory.MemoryView} class.
The bytes gathered from I/O are kept in a single memory area and worked directly upon. This principle prevents copying bytes multiple times for temporary variables or
simply for extracting subsections. This principle improves the overall performance and latency of the system because it reduces memory consumption when possible.</p>

<p>The {@link aeonics.memory.MemoryView} class will do its best to work on the bytes in-place, but may indeed copy them to a new memory section if it is more efficient 
(or unavoidable) to do so.</p>

<p>The goal is not to substitute the standard {@link java.lang.String} because there are many concerns related to the character encoding that is ignored by the {@link aeonics.memory.MemoryView}
implementation. However, i.e. when dealing with raw bytes that come from the network that need to be interpreted as JSON, 
it is much more efficient to work directly on the raw data. This internal sauce complexity is abstracted throughout the system thanks to 
the {@link aeonics.data.Data} class that will automatically convert bytes to the requested type. This means that memory allocation is performed only if-and-when it is needed.</p>

<p>In order to allow the garbage collector (gc) to reclaim unused memory fragments, you can discard parts of your data explicitly.
However, you should keep in mind that it is more efficient to release larger areas less often than little piece by little piece. The gc is very fast at
determining that an object is still necessary in memory (and thus finish its checks early) as opposed to checking every possible way if it can be unloaded.</p>
 */
public class MemoryViewSample
{
	public static void main(String[] args)
	{
		System.out.println("Start");
		
		lowCopy();
		lateEvaluation();
		allowGC();
		
		System.out.println("End");
	}
	
	public static void lowCopy()
	{
		// CLASSIC JAVA
		String a = "Hello "; // 6 bytes of data
		String b = "World!"; // 6 bytes of data
		String c = a + b;    // 12 bytes of data copied from 2 memory areas
		// total 24 bytes of data until the garbage collector runs
		
		// LOW COPY
		String d = "Hello "; // 6 bytes of data
		String e = "World!"; // 6 bytes of data
		MemoryView f = MemoryView.from(d, e); // 0 bytes of data, no copy
		// total 12 bytes of data
		
		// CLASSIC JAVA
		String g = "Hello World!"; // 12 bytes of data
		g = g.toLowerCase();       // 12 additional bytes
		g = g.substring(0, 5);     // 5 additional bytes
		// total 29 bytes of data until the garbage collector runs

		// LOW COPY
		MemoryView h = MemoryView.from("Hello World!"); // 12 bytes of data
		h.toLowerCase();                                // 0 additional bytes
		h = h.substring(0, 5);                          // 0 additional bytes
		// total 12 bytes of data
	}
	
	public static void lateEvaluation()
	{
		// semantically 42 is a value. Whether it is a string, bytes or an int
		// does not affect its meaning. It is only if-and-when you actually use it that
		// its type should match what you expect.
		
		// CLASSIC JAVA
		byte[] data = new byte[] { '4', '2' };
		String a = new String(data);
		Integer b = Integer.valueOf(a);
		// 3 distinct memory usage needed

		// LATE EVALUATION
		byte[] data2 = new byte[] { '4', '2' };
		byte[] data3 = new byte[] { '4', '3' };
		Data c = Data.map()
			.put("d", MemoryView.from(data2))
			.put("e", MemoryView.from(data3));
		
		int f = c.asInt("d"); // data2 bytes to int conversion : 42
		// data3 bytes are never converted and do not need another decoding or memory area to hold the value
	}
	
	public static void allowGC()
	{
		// when constructed from multiple memory segments,
		// the garbage collector can reclaim the discarded segments
		
		MemoryView b = MemoryView.from("Hello", " ", "World!");
		b.discardBefore(6); // get rid of "Hello "

		// "Hello" and " " can be gc'd
		// "World!" remains in memory
	}
}