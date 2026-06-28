package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class TargetValidatorRegistry {

    private final Map<Class<? extends CardEffect>, TargetValidator> validators = new LinkedHashMap<>();

    public void register(Class<? extends CardEffect> effectType, TargetValidator validator) {
        validators.put(effectType, validator);
    }

    public TargetValidator getValidator(CardEffect effect) {
        return validators.get(effect.getClass());
    }

    /**
     * Scans a bean for {@link ValidatesTarget}-annotated methods and registers them.
     * Used by non-Spring contexts (tests, AI simulator) that cannot use annotation scanning.
     *
     * @return the number of validators registered from this bean
     */
    public static int scanBean(Object bean, TargetValidatorRegistry registry) {
        int count = 0;
        for (Method method : bean.getClass().getDeclaredMethods()) {
            ValidatesTarget validatesTarget = method.getAnnotation(ValidatesTarget.class);
            if (validatesTarget != null) {
                registerMethod(bean, method, validatesTarget.value(), registry);
                count++;
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static void registerMethod(Object bean, Method method, Class<? extends CardEffect> effectClass,
                                       TargetValidatorRegistry registry) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);

            if (params.length == 2
                    && params[0] == TargetValidationContext.class
                    && CardEffect.class.isAssignableFrom(params[1])) {
                Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[1];
                registry.register(effectClass, (ctx, effect) -> {
                    try {
                        handle.invoke(ctx, effectParam.cast(effect));
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            } else if (params.length == 1
                    && params[0] == TargetValidationContext.class) {
                registry.register(effectClass, (ctx, effect) -> {
                    try {
                        handle.invoke(ctx);
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            } else {
                throw new IllegalStateException(
                        "@ValidatesTarget method " + method.getDeclaringClass().getSimpleName() + "." + method.getName()
                                + " must have signature (TargetValidationContext) or (TargetValidationContext, <? extends CardEffect>)");
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access @ValidatesTarget method " + method.getName(), e);
        }
    }

    private static RuntimeException wrapException(Throwable t, Method method) {
        if (t instanceof RuntimeException re) {
            throw re;
        }
        throw new RuntimeException("Error invoking handler " + method.getDeclaringClass().getSimpleName() + "." + method.getName(), t);
    }
}
