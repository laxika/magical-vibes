package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that maps (EffectSlot, {@link ZoneChangeWatch}, {@link SpellAbilityWatch}, {@link CombatWatch},
 * {@link DamageWatch}, {@link CounterWatch}, {@link LibraryWatch}, {@link TapWatch}, {@link LifeWatch},
 * {@link SagaWatch}, {@link ActionWatch} or {@link MiscWatch}, CardEffect class) pairs to trigger collector handlers.
 * <p>
 * Discovered automatically at startup by {@link com.github.laxika.magicalvibes.service.GameEngineConfig}.
 */
public class TriggerCollectorRegistry {

    private record TriggerKey(Object slot, Class<? extends CardEffect> effectClass) {}

    private final Map<TriggerKey, TriggerCollectorHandler> handlers = new LinkedHashMap<>();

    /**
     * Registers {@code handler} for {@code effectClass} on {@code slot}. A key has one handler: registering a second
     * one for the same slot and class throws, so two collectors can't silently replace each other's handler.
     */
    public void register(Object slot, Class<? extends CardEffect> effectClass, TriggerCollectorHandler handler) {
        TriggerKey key = new TriggerKey(slot, effectClass);
        if (handlers.putIfAbsent(key, handler) != null) {
            throw new IllegalStateException("A trigger collector handler is already registered for "
                    + effectClass.getSimpleName() + " on " + slot);
        }
    }

    /**
     * Dispatches the ability {@code match} carries, its effects together, to {@code slot}'s handler, which puts it on
     * the stack as one object (CR 603.3).
     * <p>
     * Resolution order:
     * <ol>
     *   <li>A handler registered for the exact class of one of the ability's effects, the first in order that has
     *       one (a ROUTE: it routes the whole ability)</li>
     *   <li>The default handler registered for {@code CardEffect.class}, which routes any ability of the watch</li>
     * </ol>
     * A "you may" ({@link com.github.laxika.magicalvibes.model.effect.MayEffect} and the may-pay effects) is the
     * effect itself, never unwrapped: an optional triggered ability goes on the stack with its option, chosen as it
     * resolves (CR 603.5).
     *
     * @return {@code true} if a handler was found and the trigger fired
     */
    public boolean dispatch(TriggerMatchContext match, Object slot, TriggerContext context) {
        for (CardEffect effect : match.effects()) {
            TriggerCollectorHandler handler = handlers.get(new TriggerKey(slot, effect.getClass()));
            if (handler != null) {
                return route(handler, match, context);
            }
        }
        TriggerCollectorHandler handler = handlers.get(new TriggerKey(slot, CardEffect.class));
        return handler != null && route(handler, match, context);
    }

    /**
     * Has {@code handler} route {@code match}'s ability as one routing of the game's trigger record hand-off
     * ({@code GameData.beginTriggerRouting}): the handler hands off the entry it builds for an ability that chooses
     * nothing before it queues or asks a choice, and what it leaves in hand is dropped.
     */
    private static boolean route(TriggerCollectorHandler handler, TriggerMatchContext match, TriggerContext context) {
        GameData gameData = match.gameData();
        GameData.TriggerRouting previous = gameData.beginTriggerRouting();
        try {
            return handler.handle(match, context);
        } finally {
            gameData.endTriggerRouting(previous);
        }
    }

    /**
     * Whether {@link #dispatch} finds a handler for an ability of {@code effects} on {@code slot}: one registered for
     * the class of one of them, or the slot's default handler.
     */
    public boolean handles(Object slot, List<CardEffect> effects) {
        return handlers.containsKey(new TriggerKey(slot, CardEffect.class))
                || effects.stream().anyMatch(effect -> handlers.containsKey(new TriggerKey(slot, effect.getClass())));
    }

    public int size() {
        return handlers.size();
    }

    /**
     * Scans a bean for {@link CollectsTrigger}-annotated methods and registers them.
     * Used by non-Spring contexts (tests, AI simulator) that cannot use annotation scanning.
     */
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

            CollectsLibraryTrigger singleLibrary = method.getAnnotation(CollectsLibraryTrigger.class);
            if (singleLibrary != null) {
                registerMethod(bean, method, singleLibrary.watch(), singleLibrary.value(), registry);
            }
            CollectsLibraryTriggers libraryContainer = method.getAnnotation(CollectsLibraryTriggers.class);
            if (libraryContainer != null) {
                for (CollectsLibraryTrigger clt : libraryContainer.value()) {
                    registerMethod(bean, method, clt.watch(), clt.value(), registry);
                }
            }

            CollectsTapTrigger singleTap = method.getAnnotation(CollectsTapTrigger.class);
            if (singleTap != null) {
                registerMethod(bean, method, singleTap.watch(), singleTap.value(), registry);
            }
            CollectsTapTriggers tapContainer = method.getAnnotation(CollectsTapTriggers.class);
            if (tapContainer != null) {
                for (CollectsTapTrigger clt : tapContainer.value()) {
                    registerMethod(bean, method, clt.watch(), clt.value(), registry);
                }
            }

            CollectsLifeTrigger singleLife = method.getAnnotation(CollectsLifeTrigger.class);
            if (singleLife != null) {
                registerMethod(bean, method, singleLife.watch(), singleLife.value(), registry);
            }
            CollectsLifeTriggers lifeContainer = method.getAnnotation(CollectsLifeTriggers.class);
            if (lifeContainer != null) {
                for (CollectsLifeTrigger clt : lifeContainer.value()) {
                    registerMethod(bean, method, clt.watch(), clt.value(), registry);
                }
            }

            CollectsSagaTrigger singleSaga = method.getAnnotation(CollectsSagaTrigger.class);
            if (singleSaga != null) {
                registerMethod(bean, method, singleSaga.watch(), singleSaga.value(), registry);
            }
            CollectsSagaTriggers sagaContainer = method.getAnnotation(CollectsSagaTriggers.class);
            if (sagaContainer != null) {
                for (CollectsSagaTrigger cst : sagaContainer.value()) {
                    registerMethod(bean, method, cst.watch(), cst.value(), registry);
                }
            }

            CollectsActionTrigger singleAction = method.getAnnotation(CollectsActionTrigger.class);
            if (singleAction != null) {
                registerMethod(bean, method, singleAction.watch(), singleAction.value(), registry);
            }
            CollectsActionTriggers actionContainer = method.getAnnotation(CollectsActionTriggers.class);
            if (actionContainer != null) {
                for (CollectsActionTrigger cst : actionContainer.value()) {
                    registerMethod(bean, method, cst.watch(), cst.value(), registry);
                }
            }

            CollectsMiscTrigger singleMisc = method.getAnnotation(CollectsMiscTrigger.class);
            if (singleMisc != null) {
                registerMethod(bean, method, singleMisc.watch(), singleMisc.value(), registry);
            }
            CollectsMiscTriggers miscContainer = method.getAnnotation(CollectsMiscTriggers.class);
            if (miscContainer != null) {
                for (CollectsMiscTrigger cst : miscContainer.value()) {
                    registerMethod(bean, method, cst.watch(), cst.value(), registry);
                }
            }

        }
    }

    private static void registerMethod(Object bean, Method method, CollectsTrigger annotation,
                                        TriggerCollectorRegistry registry) {
        registerMethod(bean, method, annotation.slot(), annotation.value(), registry);
    }

    private static void registerMethod(Object bean, Method method, Object key, Class<? extends CardEffect> effectClass,
                                        TriggerCollectorRegistry registry) {
        registry.register(key, effectClass, handlerOf(bean, method, effectClass));
    }

    /**
     * The handler {@code method} of {@code bean} is, registered for {@code effectClass}. A watch's default
     * ({@code CardEffect.class}) takes {@code (TriggerMatchContext, TriggerContext)}: it routes whatever ability of the
     * watch triggered, by its effects. A handler for an effect class takes {@code (TriggerMatchContext, <that class>,
     * TriggerContext)}: it routes an ability that has an effect of that class, which it is given. Both return
     * {@code boolean}; any other method throws.
     */
    @SuppressWarnings("unchecked")
    public static TriggerCollectorHandler handlerOf(Object bean, Method method,
                                                    Class<? extends CardEffect> effectClass) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();
        boolean isDefault = effectClass == CardEffect.class;
        boolean valid = method.getReturnType() == boolean.class && (isDefault
                ? params.length == 2 && params[0] == TriggerMatchContext.class
                        && TriggerContext.class.isAssignableFrom(params[1])
                : params.length == 3 && params[0] == TriggerMatchContext.class
                        && params[1].isAssignableFrom(effectClass)
                        && TriggerContext.class.isAssignableFrom(params[2]));
        if (!valid) {
            throw new IllegalStateException("Trigger collector method " + method.getDeclaringClass().getSimpleName()
                    + "." + method.getName() + " must return boolean and take " + (isDefault
                    ? "(TriggerMatchContext, TriggerContext): a default routes the whole ability"
                    : "(TriggerMatchContext, " + effectClass.getSimpleName() + ", TriggerContext)"));
        }
        MethodHandle handle;
        try {
            handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access trigger collector method " + method.getName(), e);
        }
        if (isDefault) {
            return (match, context) -> invoke(handle, method, match, context);
        }
        return (match, context) -> invoke(handle, method, match, keyedEffect(match, effectClass), context);
    }

    /** The effect of {@code match}'s ability whose exact class is {@code effectClass}. */
    private static CardEffect keyedEffect(TriggerMatchContext match, Class<? extends CardEffect> effectClass) {
        for (CardEffect effect : match.effects()) {
            if (effect.getClass() == effectClass) {
                return effect;
            }
        }
        throw new IllegalStateException("The ability has no " + effectClass.getSimpleName() + ": " + match.effects());
    }

    private static boolean invoke(MethodHandle handle, Method method, Object... arguments) {
        try {
            return (boolean) handle.invokeWithArguments(arguments);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException("Error invoking trigger collector "
                    + method.getDeclaringClass().getSimpleName() + "." + method.getName(), t);
        }
    }
}
