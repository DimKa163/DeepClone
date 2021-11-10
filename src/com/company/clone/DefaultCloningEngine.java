package com.company.clone;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;

public class DefaultCloningEngine implements CloningEngine {
    private static final Map<Class<?>, Collection<Field>> _fields = new HashMap<>();
    private static final Map<Class<?>, Constructor<?>> _ctors = new HashMap<>();
    private final Map<Object, Object> _clonedObj;

    private final ArrayList<String> _primitiveType = new ArrayList<>(Arrays.asList("Integer",
            "Byte", "Float", "Double", "Short", "Character", "Boolean"));

    public DefaultCloningEngine(){
        this._clonedObj = new HashMap<>();
    }
    public Object clone(Object target) throws CloneableException {
        if (target == null)
            return null;
        Class<?> clazz = target.getClass();
        if (isPrimitive(clazz) || clazz.isEnum())
            return clonePrimitive(target);
        if (isString(clazz))
            return String.valueOf(target);
        if (isDate(clazz))
            return cloneDate(target);
        if (clazz.isArray()) {
            return cloneArray(target);
        }
        return cloneClass(target);
    }

    private boolean isPrimitive(Class<?> clazz){
        return _primitiveType.contains(clazz.getSimpleName());
    }

    private boolean isString(Class<?> clazz) {
        return clazz == String.class;
    }

    private boolean isDate(Class<?> clazz) {
        return  clazz == LocalDate.class || clazz == Date.class;
    }

    private Object clonePrimitive(Object target){
        return target;
    }

    private Object cloneArray(Object target) throws CloneableException {
        Class<?> clazz = target.getClass();
        int length = Array.getLength(target);
        Object clone = Array.newInstance(clazz.getComponentType(), length);
        this._clonedObj.put(target, clone);
        for (int i = 0; i < length; i++) {
            Object item = clone(Array.get(target, i));
            Array.set(clone, i, item);
        }
        return clone;
    }

    private Object cloneDate(Object target) {
        if (target instanceof Date date) {
            return new Date(date.getTime());
        } else if (target instanceof LocalDate date) {
            return  LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
        }
        return null;
    }

    private Object cloneClass(Object target) throws CloneableException {
        if (target instanceof Collection<?> collection) {
            return cloneCollection(target, collection);
        } else {
            return cloneObject(target);
        }
    }

    private Object cloneCollection(Object target, Collection<?> targetCollection) throws CloneableException {
        try{
            Class<?> clazz = target.getClass();
            Object clone = createInstance(clazz);
            this._clonedObj.put(target, clone);
            Method mth = clazz.getDeclaredMethod("add", Object.class);
            for(Object item : targetCollection){
                Object clonedValue = this._clonedObj.get(item);
                if (clonedValue != null) {
                    mth.invoke(clone, clonedValue);
                }else{
                    mth.invoke(clone, clone(item));
                }
            }
            return clone;
        }catch (Exception ex){
            throw new CloneableException("Error cloning collection", ex);
        }
    }

    private Object cloneObject(Object target) throws CloneableException {
        try{
            Class<?> clazz = target.getClass();
            Object clone = createInstance(clazz);
            this._clonedObj.put(target, clone);
            if (!_fields.containsKey(clazz))
                _fields.put(clazz, getFieldCollection(clazz));
            Collection<Field> fields = _fields.get(clazz);
            for(Field f : fields){
                f.setAccessible(true);
                Object targetValue = f.get(target);
                Object clonedValue = this._clonedObj.get(targetValue);
                if (clonedValue != null)
                    f.set(clone, clonedValue);
                else{
                    f.set(clone, clone(targetValue));
                }
            }
            return clone;
        }catch (Exception ex){
            throw new CloneableException("Error cloning object", ex);
        }

    }

    private Object createInstance(Class<?> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!_ctors.containsKey(clazz)){
            Constructor<?>[] ctors = clazz.getConstructors();
            if (ctors.length == 0)
                throw new IllegalArgumentException("Open ctor not found");
            Arrays.sort(ctors, new CtorComparer());
            Constructor<?> ctor = ctors[0];
            ctor.setAccessible(true);
            _ctors.put(clazz, ctor);
        }
        Constructor<?> ctor =  _ctors.get(clazz);
        if (ctor.getParameterCount() == 0)
            return ctor.newInstance();
        Class<?>[] ctorParameters = ctor.getParameterTypes();
        Object[] ctorValues = new Object[ctorParameters.length];
        for(int i = 0; i < ctorValues.length; i++) {
            ctorValues[i] = getDefaultValue(ctorParameters[i]);
        }
        return ctor.newInstance(ctorValues);
    }

    private Object getDefaultValue(Class<?> clazz){
        if (clazz == int.class
            || clazz == Long.class
            || clazz == Short.class
            || clazz == Byte.class)
            return 0;
        else if (clazz == Double.class
            || clazz == Float.class)
            return 0.0;
        else if (clazz == Enum.class){
            return clazz.getEnumConstants()[0];
        }
        else if (clazz == Character.class){
            return ' ';
        }
        else
            return null;
    }

    private Collection<Field> getFieldCollection(Class<?> clazz){
        Class<?> currentClazz = clazz;
        ArrayList<Field> fieldList = new ArrayList<>();
        do{
            Field[] fields = currentClazz.getDeclaredFields();
            for(Field f : fields){
                if (Modifier.isStatic(f.getModifiers())
                        || Modifier.isTransient(f.getModifiers()))
                    continue;
                fieldList.add(f);
            }
            currentClazz = currentClazz.getSuperclass();
        }while (currentClazz!= null);
        return fieldList;
    }
}

class CtorComparer implements Comparator<Constructor<?>>{

    @Override
    public int compare(Constructor<?> o1, Constructor<?> o2) {
        return o1.getParameterCount() - o2.getParameterCount();
    }
}
