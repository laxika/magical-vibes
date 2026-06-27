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
                new MetalcraftConditionalEffectHandler(support, gameQueryService),
                new GrantKeywordEffectHandler(support),
                new RemoveKeywordEffectHandler(support),
                new GrantColorEffectHandler(support),
                new GrantSubtypeEffectHandler(support),
                new GrantCardTypeEffectHandler(support),
                new GrantSupertypeToEnchantedPermanentEffectHandler(),
                new LosesAllAbilitiesEffectHandler(support),
                new SetBasePowerToughnessStaticEffectHandler(support),
                new StaticBoostSelfEffectHandler(support),
                new StaticBoostEffectHandler(support),
                new GrantEffectSelfEffectHandler(support),
                new GrantEffectEffectHandler(support),
                new GrantActivatedAbilityEffectHandler(support),
                new ProtectionFromColorsEffectHandler(),
                new AnimateNoncreatureArtifactsEffectHandler(gameQueryService),
                new GrantEquipByManaValueEffectHandler(support),
                new EnchantedPermanentConditionalEffectHandler(support, registry),
                new GrantChosenSubtypeToOwnCreaturesEffectHandler(support),
                new EnchantedPermanentBecomesTypeEffectHandler(),
                new EnchantedPermanentBecomesChosenTypeEffectHandler(),
                new BoostCreaturesOfChosenColorEffectHandler(),
                new BoostCreaturesOfChosenSubtypeEffectHandler(support, gameQueryService),
                new BoostCreaturePerCardsInAllGraveyardsEffectHandler(support),
                new BoostCreaturePerCardsInControllerGraveyardEffectHandler(support, gameQueryService),
                new BoostCreaturePerMatchingLandNameEffectHandler(support),
                new BoostCreaturePerControlledSubtypeEffectHandler(support),
                new BoostCreaturePerControlledCardTypeEffectHandler(support),
                new BoostBySharedCreatureTypeEffectHandler(support),
                new BoostByOtherCreaturesWithSameNameSelfEffectHandler(support),
                new BoostSelfPerEnchantmentOnBattlefieldSelfEffectHandler(),
                new BoostSelfPerControlledPermanentSelfEffectHandler(support, gameQueryService),
                new BoostSelfPerCardsInControllerGraveyardSelfEffectHandler(support, gameQueryService),
                new BoostSelfPerOpponentPermanentSelfEffectHandler(support, gameQueryService),
                new BoostSelfPerEquipmentAttachedSelfEffectHandler(),
                new BoostSelfPerAttachmentSelfEffectHandler(),
                new BoostSelfByImprintedCreaturePTSelfEffectHandler(),
                new BoostSelfPerOpponentPoisonCounterSelfEffectHandler(support),
                new BoostSelfBySlimeCountersOnLinkedPermanentSelfEffectHandler(gameQueryService),
                new BoostSelfPerOtherControlledSubtypeSelfEffectHandler(support),
                new PowerToughnessEqualToCreatureCardsInAllGraveyardsSelfEffectHandler(support),
                new PowerToughnessEqualToCardsInAllGraveyardsSelfEffectHandler(support),
                new PowerToughnessEqualToCardsInControllerGraveyardSelfEffectHandler(support, gameQueryService),
                new PowerToughnessEqualToControlledLandCountSelfEffectHandler(support),
                new PowerToughnessEqualToControlledPermanentCountSelfEffectHandler(support, gameQueryService),
                new PowerToughnessEqualToControlledCreatureCountSelfEffectHandler(support),
                new PowerToughnessEqualToCardsInHandSelfEffectHandler(support),
                new PowerToughnessEqualToControllerLifeTotalSelfEffectHandler(support)
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
