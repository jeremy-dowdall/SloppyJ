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
    private final boolean array;
    private final String s;
    private int pos;
    
	JsonParser(Jay jay, boolean array) {
		this.jay = jay;
		this.array = array;
		this.s = jay.data.toString();
	}
	
	
	Object toJava() {
		return ff() ? getValue(null, null) : null;
	}
	
	
	private boolean go() {
		if(pos < s.length()) {
			switch(s.charAt(pos)) { case ']': case '}': case ':': case ',': return false; }
			return true;
		}
		return false;
	}

	private boolean ff() {
		while(go()) {
			if(!Character.isWhitespace(s.charAt(pos))) return true;
			pos++;
		}
		return false;
	}
	
	private boolean ff(char f) {
		boolean go;
		while((go = go()) && s.charAt(pos) != f) pos++;
		return go || (pos < s.length() && s.charAt(pos) == f);
	}

	private Object getArray(ObjectWrapper parent, String key) {
		ArrayList<Object> list = new ArrayList<Object>();
		while(ff()) {
			list.add(getValue(parent, key));
			if(ff(',')) pos++;
			else break;
		}
		pos++;
		list.trimToSize();
		return array ? list.toArray() : list;
	}

	private Object getObject(ObjectWrapper parent, String parentKey) {
		ObjectWrapper wrapper = (parent != null) ? parent.get(parentKey) : jay.getWrapper();
		while(ff()) {
			String key = getKey();
			if(ff(':')) {
				pos++;
				if(wrapper != null && jay.include(key)) {
					wrapper.set(key, getValue(wrapper, key));
				}
				if(ff(',')) pos++;
				else break;
			}
		}
		pos++;
		return (wrapper != null) ? wrapper.get() : null;
	}
	
	private String getKey() {
		String key = null;
		switch(s.charAt(pos)) {
		case '"':  pos++; key = getString(); break;
		case '\'': pos++; key = getString(); break;
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
		boolean number = true;
		boolean decimal = false;
		
		do {
			switch(s.charAt(pos)) {
			case '-':
				if(pos != start) number = false;
				break;
			case '.':
				if(decimal) number = false;
				else decimal = true;
				break;
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				// number still true
				break;
			default:
				// TODO support exponent format
				number = false;
				break;
			}
			if(pos > start) {
				if(Character.isWhitespace(s.charAt(pos-1))) number = false;
			}
			pos++;
		} while(number && ff() && s.charAt(pos) != ',');

		ff(',');
		
		if(pos < s.length() && s.charAt(pos) == ':') {
			pos = start;
			return getObject(parent, key);
		} else {
			String value = newString(start, pos);
			if(value.length() > 0) {
				if(number) {
					if(decimal)  return Double.valueOf(value);
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
    	// TODO handle special characters
        return new String(s.substring(start, end).trim());
    }

}
