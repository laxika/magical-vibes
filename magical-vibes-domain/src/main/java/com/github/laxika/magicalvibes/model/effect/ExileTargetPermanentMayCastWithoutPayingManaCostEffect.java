package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the target permanent, then lets its owner cast the exiled card without paying its mana
 * cost for as long as it remains exiled.
 */
public record ExileTargetPermanentMayCastWithoutPayingManaCostEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
