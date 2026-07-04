package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseEachPlayerCastCostPerSpellThisTurnEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseOpponentCastCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.IncreaseSpellCostEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceCastCostForMatchingSpellsEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostForCardTypeEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostIfControlsPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostIfMetalcraftEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostPerCreatureCardInGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.cast.costmod.ReduceOwnCastCostPerCreatureOnBattlefieldEffectHandler;
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
        CostModificationHandlerRegistry registry = new CostModificationHandlerRegistry();
        registry.register(new IncreaseOpponentCastCostEffectHandler());
        registry.register(new IncreaseEachPlayerCastCostPerSpellThisTurnEffectHandler());
        registry.register(new IncreaseSpellCostEffectHandler(predicateEvaluationService));
        registry.register(new ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler(support));
        registry.register(new ReduceOwnCastCostForCardTypeEffectHandler());
        registry.register(new ReduceCastCostForMatchingSpellsEffectHandler(predicateEvaluationService));
        registry.register(new ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffectHandler(support));
        registry.register(new ReduceOwnCastCostIfMetalcraftEffectHandler(gameQueryService));
        registry.register(new ReduceOwnCastCostPerCreatureOnBattlefieldEffectHandler(support));
        registry.register(new ReduceOwnCastCostPerCreatureCardInGraveyardEffectHandler(support));
        registry.register(new ReduceOwnCastCostIfControlsPermanentEffectHandler(support));
        return registry;
    }
}
