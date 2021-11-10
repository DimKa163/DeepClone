package com.company.clone;

public class CloneUtility {
    @SuppressWarnings("unchecked")
    public static <TValue> TValue cloneTypedValue(TValue value) throws ClassCastException, CloneableException {
        return (TValue) cloneValue(value);
    }
    @SuppressWarnings("unchecked")
    public static <TValue> TValue cloneTypedValue(TValue value, CloningEngine engine) throws ClassCastException, CloneableException {
        return (TValue) cloneValue(value, engine);
    }
    public static Object cloneValue(Object target) throws CloneableException {
        CloningEngine engine = new DefaultCloningEngine();
        return cloneValue(target, engine);
    }

    public  static  Object cloneValue(Object target, CloningEngine engine) throws CloneableException {
        return engine.clone(target);
    }
}
