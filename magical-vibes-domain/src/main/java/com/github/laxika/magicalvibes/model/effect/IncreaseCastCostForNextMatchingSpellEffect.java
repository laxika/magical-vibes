package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/** Increases the cost of a targeted player's next matching spell. */
public record IncreaseCastCostForNextMatchingSpellEffect(
        CardPredicate predicate, int amount, UUID targetPlayerId) implements NextMatchingSpellCostEffect {

    public IncreaseCastCostForNextMatchingSpellEffect(CardPredicate predicate, int amount) {
        this(predicate, amount, null);
    }

    @Override
    public boolean appliesToPlayer(UUID castingPlayerId, UUID effectControllerId) {
        return targetPlayerId != null && targetPlayerId.equals(castingPlayerId);
    }
}
