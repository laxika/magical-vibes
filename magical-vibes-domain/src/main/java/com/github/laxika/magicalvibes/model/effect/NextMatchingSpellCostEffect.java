package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/** A one-shot cost modification waiting for the next matching spell cast. */
public interface NextMatchingSpellCostEffect extends CardEffect {

    CardPredicate predicate();

    default boolean appliesToPlayer(UUID castingPlayerId, UUID effectControllerId) {
        return effectControllerId != null && effectControllerId.equals(castingPlayerId);
    }

    default boolean appliesToFaceDownCast(boolean castFaceDown) {
        return true;
    }
}
