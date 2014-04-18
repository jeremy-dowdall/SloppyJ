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
import static org.fest.assertions.data.MapEntry.entry;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class JayTests {

    @Test
    public void test_find_inObject() throws Exception {
		Map object = new HashMap() {{
    		put("a", new HashMap() {{
    			put("b", "c");
    		}});
    	}};
        assertThat(Jay.get(object).at("a.b").as(String.class)).isEqualTo("c");
    }

    @Test
    public void test_find_inArray() throws Exception {
        assertThat(Jay.get("a:[b]").at("a").asList()).containsExactly("b");
    }
    
    @Test
    public void test_find_inJson() throws Exception {
    	assertThat(Jay.get("a:b:c").at("a.b").as(String.class)).isEqualTo("c");
    }
    
    @Test
    public void test_keys() throws Exception {
		Map object = new HashMap() {{
    		put("a", "b");
			put("c", "d");
    	}};
        assertThat(Jay.get(object).asKeys()).containsOnly("a", "c");
    }
    
    @Test
    public void test_keys_atLocation() throws Exception {
    	assertThat(Jay.get("a:[b,{c:d}]").at("a").asKeys()).containsExactly("b", "c");
    }

    @Test
    public void test_fromJson_toArray() throws Exception {
    	assertThat(Jay.get("[1, 2, 3]").as(Object[].class)).containsExactly(1, 2, 3);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void test_fromJson_toArray_ofInvalidType() throws Exception {
    	assertThat(Jay.get("[1, 2, 3]").as(Integer[].class)).containsExactly(1, 2, 3);
    }
    
	@Test
	public void test_fromJson_toList_withNull() throws Exception {
		assertThat(Jay.get((String) null).asList()).isEmpty();
	}
	
	@Test
	public void test_fromJson_toList_withEmptyString() throws Exception {
		assertThat(Jay.get("").asList()).isEmpty();
		assertThat(Jay.get(" ").asList()).isEmpty();
		assertThat(Jay.get("	  	").asList()).isEmpty();
	}
	
	@Test
	public void test_fromJson_toList_withEmptyArray() throws Exception {
		assertThat(Jay.get("[]").asList()).isEmpty();
		assertThat(Jay.get("[  ]").asList()).isEmpty();
		assertThat(Jay.get("[ 	 	]").asList()).isEmpty();
	}
	
	@Test
	public void test_fromJson_toList_withIntegers() throws Exception {
		assertThat(Jay.get("[1]").asList()).containsExactly(1);
		assertThat(Jay.get("[1,2]").asList()).containsExactly(1,2);
		assertThat(Jay.get("[ 1 , 2 ]").asList()).containsExactly(1,2);
		assertThat(Jay.get("[ 1  2 ]").asList()).containsExactly("1  2");
	}
	
	@Test
	public void test_fromJson_toList_withNegativeIntegers() throws Exception {
		assertThat(Jay.get("[-1]").asList()).containsExactly(-1);
		assertThat(Jay.get("[1,-2]").asList()).containsExactly(1,-2);
		assertThat(Jay.get("[ -1 , -2 ]").asList()).containsExactly(-1,-2);
	}
	
	@Test
	public void test_fromJson_toList_withDoubles() throws Exception {
		assertThat(Jay.get("[1.0]").asList()).containsExactly(1.0);
		assertThat(Jay.get("[1.123,.2]").asList()).containsExactly(1.123,0.2);
		assertThat(Jay.get("[ 1.98 , .982 ]").asList()).containsExactly(1.98,0.982);
		assertThat(Jay.get("[ .9.8.2. ]").asList()).containsExactly(".9.8.2.");
		assertThat(Jay.get("[ 1.9 8.2 ]").asList()).containsExactly("1.9 8.2");
	}

	@Test
	public void test_fromJson_toMap_withNull() throws Exception {
		assertThat(Jay.get((String) null).asMap()).isNull();
	}
	
	@Test
	public void test_fromJson_toMap_withEmptyString() throws Exception {
		assertThat(Jay.get("").asMap()).isNull();
		assertThat(Jay.get(" ").asMap()).isNull();
		assertThat(Jay.get("	  	").asMap()).isNull();
	}
	
	@Test
	public void test_fromJson_toMap_withEmptyObject() throws Exception {
		assertThat(Jay.get("{}").asMap()).isEmpty();
		assertThat(Jay.get("{  }").asMap()).isEmpty();
		assertThat(Jay.get("{ 	 	}").asMap()).isEmpty();
	}
	
	@Test
	public void test_fromJson_toMap_withSimplePairs() throws Exception {
		assertThat(Jay.get("{\"a\":\"b\"}").asMap().get("a")).isEqualTo("b");
	}

	@Test
	public void test_fromJson_toMap_withSloppyPairs() throws Exception {
		assertThat(Jay.get("a:b").asMap().get("a")).isEqualTo("b");
		assertThat(Jay.get("a:-1").asMap().get("a")).isEqualTo(-1);
	}
	
	@Test
	public void test_fromJson_toMap_withArrayOfObjects() throws Exception {
		assertThat(Jay.get("s:[{$count:*}]").asJson()).isEqualTo("{\"s\":[{\"$count\":\"*\"}]}");
	}
	
	@Test
	public void test_fromJson_toMap_withPairsAfterArray() throws Exception {
		assertThat(Jay.get("a:[b],c:d").asJson()).isEqualTo("{\"a\":[\"b\"],\"c\":\"d\"}");
	}
	
	@Test
	public void test_fromJson_toMap_withParameterizedValues() throws Exception {
		assertThat(Jay.get("a:?").withArgs("b,c").asMap().get("a")).isEqualTo("b,c");
		assertThat(Jay.get("{a:?}").withArgs("b,c").asMap().get("a")).isEqualTo("b,c");
		assertThat(Jay.get("{a:?}").withArgs("'b,c'").asMap().get("a")).isEqualTo("'b,c'"); // values are _not_ JSON processed
		assertThat(Jay.get("a: ? ").withArgs("'b,c'").asMap().get("a")).isEqualTo("'b,c'");
	}

	@Test
	public void test_fromJson_toMap_withParameterizedKeysAndValues() throws Exception {
		assertThat(Jay.get("?:?").withArgs("a","b").asMap()).contains(entry("a", "b"));
	}

	@Test
	public void test_fromJson_toJson_withParameterizedKeysAndValues() throws Exception {
		assertThat(Jay.get("?:[?,?]").withArgs("a","b","c").asJson()).isEqualTo("{\"a\":[\"b\",\"c\"]}");
	}

	@Test
	public void test_fromArray_ofPrimitives_asJson() {
        assertThat(Jay.get(1, 2, 3).asJson()).isEqualTo("[1,2,3]");
        assertThat(Jay.get(1d, 2, 3d).asJson()).isEqualTo("[1.0,2,3.0]");
        assertThat(Jay.get('j', 'a', 'y').asJson()).isEqualTo("[\"j\",\"a\",\"y\"]");
	}

	@Test
	public void test_fromArray_ofObjects_asJson() {
        assertThat(Jay.get("col1", "col2").asJson()).isEqualTo("[\"col1\",\"col2\"]");
        assertThat(Jay.get(new Object[] { "col1", "col2" }).asJson()).isEqualTo("[\"col1\",\"col2\"]");
	}

	@Test
	public void test_fromJson_toList_withTrailingComma() {
        assertThat(Jay.get("[a, b, ]").asList()).containsExactly("a", "b");
	}

	@Test
	public void test_fromJson_toMap_withTrailingComma() {
        assertThat(Jay.get("a:b, c:d, ").asJson()).isEqualTo("{\"a\":\"b\",\"c\":\"d\"}");
	}

    @Test
    public void test_fromSloppyJson_asJson() throws Exception {
        assertThat(Jay.get("a:b").asMap()).contains(entry("a","b"));
        assertThat(Jay.get("a:\"b\"").asJson()).isEqualTo("{\"a\":\"b\"}");
        assertThat(Jay.get("[a]").asJson()).isEqualTo("[\"a\"]");
        assertThat(Jay.get("['a b']").asJson()).isEqualTo("[\"a b\"]");
		assertThat(Jay.get("c:{d:e,f:g}").asJson()).isEqualTo("{\"c\":{\"d\":\"e\",\"f\":\"g\"}}");
		assertThat(Jay.get("c:{d:e,f:{g:h,i:j}}").asJson()).isEqualTo("{\"c\":{\"d\":\"e\",\"f\":{\"g\":\"h\",\"i\":\"j\"}}}");
		assertThat(Jay.get("a:[b,c:{d:e,f:{g:h,i:j}}]").asJson()).isEqualTo("{\"a\":[\"b\",{\"c\":{\"d\":\"e\",\"f\":{\"g\":\"h\",\"i\":\"j\"}}}]}");
		assertThat(Jay.get("a:[b,c:{d:e,f:{g:'h i',j:k}}]").asJson()).isEqualTo("{\"a\":[\"b\",{\"c\":{\"d\":\"e\",\"f\":{\"g\":\"h i\",\"j\":\"k\"}}}]}");
    }

	@Test
	public void testSloppyKeys() throws Exception {
		assertThat(Jay.get("{mail.send: { prop1: val1 }}").asMap()).containsKey("mail.send");
	}

	@Test
	public void test_fromDate_toJson() throws Exception {
		final Date date = new Date();
		Map object = new HashMap() {{
			put("createdAt", date);
		}};
		assertThat(Jay.get(object).asJson()).isEqualTo("{\"createdAt\":"+date.getTime()+"}");
	}

	@Test
	public void test_fromJson_withAdapter() throws Exception {
		final String[] names = new String[] { "bob", "joe", "dan" };
		List<Object> list = Jay.get("[1,2,3]").withAdapter(new Adapter() {
			public Object toJson(Object object) {
				throw new RuntimeException("shouldn't be here...");
			}
			public Object fromJson(Class<?> type, Object json) {
				return names[((int) json) - 1];
			}
		}).asList();
		
		assertThat(list).containsExactly(names[0], names[1], names[2]);
	}

	@Test
	public void test_fromMap_withAdapter() throws Exception {
		Map map = new LinkedHashMap() {{
			put("a", Calendar.getInstance());
			put("b", "c");
		}};
		String json = Jay.get(map).withAdapter(new Adapter() {
			public Object toJson(Object object) {
				if(object instanceof Calendar) return "I'm a Calendar!";
				return object;
			}
			public Object fromJson(Class<?> type, Object json) {
				throw new RuntimeException("shouldn't be here...");
			}
		}).asJson();
		
		assertThat(json).isEqualTo("{\"a\":\"I'm a Calendar!\",\"b\":\"c\"}");
	}

	@Test
	public void test_fromJson_withMapper() throws Exception {
		Map<String, Object> map = Jay.get("a:b").withMapper(new Mapper() {
			public String toJson(String key) {
				throw new RuntimeException("shouldn't be here...");
			}
			public String fromJson(String key) {
				return "a".equals(key) ? "c" : "FAIL";
			}
		}).asMap();
		
		assertThat(map).contains(entry("c", "b"));
	}
	
	@Test
	public void test_fromJson_withMapper_withNullKey() throws Exception {
		Map<String, Object> map = Jay.get("a:b").withMapper(new Mapper() {
			public String toJson(String key) {
				throw new RuntimeException("shouldn't be here...");
			}
			public String fromJson(String key) {
				return null;
			}
		}).asMap();
		
		assertThat(map).isEmpty();
	}
	
	@Test
	public void test_fromMap_withMapper() throws Exception {
		Map map = new HashMap() {{
			put("a", "b");
		}};
		
		String json = Jay.get(map).withMapper(new Mapper() {
			public String toJson(String key) {
				return "a".equals(key) ? "special_key" : "FAIL";
			}
			public String fromJson(String key) {
				throw new RuntimeException("shouldn't be here...");
			}
		}).asJson();
		
		assertThat(json).isEqualTo("{\"special_key\":\"b\"}");
	}
	
}
