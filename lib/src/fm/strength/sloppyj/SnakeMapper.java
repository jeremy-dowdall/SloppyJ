package fm.strength.sloppyj;

import static java.lang.Character.isDigit;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

public class SnakeMapper implements Mapper {
	
	@Override
	public String fromJson(String key) {
		return fromSnake(key);
	}
	
	@Override
	public String toJson(String key) {
		return toSnake(key);
	}
	
	/**
	 * convert a string from snake_case to Java style variable names
	 * (like CamelCase except the first character is always lower case)
	 * <p>
	 * key -> key, my_key -> myKey,
	 * KEY -> key, myKey -> myKey, my_key_100 -> myKey100
	 * </p>
	 * <p> note that leading underscores will also be removed:
	 * _key -> key)
	 * </p>
	 * @param s the string to be converted
	 * @return a new string converted to CamelCase; null if the input string is null, empty, or contains only underscores
	 */
	public static String fromSnake(String s) {
		if(s == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder(s.length());
		char p = 0;
		
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c != '_') {
				if(i == 0 || isUpperCase(p)) sb.append(toLowerCase(c));
				else if(p == '_')            sb.append(toUpperCase(c));
				else                         sb.append(c);
			}
			p = c;
		}
		
		return (sb.length() > 0) ? sb.toString() : null;
	}

	/**
	 * convert a CamelCase string to snake_case:
	 * <p>
	 * key -> key, MyKey -> my_key,
	 * KEY -> key, myKey -> my_key, myKey100 -> my_key_100
	 * </p>
	 * @param s the string to be converted
	 * @return a new string converted to snake_case; null if the input string is null or empty
	 */
	public static String toSnake(String s) {
		if(s == null) {
			return null;
		}

		char p = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(i > 0 && (isUpperCase(c) || (isDigit(c) && !isDigit(p)))) {
				sb.append('_');
			}
			sb.append(toLowerCase(c));
			p = c;
		}

		return (sb.length() > 0) ? sb.toString() : null;
	}
	
}