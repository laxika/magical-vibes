package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;

import java.util.List;

/**
 * Single source of truth for instantiating the migrated {@link StaticEffectHandlerBean} handlers in
 * non-Spring sites (e.g. {@code GameTestHarness}, {@code GameSimulator}).
 *
 * <p>In Spring, these handlers are discovered as {@code @Component}s; outside Spring, callers use
 * {@link #createAll} to build the same set manually and {@link #registerAll} to register them. As
 * more effects migrate, only this factory needs updating — non-Spring sites never change again.
 */
public final class StaticEffectHandlerBeanFactory {

    private StaticEffectHandlerBeanFactory() {
    }

    public static List<StaticEffectHandlerBean> createAll(StaticEffectSupport support,
                                                          GameQueryService gameQueryService,
                                                          StaticEffectHandlerRegistry registry) {
        return List.of(
                new MetalcraftConditionalSelfEffectHandler(support, gameQueryService),
                new MetalcraftConditionalEffectHandler(support, gameQueryService)
        );
    }

    public static void registerAll(List<StaticEffectHandlerBean> beans, StaticEffectHandlerRegistry registry) {
        for (StaticEffectHandlerBean bean : beans) {
            if (bean.selfOnly()) {
                registry.registerSelfHandler(bean.handledEffect(), bean);
            } else {
                registry.register(bean.handledEffect(), bean);
            }
        }
    }
}
