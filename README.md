SloppyJ
=======
A small and lenient JSON library for Java and Android

####Gradle
```groovy
repositories {
    maven { url "http://github.com/jeremy-dowdall/mvn-repo/raw/master" }
}

dependencies {
    compile 'me.licious:sloppyj:+'
}
```

Usage
----
1 - get the object you're working with:

```java
Jay.get("a:?")
```
2 - configure with any necessary options:

```java
Jay.get("a:?").withArgs("c")
```
3 - return as the desired type:
```java
Jay.get("a:?").withArgs("c").asMap();
```


####convert to / from JSON and standard objects:
```java
Map<String, Object> map = Jay.get("a:?").asMap();
List<Object> list       = Jay.get("a,b").asList();
String json1            = Jay.get(map).asJson();
String json2            = Jay.get(list).asJson();
```

####with parameterized arguments:
Any element in the JSON that consists of a single question mark character '?' can be replaced:
```java
Jay.get("?:[?,?]").withArgs("a", "b", "c").asMap(); // -> {a=[b, c]}
```
Each placeholder '?' is replaced, in order, by the objects passed into the withArgs call.
They can be used in array elements, as well as object keys and values.

####without certain fields:
```java
Jay.get("a:b,c:d").withOut("a").asMap(); // -> {c=d}
```

####get data at a certain location
```java
Jay.get("a:b:c").at("a.b").as(String.class); // -> c
```

####get a list of keys
```java
Jay.get("a:[b,{c:d}]").at("a").asKeys(); // -> [b, c]
```

####send JSON direct to an OutputStream
```java
try {
    Jay.get(data).sendJson(System.out);
} catch (IOException e) {
    e.printStackTrace();
}
```

Mapping / custom keys
---------------------
Mapping custom keys can be handled using a Mapper:
```java
Jay.get(data).withMapper(new Mapper() {
    public String fromJson(String key) {
        return toCamel(key);
	}
	public String toJson(String key) {
		return toSnake(key);
	}
}).asMap();
```

For the relatively standard mapping between snake_case (in the JSON) and Java variable names, use the Mapper.CamelSnake class:
```java
Jay.get(data).withMapper(new Mapper.CamelSnake()).asMap();
```

Adaptation
----------
Handling the conversion of specific types, to and from JSON, is done using an Adapter class:
```java
Jay.get(data).withAdapter(new Adapter() {
    public Object fromJson(Class<?> type, Object object) {
        if(type == DateTime.class) return DateTime.parse(object.toString());
        return object;
    }
    public Object toJson(Object object) {
        if(object instanceof DateTime) return object.toString();
        return null;
    }
}).asMap();
```
Note that an adapter is only for converting values within objects and arrays - not whole objects.

Custom objects
--------------
SloppyJ can also handle converting (pretty much) any object to and from JSON:
```java
class MyObject {
    public String name;
}

MyObject m = Jay.get("name:bob").as(MyObject.class);
String json = Jay.get(m).asJson(); // -> {"name":"bob"}
```

Object fields can be of any visibility (public, private, etc), as well as final.
Static and synthetics fields are skipped.

Some caveats:

1. Constructors will NEVER be called - default or otherwise - they are skipped by SloppyJ
1. Final fields must be initialized in a constructor, or to null
1. Collection fields must be of type List and the element type is subject to these caveats

To be clear about item #2 - if a final field is initialised to a non-null in its declaration, the compiler will inline it and you'll get some wonky behavior:
```java
class MyModel {
    public final String name = "bob";
}

MyObject m = Jay.get("name:joe").as(MyObject.class);

System.out.println(m.name);
// -> bob

System.out.println(Jay.get(m).asJson());
// -> {"name":"joe"}
```

Note that system classes (anything in the packages 'java' or 'android') are not converted as complex objects, but simply with a call of toString(). This works as intended for some classes, such as Uri, but may not for others. YMMV.

Sloppy input
------------
SloppyJ is all about letting you off easy when it comes to formatting your input JSON.

* double quotes, single quotes, no quotes - if you can read it, it's probably fine

```java
Jay.get("user.name:bob").asJson();   // -> {"user.name":"bob"}
```

* brackets and braces... or not

```java
Jay.get("a,b,c,d,e,f,g,h").asJson(); // -> ["a","b","c","d","e","f","g","h"]
Jay.get("a:b:c:d:e:f:g:h").asJson(); // -> {"a":{"b":{"c":{"d":{"e":{"f":{"g":"h"}}}}}}}
```

* commas schmommas - no need to use them between object or arrays

```java
Jay.get("[][][]").asJson();          // -> [[],[],[]]
Jay.get("[1,2][3,4][5,6]").asJson(); // -> [[1,2],[3,4],[5,6]]
Jay.get("{a:b}{c:d}{e:f}").asJson(); // -> [{"a":"b"},{"c":"d"},{"e":"f"}]
```

