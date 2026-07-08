package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deal {@code damage} to each permanent matching {@code predicate} across the battlefields
 * selected by {@code scope}.
 *
 * <p>Covers "each attacking creature" ({@link EachPermanentScope#ALL_PLAYERS} + an attacking-creature
 * predicate), "each creature target player controls" ({@link EachPermanentScope#TARGET_PLAYER}) and
 * filtered subsets such as "each attacking or blocking creature target player controls".</p>
 */
public record DealDamageToEachMatchingPermanentEffect(int damage, PermanentPredicate predicate,
                                                      EachPermanentScope scope) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return scope == EachPermanentScope.TARGET_PLAYER;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
