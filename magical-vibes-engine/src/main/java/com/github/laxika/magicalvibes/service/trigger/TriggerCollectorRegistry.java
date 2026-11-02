package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry that maps (EffectSlot, {@link ZoneChangeWatch}, {@link SpellAbilityWatch}, {@link CombatWatch},
 * {@link DamageWatch} or {@link CounterWatch}, CardEffect class) pairs to trigger collector handlers.
 * <p>
 * Discovered automatically at startup by {@link com.github.laxika.magicalvibes.service.GameEngineConfig}.
 */
public class TriggerCollectorRegistry {

    private record TriggerKey(Object slot, Class<? extends CardEffect> effectClass) {}

    private final Map<TriggerKey, TriggerCollectorHandler> handlers = new LinkedHashMap<>();
    private final Map<Class<? extends CardEffect>, EmblemTriggerCollectorHandler> emblemHandlers = new LinkedHashMap<>();

    public void register(Object slot, Class<? extends CardEffect> effectClass, TriggerCollectorHandler handler) {
        handlers.put(new TriggerKey(slot, effectClass), handler);
    }

    public void registerEmblem(Class<? extends CardEffect> effectClass, EmblemTriggerCollectorHandler handler) {
        emblemHandlers.put(effectClass, handler);
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
    public boolean dispatch(TriggerMatchContext match, Object slot, CardEffect effect, TriggerContext context) {
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

        // 4. MayPayTapPermanentsEffect unwrap
        if (effect instanceof MayPayTapPermanentsEffect mayPayTap) {
            CardEffect inner = mayPayTap.wrapped();
            handler = handlers.get(new TriggerKey(slot, inner.getClass()));
            if (handler != null) {
                return handler.handle(match, inner, context);
            }
        }

        // 5. Default handler (CardEffect.class)
        handler = handlers.get(new TriggerKey(slot, CardEffect.class));
        if (handler != null) {
            return handler.handle(match, effect, context);
        }

        return false;
    }

    /** Dispatches an effect stored on an emblem to its class-keyed handler. */
    public boolean dispatchEmblem(EmblemTriggerMatchContext match, CardEffect effect, TriggerContext context) {
        EmblemTriggerCollectorHandler handler = emblemHandlers.get(effect.getClass());
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

            CollectsWatchTrigger singleWatch = method.getAnnotation(CollectsWatchTrigger.class);
            if (singleWatch != null) {
                registerMethod(bean, method, singleWatch.watch(), singleWatch.value(), registry);
            }
            CollectsWatchTriggers watchContainer = method.getAnnotation(CollectsWatchTriggers.class);
            if (watchContainer != null) {
                for (CollectsWatchTrigger cwt : watchContainer.value()) {
                    registerMethod(bean, method, cwt.watch(), cwt.value(), registry);
                }
            }

            CollectsSpellAbilityTrigger singleSpell = method.getAnnotation(CollectsSpellAbilityTrigger.class);
            if (singleSpell != null) {
                registerMethod(bean, method, singleSpell.watch(), singleSpell.value(), registry);
            }
            CollectsSpellAbilityTriggers spellContainer = method.getAnnotation(CollectsSpellAbilityTriggers.class);
            if (spellContainer != null) {
                for (CollectsSpellAbilityTrigger cst : spellContainer.value()) {
                    registerMethod(bean, method, cst.watch(), cst.value(), registry);
                }
            }

            CollectsCombatTrigger singleCombat = method.getAnnotation(CollectsCombatTrigger.class);
            if (singleCombat != null) {
                registerMethod(bean, method, singleCombat.watch(), singleCombat.value(), registry);
            }
            CollectsCombatTriggers combatContainer = method.getAnnotation(CollectsCombatTriggers.class);
            if (combatContainer != null) {
                for (CollectsCombatTrigger cct : combatContainer.value()) {
                    registerMethod(bean, method, cct.watch(), cct.value(), registry);
                }
            }

            CollectsDamageTrigger singleDamage = method.getAnnotation(CollectsDamageTrigger.class);
            if (singleDamage != null) {
                registerMethod(bean, method, singleDamage.watch(), singleDamage.value(), registry);
            }
            CollectsDamageTriggers damageContainer = method.getAnnotation(CollectsDamageTriggers.class);
            if (damageContainer != null) {
                for (CollectsDamageTrigger cdt : damageContainer.value()) {
                    registerMethod(bean, method, cdt.watch(), cdt.value(), registry);
                }
            }

            CollectsCounterTrigger singleCounter = method.getAnnotation(CollectsCounterTrigger.class);
            if (singleCounter != null) {
                registerMethod(bean, method, singleCounter.watch(), singleCounter.value(), registry);
            }
            CollectsCounterTriggers counterContainer = method.getAnnotation(CollectsCounterTriggers.class);
            if (counterContainer != null) {
                for (CollectsCounterTrigger cct : counterContainer.value()) {
                    registerMethod(bean, method, cct.watch(), cct.value(), registry);
                }
            }

            CollectsEmblemTrigger singleEmblem = method.getAnnotation(CollectsEmblemTrigger.class);
            if (singleEmblem != null) {
                registerEmblemMethod(bean, method, singleEmblem, registry);
            }
            CollectsEmblemTriggers emblemContainer = method.getAnnotation(CollectsEmblemTriggers.class);
            if (emblemContainer != null) {
                for (CollectsEmblemTrigger cet : emblemContainer.value()) {
                    registerEmblemMethod(bean, method, cet, registry);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerMethod(Object bean, Method method, CollectsTrigger annotation,
                                        TriggerCollectorRegistry registry) {
        registerMethod(bean, method, annotation.slot(), annotation.value(), registry);
    }

    @SuppressWarnings("unchecked")
    private static void registerMethod(Object bean, Method method, Object key, Class<? extends CardEffect> effectClass,
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

            registry.register(key, effectClass, (match, innerEffect, context) -> {
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

    @SuppressWarnings("unchecked")
    private static void registerEmblemMethod(Object bean, Method method, CollectsEmblemTrigger annotation,
                                              TriggerCollectorRegistry registry) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();

        if (params.length != 3
                || params[0] != EmblemTriggerMatchContext.class
                || !CardEffect.class.isAssignableFrom(params[1])
                || !TriggerContext.class.isAssignableFrom(params[2])) {
            return;
        }

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
            Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[1];

            registry.emblemHandlers.put(annotation.value(), (match, innerEffect, context) -> {
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
