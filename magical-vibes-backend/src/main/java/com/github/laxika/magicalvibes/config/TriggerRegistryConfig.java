package com.github.laxika.magicalvibes.config;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.trigger.CollectsTrigger;
import com.github.laxika.magicalvibes.service.trigger.CollectsTriggers;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectorRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.TriggerMatchContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Auto-discovers {@link CollectsTrigger}-annotated methods on Spring beans and registers them
 * in the {@link TriggerCollectorRegistry}.
 * <p>
 * Handler method must have signature:
 * <pre>
 *   boolean method(TriggerMatchContext, ConcreteEffect, TriggerContext)
 * </pre>
 */
@Slf4j
@Configuration
public class TriggerRegistryConfig implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final TriggerCollectorRegistry triggerCollectorRegistry;

    public TriggerRegistryConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.triggerCollectorRegistry = new TriggerCollectorRegistry();
    }

    @Bean
    public TriggerCollectorRegistry triggerCollectorRegistry() {
        return triggerCollectorRegistry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        int count = 0;

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();

            for (Method method : beanClass.getDeclaredMethods()) {
                // Handle single annotation
                CollectsTrigger single = method.getAnnotation(CollectsTrigger.class);
                if (single != null) {
                    registerTriggerHandler(bean, method, single);
                    count++;
                }

                // Handle repeatable container
                CollectsTriggers container = method.getAnnotation(CollectsTriggers.class);
                if (container != null) {
                    for (CollectsTrigger ct : container.value()) {
                        registerTriggerHandler(bean, method, ct);
                        count++;
                    }
                }
            }
        }

        log.info("Trigger auto-registration complete: {} trigger collectors", count);
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
