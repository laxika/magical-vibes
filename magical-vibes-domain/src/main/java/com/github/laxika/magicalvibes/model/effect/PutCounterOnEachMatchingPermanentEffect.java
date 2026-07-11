package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Put {@code amount} counter(s) of the specified type on each permanent matching {@code predicate}
 * across the battlefields selected by {@code scope}.
 *
 * <p>Covers "each attacking creature" ({@link EachPermanentScope#ALL_PLAYERS} + an attacking-creature
 * predicate), "each other creature" ({@code ALL_PLAYERS} + creature-and-not-the-source), "each
 * creature target player controls" ({@link EachPermanentScope#TARGET_PLAYER}) and "each creature"
 * ({@code ALL_PLAYERS}). For "each permanent you control" use
 * {@link PutCounterOnEachControlledPermanentEffect} instead.</p>
 */
public record PutCounterOnEachMatchingPermanentEffect(CounterType counterType, DynamicAmount amount,
                                                      PermanentPredicate predicate,
                                                      EachPermanentScope scope) implements CardEffect {

    public PutCounterOnEachMatchingPermanentEffect(CounterType counterType, int count,
                                                   PermanentPredicate predicate, EachPermanentScope scope) {
        this(counterType, new Fixed(count), predicate, scope);
    }

    @Override
    public boolean canTargetPlayer() {
        return scope == EachPermanentScope.TARGET_PLAYER;
    }
}
