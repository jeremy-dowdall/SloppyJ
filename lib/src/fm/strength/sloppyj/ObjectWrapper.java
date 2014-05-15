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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ObjectWrapper {
	
	private final Class<?> type;
	private final Object object;

	public ObjectWrapper(Class<?> type) {
		this(type, type);
	}
	public ObjectWrapper(Class<?> type, Class<?> objectType) {
		this.type = type;
		if(useMap(objectType)) {
			this.object = new LinkedHashMap<>();
		}
		else {
			this.object = Kreator.newInstance(type);
		}
	}
	
	public Object get() {
		return object;
	}
	
	public ObjectWrapper get(String key) {
		if(object instanceof Map) {
			return new ObjectWrapper(type);
		} else {
			try {
				Field field = type.getDeclaredField(key);
				Class<?> fieldType = field.getType();
				if(Map.class.isAssignableFrom(fieldType)) {
					fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
					return new ObjectWrapper(fieldType, Map.class);
				}
				if(List.class.isAssignableFrom(fieldType)) {
	                fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
				}
				return new ObjectWrapper(fieldType);
			} catch(Exception e) {
				// field exists in JSON, but not in Object - return null and JsonParser will skip it
				return null;
			}
		}
	}

	public Class<?> getType(String key) {
		if(object instanceof Map) {
			return type;
		} else {
			try {
				Field field = object.getClass().getDeclaredField(key);
				Class<?> fieldType = field.getType();
				if(List.class.isAssignableFrom(fieldType)) {
	                fieldType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
				}
				return fieldType;
			} catch(Exception e) {
				// field exists in JSON, but not in Object - return null and Jay will skip it
				return null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void set(String key, Object value) {
		if(object instanceof Map) {
			((Map<String, Object>) object).put(key, value);
		} else {
			try {
				Field field = object.getClass().getDeclaredField(key);
				field.setAccessible(true);
				field.set(object, value);
			} catch(Exception e) {
				// field exists in JSON, but not in Object - skip it
			}
		}
	}
	
	
	private static boolean useMap(Class<?> type) {
		return (
				type == null ||
				type.isPrimitive() || type == Object.class || type == Object[].class ||
				Map.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type) ||
				String.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type) ||
				Character.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)
				);
	}

}
