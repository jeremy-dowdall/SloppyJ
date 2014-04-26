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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Jay {

	public static final String ERR_NULL_KEY = "key cannot be null";


	public static Jay get(Object object) {
    	return new Jay(object);
    }

    public static Jay get(Object...array) {
    	return new Jay(array);
    }

    
    Object data;

    private Class<?> objType;
    private Adapter adapter;
    private Mapper mapper;
    private Set<String> skip;
    
    private String[] path;
	private Object[] args;
	private int nextArg;
    
    private Jay(Object from) {
    	this.data = from;
    }

    
    @SuppressWarnings("unchecked")
	public <T> T as(Class<T> type) {
    	if(type == null) {
    		return null;
    	}
    	if(type.isArray() && type != Object[].class) {
    		throw new IllegalArgumentException("only Object arrays are supported");
    	}
    	if(data != null) {
    		if(path != null) {
    			if(data instanceof String) data = new JsonParser(this, type).toJava();
    			data = find(0, data);
    		}
    		else if(type != data.getClass()) {
				if(objType == null) objType = type;
				if(!(data instanceof String)) data = new JsonBuilder(this).toJson();
				data = new JsonParser(this, type).toJava();
			}
    	}
    	if(data == null) {
    		if(List.class.isAssignableFrom(type)) data = new ArrayList<>(0);
    	}
    	if(type.isPrimitive()) {
    		if(type == boolean.class) return (T) ((data instanceof Boolean) ? data : false);
    		if(type == long.class) return (T) ((data instanceof Long) ? data : 0l);
    		return (T) ((data instanceof Integer) ? data : 0);
    	}
    	return (type != null && type.isInstance(data)) ? type.cast(data) : null;
    }
    
    public String asJson() {
    	if(data == null) return null;
		if(data instanceof String) data = new JsonParser(this, Object.class).toJava();
    	if(path != null) data = find(0, data);
		return new JsonBuilder(this).toJson();
    }
    
    public List<String> asKeys() {
    	if(data == null) return new ArrayList<String>(0);
    	if(data instanceof String) data = new JsonParser(this, Object.class).toJava();
    	if(path != null) data = find(0, data);
    	return addKeys(data, new ArrayList<String>());
    }
    
    @SuppressWarnings("unchecked")
	public List<Object> asList() {
    	return as(List.class);
    }
    
    @SuppressWarnings("unchecked")
	public <E> List<E> asList(Class<E> elementType) {
		objType = elementType;
		return (List<E>) as(List.class);
    }
    
    @SuppressWarnings("unchecked")
	public Map<String, Object> asMap() {
    	return as(Map.class);
    }
    
    public Jay at(String path) {
    	this.path = path.split("[\\.:]");
    	return this;
    }
    
    public void sendJson(OutputStream out) throws IOException {
    	if(data != null) {
    		if(data instanceof String) Jay.get(asMap()).sendJson(out);
    		else new JsonBuilder(this).sendJson(out);
    	}
    }

    
    public Jay withArgs(Object...args) {
    	this.args = args;
    	this.nextArg = 0;
    	return this;
    }
    
    public Jay withAdapter(Adapter adapter) {
    	this.adapter = adapter;
    	return this;
    }
    
    public Jay withMapper(Mapper mapper) {
    	this.mapper = mapper;
    	return this;
    }
    
    public Jay withOut(String...keys) {
    	this.skip = new HashSet<>(Arrays.asList(keys));
    	return this;
    }
    

    Object adaptFromJson(ObjectWrapper wrapper, String key, Object json) {
    	if(adapter != null) {
    		Class<?> type = (wrapper != null) ? wrapper.getType(key) : Object.class;
    		if(type != null) return adapter.fromJson(type, json);
    	}
    	return json;
    }
    
    Object adaptToJson(Object value) {
    	return (adapter != null) ? adapter.toJson(value) : value;
    }
    
    ObjectWrapper getWrapper() {
    	return new ObjectWrapper(objType);
    }
    
    boolean include(String key) {
    	return (skip != null) ? !skip.contains(key) : (key != null);
    }
    
    String mapFromJson(String key) {
    	return (mapper != null) ? mapper.fromJson(key) : key;
    }
    
    String mapToJson(String key) {
    	return (mapper != null) ? mapper.toJson(key) : key;
    }
    
    Object nextArg() {
    	return (args != null) ? args[nextArg++] : "?";
    }
    
    
    private List<String> addKeys(Object o, List<String> keys) {
    	if(o instanceof Iterable) {
    		for(Object i : (Iterable<?>) o) {
                if(i instanceof Map) addKeys(i, keys);
                else if(i != null) keys.add(i.toString());
    		}
    	} else if(o instanceof Map) {
    		addKeys(((Map<?,?>) o).keySet(), keys);
        }
        return keys;
    }
 
    private Object find(int i, Object o) {
        return (i < path.length-1) ? find(i+1, get(i, o)) : get(i, o);
    }

    private Object get(int i, Object o) {
        if(o instanceof Map) {
            return ((Map<?,?>) o).get(path[i]);
        }
        if(o instanceof List) {
            try {
                return ((List<?>) o).get(Integer.parseInt(path[i]));
            } catch(NumberFormatException e) {
                for(Object el : (List<?>) o) {
                    if(path[i].equals(el)) return el;
                    if(el instanceof Map) {
                    	return ((Map<?,?>) el).get(path[i]);
                    }
                }
            }
        }
        return null;
    }

}
