package com.github.laxika.magicalvibes.config;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.service.effect.HandlesStaticEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

@Slf4j
@Configuration
public class EffectRegistryConfig implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final StaticEffectHandlerRegistry staticEffectHandlerRegistry;
    private final TargetValidatorRegistry targetValidatorRegistry;

    public EffectRegistryConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.effectHandlerRegistry = new EffectHandlerRegistry();
        this.staticEffectHandlerRegistry = new StaticEffectHandlerRegistry();
        this.targetValidatorRegistry = new TargetValidatorRegistry();
    }

    @Bean
    public EffectHandlerRegistry effectHandlerRegistry() {
        return effectHandlerRegistry;
    }

    @Bean
    public StaticEffectHandlerRegistry staticEffectHandlerRegistry() {
        return staticEffectHandlerRegistry;
    }

    @Bean
    public TargetValidatorRegistry targetValidatorRegistry() {
        return targetValidatorRegistry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        int effectCount = 0;
        int staticCount = 0;
        int validatorCount = 0;

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();

            for (Method method : beanClass.getDeclaredMethods()) {
                HandlesEffect handlesEffect = method.getAnnotation(HandlesEffect.class);
                if (handlesEffect != null) {
                    registerEffectHandler(bean, method, handlesEffect.value());
                    effectCount++;
                }

                HandlesStaticEffect handlesStatic = method.getAnnotation(HandlesStaticEffect.class);
                if (handlesStatic != null) {
                    registerStaticEffectHandler(bean, method, handlesStatic.value(), handlesStatic.selfOnly());
                    staticCount++;
                }

                ValidatesTarget validatesTarget = method.getAnnotation(ValidatesTarget.class);
                if (validatesTarget != null) {
                    registerTargetValidator(bean, method, validatesTarget.value());
                    validatorCount++;
                }
            }
        }

        log.info("Effect auto-registration complete: {} runtime handlers, {} static handlers, {} target validators",
                effectCount, staticCount, validatorCount);
    }

    @SuppressWarnings("unchecked")
    private void registerEffectHandler(Object bean, Method method, Class<? extends CardEffect> effectClass) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);

            if (params.length == 3
                    && params[0] == GameData.class
                    && params[1] == StackEntry.class
                    && CardEffect.class.isAssignableFrom(params[2])) {
                // Pattern B: (GameData, StackEntry, ConcreteEffect)
                Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[2];
                effectHandlerRegistry.register(effectClass, (gd, entry, effect) -> {
                    try {
                        handle.invoke(gd, entry, effectParam.cast(effect));
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            } else if (params.length == 2
                    && params[0] == GameData.class
                    && params[1] == StackEntry.class) {
                // Pattern A: (GameData, StackEntry)
                effectHandlerRegistry.register(effectClass, (gd, entry, effect) -> {
                    try {
                        handle.invoke(gd, entry);
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            } else if (params.length == 1
                    && params[0] == GameData.class) {
                // Pattern C: (GameData)
                effectHandlerRegistry.register(effectClass, (gd, entry, effect) -> {
                    try {
                        handle.invoke(gd);
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            } else {
                throw new IllegalStateException(
                        "@HandlesEffect method " + method.getDeclaringClass().getSimpleName() + "." + method.getName()
                                + " must have signature (GameData), (GameData, StackEntry), or (GameData, StackEntry, <? extends CardEffect>)");
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access @HandlesEffect method " + method.getName(), e);
        }
    }

    private void registerStaticEffectHandler(Object bean, Method method, Class<? extends CardEffect> effectClass, boolean selfOnly) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();

        if (params.length != 3
                || params[0] != StaticEffectContext.class
                || params[1] != CardEffect.class
                || params[2] != StaticBonusAccumulator.class) {
            throw new IllegalStateException(
                    "@HandlesStaticEffect method " + method.getDeclaringClass().getSimpleName() + "." + method.getName()
                            + " must have signature (StaticEffectContext, CardEffect, StaticBonusAccumulator)");
        }

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);

            if (selfOnly) {
                staticEffectHandlerRegistry.registerSelfHandler(effectClass, (context, effect, accumulator) -> {
                    try {
                        handle.invoke(context, effect, accumulator);
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            } else {
                staticEffectHandlerRegistry.register(effectClass, (context, effect, accumulator) -> {
                    try {
                        handle.invoke(context, effect, accumulator);
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access @HandlesStaticEffect method " + method.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerTargetValidator(Object bean, Method method, Class<? extends CardEffect> effectClass) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);

            if (params.length == 2
                    && params[0] == TargetValidationContext.class
                    && CardEffect.class.isAssignableFrom(params[1])) {
                // Pattern B: (TargetValidationContext, ConcreteEffectType)
                Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[1];
                targetValidatorRegistry.register(effectClass, (ctx, effect) -> {
                    try {
                        handle.invoke(ctx, effectParam.cast(effect));
                    } catch (Throwable t) {
                        throw wrapException(t, method);
                    }
                });
            } else if (params.length == 1
                    && params[0] == TargetValidationContext.class) {
                // Pattern A: (TargetValidationContext)
                targetValidatorRegistry.register(effectClass, (ctx, effect) -> {
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
