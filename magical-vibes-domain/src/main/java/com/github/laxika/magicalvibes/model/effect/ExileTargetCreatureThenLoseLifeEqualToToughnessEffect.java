package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles a target creature, then makes the effect controller lose life equal to that creature's
 * last-known toughness unless that player controls a permanent matching the exemption predicate.
 */
public record ExileTargetCreatureThenLoseLifeEqualToToughnessEffect(
        PermanentPredicate exemptIfControls
) implements RemovalEffect {

    public ExileTargetCreatureThenLoseLifeEqualToToughnessEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
