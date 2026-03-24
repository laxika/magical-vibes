package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry that maps (EffectSlot, CardEffect class) pairs to trigger collector handlers.
 * <p>
 * Discovered automatically at startup by {@code TriggerRegistryConfig}.
 */
public class TriggerCollectorRegistry {

    private record TriggerKey(EffectSlot slot, Class<? extends CardEffect> effectClass) {}

    private final Map<TriggerKey, TriggerCollectorHandler> handlers = new LinkedHashMap<>();

    public void register(EffectSlot slot, Class<? extends CardEffect> effectClass, TriggerCollectorHandler handler) {
        handlers.put(new TriggerKey(slot, effectClass), handler);
    }

    /**
     * Dispatches a single effect to its registered handler.
     * <p>
     * Resolution order:
     * <ol>
     *   <li>Exact match on the raw effect's class</li>
     *   <li>If the raw effect is a {@link MayEffect}, try the wrapped effect's class</li>
     *   <li>Fallback handler registered for {@code CardEffect.class} (default handler)</li>
     * </ol>
     *
     * @return {@code true} if a handler was found and the trigger fired
     */
    public boolean dispatch(TriggerMatchContext match, EffectSlot slot, CardEffect effect, TriggerContext context) {
        // 1. Exact match
        TriggerCollectorHandler handler = handlers.get(new TriggerKey(slot, effect.getClass()));
        if (handler != null) {
            return handler.handle(match, effect, context);
        }

        // 2. MayEffect unwrap
        if (effect instanceof MayEffect may) {
            CardEffect inner = may.wrapped();
            handler = handlers.get(new TriggerKey(slot, inner.getClass()));
            if (handler != null) {
                return handler.handle(match, inner, context);
            }
        }

        // 3. MayPayManaEffect unwrap
        if (effect instanceof MayPayManaEffect mayPay) {
            CardEffect inner = mayPay.wrapped();
            handler = handlers.get(new TriggerKey(slot, inner.getClass()));
            if (handler != null) {
                return handler.handle(match, inner, context);
            }
        }

        // 4. Default handler (CardEffect.class)
        handler = handlers.get(new TriggerKey(slot, CardEffect.class));
        if (handler != null) {
            return handler.handle(match, effect, context);
        }

        return false;
    }

    public int size() {
        return handlers.size();
    }

    /**
     * Scans a bean for {@link CollectsTrigger}-annotated methods and registers them.
     * Used by non-Spring contexts (tests, AI simulator) that cannot use annotation scanning.
     */
    @SuppressWarnings("unchecked")
    public static void scanBean(Object bean, TriggerCollectorRegistry registry) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            CollectsTrigger single = method.getAnnotation(CollectsTrigger.class);
            if (single != null) {
                registerMethod(bean, method, single, registry);
            }
            CollectsTriggers container = method.getAnnotation(CollectsTriggers.class);
            if (container != null) {
                for (CollectsTrigger ct : container.value()) {
                    registerMethod(bean, method, ct, registry);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerMethod(Object bean, Method method, CollectsTrigger annotation,
                                        TriggerCollectorRegistry registry) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();

        if (params.length != 3
                || params[0] != TriggerMatchContext.class
                || !CardEffect.class.isAssignableFrom(params[1])
                || !TriggerContext.class.isAssignableFrom(params[2])) {
            return;
        }

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
            Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[1];

            registry.register(annotation.slot(), annotation.value(), (match, innerEffect, context) -> {
                try {
                    return (boolean) handle.invoke(match, effectParam.cast(innerEffect), context);
                } catch (RuntimeException re) {
                    throw re;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            });
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
