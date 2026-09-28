package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.Set;
import java.util.UUID;

/** A controller-scoped activation-cost reduction usable once each turn per source permanent. */
public interface FirstActivatedAbilityCostReducingEffect extends ActivatedAbilityCostReducingEffect {

    String usageKey();

    default boolean isUsedThisTurn(GameData gameData, UUID reducingPermanentId) {
        return gameData.keyedOncePerTurnTriggersFiredThisTurn
                .getOrDefault(reducingPermanentId, Set.of())
                .contains(usageKey());
    }

    default void markUsedThisTurn(GameData gameData, UUID reducingPermanentId) {
        gameData.keyedOncePerTurnTriggersFiredThisTurn
                .computeIfAbsent(reducingPermanentId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(usageKey());
    }

    /** Whether at least one chosen target must be a creature controlled by the activating player. */
    default boolean requiresControlledCreatureTarget() {
        return false;
    }
}
