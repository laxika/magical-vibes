package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.NetworkingConfig;
import com.github.laxika.magicalvibes.scryfall.ScryfallConfig;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.NormalEffectHandlerBean;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectHandlerBean;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.CollectsTrigger;
import com.github.laxika.magicalvibes.service.trigger.CollectsTriggers;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectorRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.TriggerMatchContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Single Spring configuration for the game engine object graph: services, support beans,
 * effect handlers, target validators, and trigger collectors. Used by production, card tests,
 * and headless AI simulation — with different {@code SessionManager} / {@code GameRegistry}
 * bindings supplied by each outer context.
 *
 * <p>Owns only the engine's own {@code service} package and composes the lower-level module
 * wiring ({@link NetworkingConfig}, {@link ScryfallConfig}) via {@code @Import}. This keeps each
 * module responsible for its own beans instead of having downstream modules reach in with their
 * own component scans.
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.github.laxika.magicalvibes.service")
@Import({NetworkingConfig.class, ScryfallConfig.class})
public class GameEngineConfig implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final StaticEffectHandlerRegistry staticEffectHandlerRegistry;
    private final TargetValidatorRegistry targetValidatorRegistry;
    private final TriggerCollectorRegistry triggerCollectorRegistry;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public GameEngineConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.effectHandlerRegistry = new EffectHandlerRegistry();
        this.staticEffectHandlerRegistry = new StaticEffectHandlerRegistry();
        this.targetValidatorRegistry = new TargetValidatorRegistry();
        this.triggerCollectorRegistry = new TriggerCollectorRegistry();
        this.interactionHandlerRegistry = new InteractionHandlerRegistry();
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

    @Bean
    public TriggerCollectorRegistry triggerCollectorRegistry() {
        return triggerCollectorRegistry;
    }

    @Bean
    public InteractionHandlerRegistry interactionHandlerRegistry() {
        return interactionHandlerRegistry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<StaticEffectHandlerBean> staticEffectHandlerBeans =
                new ArrayList<>(applicationContext.getBeansOfType(StaticEffectHandlerBean.class).values());
        List<NormalEffectHandlerBean> normalEffectHandlerBeans =
                new ArrayList<>(applicationContext.getBeansOfType(NormalEffectHandlerBean.class).values());

        int validatorCount = 0;
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            validatorCount += TargetValidatorRegistry.scanBean(bean, targetValidatorRegistry);
        }

        for (StaticEffectHandlerBean bean : staticEffectHandlerBeans) {
            if (bean.selfOnly()) {
                staticEffectHandlerRegistry.registerSelfHandler(bean.handledEffect(), bean);
            } else {
                staticEffectHandlerRegistry.register(bean.handledEffect(), bean);
            }
        }

        for (NormalEffectHandlerBean bean : normalEffectHandlerBeans) {
            effectHandlerRegistry.register(bean.handledEffect(), bean);
        }

        for (InteractionHandler<?> bean : applicationContext.getBeansOfType(InteractionHandler.class).values()) {
            interactionHandlerRegistry.register(bean);
        }

        int triggerCount = registerTriggerCollectors();

        log.info("Effect auto-registration complete: {} normal handlers, {} static handlers, {} target validators",
                normalEffectHandlerBeans.size(), staticEffectHandlerBeans.size(), validatorCount);
        log.info("Trigger auto-registration complete: {} trigger collectors", triggerCount);
    }

    private int registerTriggerCollectors() {
        int count = 0;

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();

            for (Method method : beanClass.getDeclaredMethods()) {
                CollectsTrigger single = method.getAnnotation(CollectsTrigger.class);
                if (single != null) {
                    registerTriggerHandler(bean, method, single);
                    count++;
                }

                CollectsTriggers container = method.getAnnotation(CollectsTriggers.class);
                if (container != null) {
                    for (CollectsTrigger ct : container.value()) {
                        registerTriggerHandler(bean, method, ct);
                        count++;
                    }
                }
            }
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    private void registerTriggerHandler(Object bean, Method method, CollectsTrigger annotation) {
        method.setAccessible(true);
        Class<?>[] params = method.getParameterTypes();

        if (params.length != 3
                || params[0] != TriggerMatchContext.class
                || !CardEffect.class.isAssignableFrom(params[1])
                || !TriggerContext.class.isAssignableFrom(params[2])) {
            throw new IllegalStateException(
                    "@CollectsTrigger method " + method.getDeclaringClass().getSimpleName() + "." + method.getName()
                            + " must have signature (TriggerMatchContext, <? extends CardEffect>, TriggerContext)");
        }

        if (method.getReturnType() != boolean.class) {
            throw new IllegalStateException(
                    "@CollectsTrigger method " + method.getDeclaringClass().getSimpleName() + "." + method.getName()
                            + " must return boolean");
        }

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
            Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[1];

            triggerCollectorRegistry.register(annotation.slot(), annotation.value(), (match, innerEffect, context) -> {
                try {
                    return (boolean) handle.invoke(match, effectParam.cast(innerEffect), context);
                } catch (Throwable t) {
                    throw wrapException(t, method);
                }
            });
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access @CollectsTrigger method " + method.getName(), e);
        }
    }

    private static RuntimeException wrapException(Throwable t, Method method) {
        if (t instanceof RuntimeException re) {
            throw re;
        }
        throw new RuntimeException("Error invoking trigger collector "
                + method.getDeclaringClass().getSimpleName() + "." + method.getName(), t);
    }
}
