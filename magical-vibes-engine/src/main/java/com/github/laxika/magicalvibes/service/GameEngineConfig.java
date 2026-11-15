package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.NetworkingConfig;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.MayEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.service.effect.mayfx.MayEffectHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.NormalEffectHandlerBean;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectHandlerBean;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * Single Spring configuration for the game engine object graph: services, support beans,
 * effect handlers and target validators. Used by production, card tests,
 * and headless AI simulation — with different {@code SessionManager} / {@code GameRegistry}
 * bindings supplied by each outer context.
 *
 * <p>Owns only the engine's own {@code service} package and composes the networking wiring
 * ({@link NetworkingConfig}) via {@code @Import}. This keeps each module responsible for its own
 * beans instead of having downstream modules reach in with their own component scans.
 *
 * <p>Card data ({@code CardDataConfiguration}) is deliberately left to each outer context. The
 * application and the card-test context import it next to this config; headless AI simulation
 * reuses the application's already-loaded card-data beans instead of loading every set again.
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.github.laxika.magicalvibes.service")
@Import(NetworkingConfig.class)
public class GameEngineConfig implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    private final EffectHandlerRegistry effectHandlerRegistry;

    private final MayEffectHandlerRegistry mayEffectHandlerRegistry;

    private final StaticEffectHandlerRegistry staticEffectHandlerRegistry;

    private final TargetValidatorRegistry targetValidatorRegistry;

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    private final CostModificationHandlerRegistry costModificationHandlerRegistry;

    public GameEngineConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.effectHandlerRegistry = new EffectHandlerRegistry();
        this.mayEffectHandlerRegistry = new MayEffectHandlerRegistry();
        this.staticEffectHandlerRegistry = new StaticEffectHandlerRegistry();
        this.targetValidatorRegistry = new TargetValidatorRegistry();
        this.interactionHandlerRegistry = new InteractionHandlerRegistry(
                () -> applicationContext.getBean(
                        com.github.laxika.magicalvibes.service.event.GameMutationCoordinator.class));
        this.costModificationHandlerRegistry = new CostModificationHandlerRegistry();
    }

    @Bean
    public EffectHandlerRegistry effectHandlerRegistry() {
        return effectHandlerRegistry;
    }

    @Bean
    public MayEffectHandlerRegistry mayEffectHandlerRegistry() {
        return mayEffectHandlerRegistry;
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
    public InteractionHandlerRegistry interactionHandlerRegistry() {
        return interactionHandlerRegistry;
    }

    @Bean
    public CostModificationHandlerRegistry costModificationHandlerRegistry() {
        return costModificationHandlerRegistry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<StaticEffectHandlerBean> staticEffectHandlerBeans =
                new ArrayList<>(applicationContext.getBeansOfType(StaticEffectHandlerBean.class).values());
        List<NormalEffectHandlerBean> normalEffectHandlerBeans =
                new ArrayList<>(applicationContext.getBeansOfType(NormalEffectHandlerBean.class).values());
        List<MayEffectHandlerBean> mayEffectHandlerBeans =
                new ArrayList<>(applicationContext.getBeansOfType(MayEffectHandlerBean.class).values());

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

        for (MayEffectHandlerBean bean : mayEffectHandlerBeans) {
            mayEffectHandlerRegistry.register(bean.handledEffect(), bean);
        }

        for (InteractionHandler<?> bean : applicationContext.getBeansOfType(InteractionHandler.class).values()) {
            interactionHandlerRegistry.register(bean);
        }

        for (CostModificationHandlerBean bean : applicationContext.getBeansOfType(CostModificationHandlerBean.class).values()) {
            costModificationHandlerRegistry.register(bean);
        }

        log.info("Effect auto-registration complete: {} normal handlers, {} may handlers, {} static handlers, {} target validators",
                normalEffectHandlerBeans.size(), mayEffectHandlerBeans.size(), staticEffectHandlerBeans.size(), validatorCount);
    }
}
