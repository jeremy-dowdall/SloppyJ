package fm.strength.sloppyj;

import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Kreator {

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