package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.costmod.ConditionalCostModificationHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseEachPlayerCastCostPerSpellThisTurnEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseSpellCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseSpellCostExceptOnControllersTurnEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ModifyFlashbackCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceCastCostForChosenNameSpellsEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceCastCostForChosenSubtypeSpellsEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceCastCostForMatchingSpellsEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceBuybackCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

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
        ConditionEvaluationService conditionEvaluationService =
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService);

        CostModificationHandlerRegistry registry = new CostModificationHandlerRegistry();
        registry.register(new ReduceBuybackCostEffectHandler());
        registry.register(new IncreaseEachPlayerCastCostPerSpellThisTurnEffectHandler());
        registry.register(new IncreaseSpellCostEffectHandler(predicateEvaluationService, amountEvaluationService));
        registry.register(new IncreaseSpellCostExceptOnControllersTurnEffectHandler());
        registry.register(new ModifyFlashbackCostEffectHandler());
        registry.register(new IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler(gameQueryService));
        registry.register(new ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler(support, amountEvaluationService));
        registry.register(new ReduceCastCostForMatchingSpellsEffectHandler(predicateEvaluationService, amountEvaluationService));
        registry.register(new ReduceCastCostForChosenNameSpellsEffectHandler());
        registry.register(new ReduceCastCostForChosenSubtypeSpellsEffectHandler(gameQueryService));
        registry.register(new ReduceOwnCastCostEffectHandler(amountEvaluationService));
        registry.register(new ConditionalCostModificationHandler(conditionEvaluationService, registry));
        return registry;
    }
}
