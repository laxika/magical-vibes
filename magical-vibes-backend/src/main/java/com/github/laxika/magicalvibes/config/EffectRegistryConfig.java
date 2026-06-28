package com.github.laxika.magicalvibes.config;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.NormalEffectHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.NormalEffectHandlerBeanFactory;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectHandlerBean;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectHandlerBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class EffectRegistryConfig implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final StaticEffectHandlerRegistry staticEffectHandlerRegistry;
    private final TargetValidatorRegistry targetValidatorRegistry;

    /**
     * Migrated per-effect handlers, collected by Spring. Field-injected (not constructor-injected)
     * to avoid a bootstrap cycle: these beans depend on {@code GameQueryService}, which depends on
     * the {@code staticEffectHandlerRegistry} {@code @Bean} defined in this configuration.
     */
    @Autowired(required = false)
    private List<StaticEffectHandlerBean> staticEffectHandlerBeans = List.of();

    /**
     * Migrated per-effect "normal" (stack-resolution) handlers, collected by Spring. Field-injected
     * for the same bootstrap-cycle reasons as {@link #staticEffectHandlerBeans}.
     */
    @Autowired(required = false)
    private List<NormalEffectHandlerBean> normalEffectHandlerBeans = List.of();

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
        int validatorCount = 0;

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            validatorCount += TargetValidatorRegistry.scanBean(bean, targetValidatorRegistry);
        }

        StaticEffectHandlerBeanFactory.registerAll(staticEffectHandlerBeans, staticEffectHandlerRegistry);
        NormalEffectHandlerBeanFactory.registerAll(normalEffectHandlerBeans, effectHandlerRegistry);

        log.info("Effect auto-registration complete: {} normal handlers, {} static handlers, {} target validators",
                normalEffectHandlerBeans.size(), staticEffectHandlerBeans.size(), validatorCount);
    }
}
