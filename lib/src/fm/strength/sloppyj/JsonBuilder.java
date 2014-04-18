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

import static fm.strength.sloppyj.Jay.ERR_NULL_KEY;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class JsonBuilder {
	
	private final Jay jay;
	private Writer w;
	
	public JsonBuilder(Jay jay) {
		this.jay = jay;
	}
	
	String toJson() {
		try {
			w = new StringWriter();
	        appendValue(jay.data);
			return w.toString();
		} catch(IOException e) {
			return null;
		}
	}
	
	void sendJson(OutputStream out) throws IOException {
		w = new OutputStreamWriter(out);
		appendValue(jay.data);
		w.flush();
	}
	

	private void append(char s, Iterable<?> iter, char e) throws IOException {
		w.write(s);
		boolean sep = false;
		for(Object i : iter) {
			if(i instanceof Entry)  {
				Entry<?,?> entry = (Entry<?,?>) i;
				sep = append(entry.getKey(), entry.getValue(), sep) || sep;
			}
			else {
				if(sep) w.write(',');
				appendValue(i);
				sep = true;
			}
		}
		w.write(e);
	}
	
    private boolean append(Object k, Object val, boolean sep) throws IOException {
        if(k == null) throw new IllegalArgumentException(ERR_NULL_KEY);
        String key = k.toString();
        if(jay.include(key)) {
			if(sep) w.write(',');
        	w.write('"');
        	w.write(jay.mapToJson(key));
        	w.write('"');
        	w.write(':');
			appendValue(val);
			return true;
        }
        return false;
    }

	private void appendArray(Object array) throws IOException {
		w.write('[');
		for(int i = 0; i < Array.getLength(array); i++) {
			if(i != 0) w.write(',');
			appendValue(Array.get(array, i));
		}
		w.write(']');
	}
	
    private void appendEscaped(String s) throws IOException {
    	w.write('"');
    	if(s.length() > 0) {
    		char b;
    		char c = 0;
    		for(int i = 0; i < s.length(); i++) {
    			b = c; c = s.charAt(i);
    			switch(c) {
    			case '/':
    				if(b == '<') w.write('\\');
    				w.write(c);
    				break;
    			case '\\': w.write("\\\\"); break;
    			case '"':  w.write("\\\""); break;
    			case '\b': w.write("\\b"); break;
    			case '\t': w.write("\\t"); break;
    			case '\n': w.write("\\n"); break;
    			case '\f': w.write("\\f"); break;
    			case '\r': w.write("\\r"); break;
    			default:
    				if(c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
    					String hex = Integer.toHexString(c);
    					w.write("\\u");
    					w.write("0000", 0, 4 - hex.length());
    					w.write(hex);
    				} else {
    					w.write(c);
    				}
    			}
    		}
    	}
    	w.write('"');
    }
    
    private void appendModel(Object o) throws IOException {
    	w.write('{');
		boolean sep = false;
		for(Field field : o.getClass().getDeclaredFields()) {
			if(!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
				try {
					field.setAccessible(true);
					sep = append(field.getName(), field.get(o), sep) || sep;
				} catch(IllegalAccessException e) {
					// should never happen...
					throw new RuntimeException(e);
				}
			}
		}
    	w.write('}');
    }
	
    private void appendValue(Object o) throws IOException {
    	if(o == null) {
    		w.write("null");
    	} else {
    		o = jay.adaptToJson(o);
	    	if(o instanceof Boolean)        w.write(o.toString());
	    	else if(o instanceof Number)    w.write(o.toString());
	    	else if(o instanceof String)    appendEscaped(o.toString());
	    	else if(o instanceof Character) appendEscaped(o.toString());
	    	else if(o instanceof Date)      w.write(Long.toString(((Date) o).getTime()));
	    	else if(o instanceof Map)       append('{', ((Map<?,?>) o).entrySet(), '}');
	    	else if(o instanceof Iterable)  append('[', (Iterable<?>) o, ']');
	    	else if(o.getClass().isArray()) appendArray(o);
	    	else if(isSystem(o))            appendEscaped(o.toString());
	    	else                            appendModel(o);
    	}
    }
    
    private boolean isSystem(Object o) {
    	String cname = o.getClass().getName();
    	return (cname.startsWith("java.") || cname.startsWith("android."));
    }
    
}
