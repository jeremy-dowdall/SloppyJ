/*
 * Copyright (C) 2014 Jeremy Dowdall <jeremyd@aspencloud.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fm.strength.sloppyj;

import static org.fest.assertions.api.Assertions.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("serial")
public class ModelTests {

	@Test
	public void test_toJson_withSystemClasses() throws Exception {
		URI uri = URI.create("http://example.com");
		assertThat(Jay.get("{uri:?}").withArgs(uri).asJson()).isEqualTo("{\"uri\":\"http://example.com\"}");
	}
	
    /** public, non-final fields */
    public static class Class00 {
        public String name;
        public int weight;
    }
    @Test
    public void test_fromJson_withPublicFields() throws Exception {
        Class00 result = Jay.get("name:bob,weight:150").as(Class00.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.weight).isEqualTo(150);
    }
    @Test
    public void test_fromJson_withExtraFields() throws Exception {
        Class00 result = Jay.get("name:bob,trainer:{name:joe},weight:150").as(Class00.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.weight).isEqualTo(150);
    }
    @Test
    public void test_fromJson_withMissingFields() throws Exception {
        Class00 result = Jay.get("name:bob").as(Class00.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.weight).isEqualTo(0);
    }
    @Test
    public void test_fromModel_withPublicFields() throws Exception {
    	Class00 model = new Class00();
    	model.name = "bob";
    	model.weight = 150;
    	
        String result = Jay.get(model).asJson();

        assertThat(result).isEqualTo("{\"name\":\"bob\",\"weight\":150}");
    }
    @Test
    public void test_fromModel_withNullFields() throws Exception {
    	Class00 model = new Class00();
    	
        String result = Jay.get(model).asJson();

        assertThat(result).isEqualTo("{\"weight\":0}");
    }

    /** private, non-final fields */
    public static class Class01 {
        private String name;
        private int weight;
    }
    @Test
    public void test_fromJson_withPrivateFields() throws Exception {
        Class01 result = Jay.get("name:bob,weight:150").as(Class01.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.weight).isEqualTo(150);
    }
    @Test
    public void test_fromModel_withPrivateFields() throws Exception {
    	Class01 model = new Class01();
    	model.name = "bob";
    	model.weight = 150;
    	
        String result = Jay.get(model).asJson();

        assertThat(result).isEqualTo("{\"name\":\"bob\",\"weight\":150}");
    }

    /** public final fields, initialized inline */
    public static class Class02 {
        public final String name = null;
        public final int weight = -1;
    }
    @Test
    public void test_fromJson_withPublicFinalFields_initializedInline() throws Exception {
        Class02 result = Jay.get("name:bob,weight:150").as(Class02.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob"); // name can be written because it was initialized as null
        assertThat(result.weight).isEqualTo(-1);     // weight cannot be written - it was inlined by the complier
    }

    /** public final fields, initialized inline */
    public static class Class03 {
        public final String name = "bob";
        public final int weight = 100;
    }
    @Test
    public void test_fromJson_withInlineInits() throws Exception {
        Class03 result = Jay.get("name:joe,weight:150").as(Class03.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob"); // name cannot be written - it was inlined by the compiler
        assertThat(result.weight).isEqualTo(100); // weight cannot be written - it was inlined by the compiler
    }

    /** public final fields, initialized in a public no-args constructor */
    public static class Class04 {
        public final String name;
        public final int weight;
        public Class04() { this.name = "ERROR!!!"; this.weight = -1; }
    }
    @Test
    public void test_fromJson_withPublicFinalFields_initializedInPublicNoArgsConstructor() throws Exception {
        Class04 result = Jay.get("name:bob,weight:150").as(Class04.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.weight).isEqualTo(150);
    }

    /** public final fields, initialized in a private no-args constructor */
    public static class Class05 {
        public final String name;
        public final int weight;
        private Class05() { this.name = null; this.weight = 0; }
    }
    @Test
    public void test_fromJson_withPublicFinalFields_initializedInPrivateNoArgsConstructor() throws Exception {
        Class05 result = Jay.get("name:bob,weight:150").as(Class05.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.weight).isEqualTo(150);
    }

    /** List field of Strings */
    public static class Class06 {
        public String name;
        public List<String> strings;
    }
    @Test
    public void test_fromJson_withListOfStrings() throws Exception {
        Class06 result = Jay.get("name:bob,strings:['s1','s2']").as(Class06.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.strings).containsExactly("s1", "s2");
    }
    @Test
    public void test_fromModel_withListOfStrings() throws Exception {
        Class06 model = new Class06();
        model.name = "bob";
        model.strings = Arrays.asList("s1", "s2");
        
        String result = Jay.get(model).asJson();

        assertThat(result).isEqualTo("{\"name\":\"bob\",\"strings\":[\"s1\",\"s2\"]}");
    }

    /** List field of Objects */
    public static class Class07 {
        public String name;
        public List<Child> children;
        public static class Child {
        	public int age;
        }
    }
    @Test
    public void test_fromJson_withListOfObjects() throws Exception {
        Class07 result = Jay.get("name:bob,children:[{age:10},{age:11}]").as(Class07.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.children).hasSize(2);
        assertThat(result.children.get(0).age).isEqualTo(10);
        assertThat(result.children.get(1).age).isEqualTo(11);
    }
    @Test
    public void test_fromModel_withListOfObjects() throws Exception {
    	Class07.Child child1 = new Class07.Child();
    	child1.age = 10;
    	Class07.Child child2 = new Class07.Child();
    	child2.age = 11;
        Class07 model = new Class07();
        model.name = "bob";
        model.children = Arrays.asList(child1, child2);
        
        String result = Jay.get(model).asJson();

        assertThat(result).isEqualTo("{\"name\":\"bob\",\"children\":[{\"age\":10},{\"age\":11}]}");
    }

    /** Map field of Objects */
    public static class Class08 {
        public String name;
        public Map<String, Child> children;
        public static class Child {
        	public int age;
        }
    }
    @Test
    public void test_fromJson_withMapOfObjects() throws Exception {
        Class08 result = Jay.get("name:bob,children:{child1:{age:10},child2:{age:11}}").as(Class08.class);

        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("bob");
        assertThat(result.children).hasSize(2);
        assertThat(result.children.get("child1").age).isEqualTo(10);
        assertThat(result.children.get("child2").age).isEqualTo(11);
    }
	@Test
    public void test_fromModel_withMapOfObjects() throws Exception {
    	final Class08.Child child1 = new Class08.Child();
    	child1.age = 10;
    	final Class08.Child child2 = new Class08.Child();
    	child2.age = 11;
        Class08 model = new Class08();
        model.name = "bob";
        model.children = new HashMap<String, ModelTests.Class08.Child>() {{
        	put("child1", child1);
        	put("child2", child2);        	
        }};
        
        String result = Jay.get(model).asJson();

        assertThat(result).isEqualTo("{\"name\":\"bob\",\"children\":{\"child1\":{\"age\":10},\"child2\":{\"age\":11}}}");
    }
	
	public static class Class09 {
		public String id;
	}
	@Test
	public void test_fromJson_convertIntsToStrings() throws Exception {
		Class09 result = Jay.get("id:123").as(Class09.class);
		
		assertThat(result.id).isEqualTo("123");
	}

}
