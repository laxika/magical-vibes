package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DamageDealtToTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Discards the controller's entire hand, then draws cards equal to a {@link DynamicAmount}
 * evaluated at draw time. Unlike {@link DiscardOwnHandThenDrawThatManyEffect} (draw = discarded
 * count) the draw count is independent of how many cards were discarded — e.g. Knollspine Dragon
 * draws equal to the damage dealt to target opponent this turn
 * ({@link DamageDealtToTargetPlayerThisTurn}). All discards are automatic (no player choice) and
 * fire discard triggers per card.
 */
public record DiscardOwnHandThenDrawEffect(DynamicAmount amount) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        // Only target-relative amounts require a player target on the stack entry.
        return amount instanceof DamageDealtToTargetPlayerThisTurn;
    }
}
