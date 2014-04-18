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

import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ObjectWrapper {
	
	private final Object object;

	public ObjectWrapper(Class<?> type) {
		if(useMap(type)) {
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
			return new ObjectWrapper(Map.class);
		} else {
			try {
				Field field = object.getClass().getDeclaredField(key);
				Class<?> fieldType = field.getType();
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
			return Object.class;
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
				type.isPrimitive() || type == Object.class || type == Object[].class ||
				Map.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type) ||
				String.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type) ||
				Character.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)
				);
	}


    public static abstract class Kreator {

    	public static final String ERR_COULD_NOT_INSTANTIATE_TYPE = "could not instantiate the requested type: %s";
    	public static final String ERR_ILLEGAL_ACCESS = "field should have been set to accessible...";

    	private static final Kreator instance = load();
        private static Kreator load() {
        	Kreator k = loadJVM();
        	if(k == null) loadDalvik(long.class);
        	if(k == null) loadDalvik(int.class);
        	return k;
        }
        
        private static Kreator loadJVM() {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                final Object unsafe = f.get(null);
                final Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
                return new Kreator() {
                    <T> T createNewInstance(Class<T> type) throws InvocationTargetException, IllegalAccessException {
                        return type.cast(allocateInstance.invoke(unsafe, type));
                    }
                };
              } catch (Exception ignored) {
            	  return null;
              }
        }
        
        private static Kreator loadDalvik(final Class<?> idType) {
            try {
                Method getConstructorId = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
                getConstructorId.setAccessible(true);
                final Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, idType);
                final long constructorId = (Long) getConstructorId.invoke(null, Object.class);
                newInstance.setAccessible(true);
                return new Kreator() {
                    <T> T createNewInstance(Class<T> type) throws InvocationTargetException, IllegalAccessException {
                        return type.cast(newInstance.invoke(null, type, idType.cast(constructorId)));
                    }
                };
            } catch(Exception e) {
                return null;
            }
        }
        
        static <T> T newInstance(Class<T> type) {
            try {
                return instance.createNewInstance(type);
            } catch(IllegalAccessException e) {
                throw new IllegalArgumentException(ERR_ILLEGAL_ACCESS, e);
            } catch(InvocationTargetException e) {
            	throw new IllegalArgumentException(String.format(ERR_COULD_NOT_INSTANTIATE_TYPE, type), e);
            }
        }
        
        abstract <T> T createNewInstance(Class<T> type) throws InvocationTargetException, IllegalAccessException;
        
    }

}
