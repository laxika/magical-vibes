package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.costmod.ConditionalCostModificationHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ConditionalBattlefieldCostModificationHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ForetellCostReductionEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseSpellCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceTurnFaceUpCostForPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.MinimumSpellCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceCastCostForMatchingSpellsEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceCastCostForEachChosenColorEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceColoredCastCostForMatchingSpellsEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.PayLifeToReduceColoredCastCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ModifyKeywordCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceNonHandSpellCastCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnColoredCastCostEffectHandler;
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
        registry.register(new ModifyKeywordCostEffectHandler());
        registry.register(new ForetellCostReductionEffectHandler());
        registry.register(new MinimumSpellCostEffectHandler());
        registry.register(new IncreaseSpellCostEffectHandler(predicateEvaluationService, amountEvaluationService));
        registry.register(new ReduceTurnFaceUpCostForPermanentEffectHandler());
        registry.register(new IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler(gameQueryService));
        registry.register(new ReduceCastCostForMatchingSpellsEffectHandler(predicateEvaluationService, amountEvaluationService));
        registry.register(new ReduceCastCostForEachChosenColorEffectHandler(gameQueryService));
        registry.register(new ReduceNonHandSpellCastCostEffectHandler());
        registry.register(new ReduceColoredCastCostForMatchingSpellsEffectHandler(predicateEvaluationService));
        registry.register(new ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffectHandler());
        registry.register(new PayLifeToReduceColoredCastCostEffectHandler(predicateEvaluationService));
        registry.register(new ReduceOwnCastCostEffectHandler(amountEvaluationService));
        registry.register(new ReduceOwnColoredCastCostEffectHandler(amountEvaluationService));
        registry.register(new ConditionalCostModificationHandler(conditionEvaluationService, registry));
        registry.register(new ConditionalBattlefieldCostModificationHandler(conditionEvaluationService, registry));
        return registry;
    }
}
