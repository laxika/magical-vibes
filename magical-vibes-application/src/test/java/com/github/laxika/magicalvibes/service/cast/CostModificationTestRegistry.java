package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.costmod.ConditionalCostModificationHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseEachPlayerCastCostPerSpellThisTurnEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseOpponentCastCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseSpellCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceCastCostForMatchingSpellsEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostForCardTypeEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.mockito.Mockito;

/**
 * Builds the {@link CostModificationHandlerRegistry} with all production handlers, the same
 * way {@code GameEngineConfig} does at runtime, so unit tests exercise the real dispatch path.
 */
public final class CostModificationTestRegistry {

    private CostModificationTestRegistry() {
    }

    public static CostModificationHandlerRegistry build(GameQueryService gameQueryService,
                                                        PredicateEvaluationService predicateEvaluationService,
                                                        CostModificationSupport support) {
        AmountEvaluationService amountEvaluationService =
                new AmountEvaluationService(predicateEvaluationService, gameQueryService);
        // Cast-cost evaluation always uses non-static condition contexts, so the static-only
        // StaticEffectSupport dependency is never invoked here.
        ConditionEvaluationService conditionEvaluationService = new ConditionEvaluationService(
                gameQueryService, predicateEvaluationService, Mockito.mock(StaticEffectSupport.class));

        CostModificationHandlerRegistry registry = new CostModificationHandlerRegistry();
        registry.register(new IncreaseOpponentCastCostEffectHandler());
        registry.register(new IncreaseEachPlayerCastCostPerSpellThisTurnEffectHandler());
        registry.register(new IncreaseSpellCostEffectHandler(predicateEvaluationService));
        registry.register(new IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler(gameQueryService));
        registry.register(new ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler(support, amountEvaluationService));
        registry.register(new ReduceOwnCastCostForCardTypeEffectHandler(amountEvaluationService));
        registry.register(new ReduceCastCostForMatchingSpellsEffectHandler(predicateEvaluationService));
        registry.register(new ReduceOwnCastCostEffectHandler(amountEvaluationService));
        registry.register(new ConditionalCostModificationHandler(conditionEvaluationService, registry));
        return registry;
    }
}
