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

import java.util.ArrayList;


public class JsonParser {
	
	public static final String ERR_CHAR_ARRAY = "java.util.String appears to have changed its implementation...";

    
    private final Jay jay;
    private final Class<?> type;
    private final boolean array;
    private final String s;
    private int pos;
    
	JsonParser(Jay jay, Class<?> type) {
		this.jay = jay;
		this.type = type;
		this.array = type.isArray();
		this.s = jay.data.toString();
	}
	
	
	Object toJava() {
		ArrayList<Object> result = new ArrayList<Object>(1);
		while(ff()) {
			result.add(getValue(null, null));
		}
		if(result.size() == 0) return null;
		if(result.size() == 1) return result.get(0);
		return array ? result.toArray() : result;
	}
	
	
	private boolean ff() {
		return ff((char) 0) == ' ';
	}
	
	private char ff(char f) {
		while(pos < s.length()) {
			char c = s.charAt(pos);
			if(c == f || c == ']' || c == '}' || c == ':' || c == ',') return c;
			if(f == 0) {
				if(!Character.isWhitespace(c)) return ' ';
			} else {
				if(c == '[' || c == '{') return c;
			}
			pos++;
		}
		return 0;
	}

	private Object getArray(ObjectWrapper parent, String key) {
		ArrayList<Object> list = new ArrayList<Object>();
		while(ff()) {
			list.add(getValue(parent, key));
			char c = ff(',');
			if(c == ',') pos++;
			else if(c != '[' && c != '{') break;
		}
		pos++;
		list.trimToSize();
		return array ? list.toArray() : list;
	}

	private Object getObject(ObjectWrapper parent, String parentKey) {
		ObjectWrapper wrapper = (parent != null) ? parent.get(parentKey) : jay.getWrapper();
		while(ff()) {
			String key = getKey();
			if(ff(':') == ':') {
				pos++;
				if(ff() && wrapper != null && jay.include(key)) {
					wrapper.set(key, getValue(wrapper, key));
				}
				if(ff(',') == ',') pos++;
				else break;
			}
		}
		pos++;
		return (wrapper != null) ? wrapper.get() : null;
	}
	
	private String getKey() {
		String key = null;
		switch(s.charAt(pos)) {
		case '"':
		case '\'':
			pos++; key = getString();
			break;
		default:
			int start = pos;
			while(pos < s.length() && s.charAt(pos) != ':' && s.charAt(pos) != ',' && s.charAt(pos) != '}') pos++;
			key = newString(start, pos);
		}
		return "?".equals(key) ? String.valueOf(jay.nextArg()) : jay.mapFromJson(key);
	}
	
	private String getString() {
		int start = pos;
		char c = s.charAt(pos-1);
		while(pos < s.length() && (s.charAt(pos) != c || s.charAt(pos-1) == '\\')) pos++;
		return newString(start, pos++);
	}
	
	private Object getValue(ObjectWrapper parent, String key) {
		switch(s.charAt(pos)) {
		case '[':  pos++; return getArray(parent, key);
		case '{':  pos++; return getObject(parent, key);
		default:
			return jay.adaptFromJson(parent, key, getRawValue(parent, key));
		}
	}
	
	private Object getRawValue(ObjectWrapper parent, String key) {
		switch(s.charAt(pos)) {
		case '"':  pos++; return getString();
		case '\'': pos++; return getString();
		}
		
		int start = pos;
		boolean num = true;
		boolean dec = false;
		boolean exp = false;
		
		do {
			switch(s.charAt(pos)) {
			case '-':
				if(pos != start) num = false;
				break;
			case '.':
				if(dec) num = false;
				else dec = true;
				break;
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				// number still true
				break;
			case 'e': case 'E':
				if(!exp && pos != start && pos+1 < s.length()) {
					char p = s.charAt(pos-1);
					char n = s.charAt(++pos);
					if(Character.isDigit(p) && (n == '+' || n == '-')) {
						exp = true;
						break;
					}
				}
				num = false;
				break;
			default:
				num = false;
				break;
			}
			if(pos > start) {
				if(Character.isWhitespace(s.charAt(pos-1))) num = false;
			}
			pos++;
		} while(num && ff() && s.charAt(pos) != ',');

		if(ff(',') == ':') {
			pos = start;
			return getObject(parent, key);
		} else {
			String value = newString(start, pos);
			if(value.length() > 0) {
				if(num) {
					if(dec || exp) return Double.valueOf(value);
					else if(type == long.class || type == Long.class) return Long.valueOf(value);
					else return Integer.valueOf(value);
				}
				if("?".equals(value)) return jay.nextArg();
				if("null".equals(value)) return null;
				if("true".equals(value)) return true;
				if("false".equals(value)) return false;
				return value;
			}
			return null;
		}
	}

    private String newString(int start, int end) {
    	StringBuilder sb = new StringBuilder(end-start);
    	for(int i = start; i < end; i++) {
    		char c = s.charAt(i);
    		if(c == '\\') {
    			i++;
    			if(i < end) {
    				c = s.charAt(i);
    				switch(c) {
    				case 'b': case 't': case 'n': case 'f': case 'r':
    				case '"': case '/': case '\'': case '\\': 
    					sb.append(c);
    					break;
    				case 'u':
    					if(i+4 < end) {
    						sb.append((char)Integer.parseInt(s.substring(i, i+4), 16));
    						break;
    					} // else fall through and throw exception
    				default:
    					throw new IllegalArgumentException("illegal escape");
    				}
    			}
    		} else {
    			sb.append(c);
    		}
    	}
    	return sb.toString().trim();
    }

}
