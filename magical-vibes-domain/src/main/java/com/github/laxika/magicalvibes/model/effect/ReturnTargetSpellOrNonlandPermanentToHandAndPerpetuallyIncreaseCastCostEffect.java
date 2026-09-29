package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/** Returns a target spell or nonland permanent to its owner's hand and taxes that card perpetually. */
public record ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect(int amount)
        implements RemovalEffect {

    public ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect() {
        this(2);
    }

    public ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(new PermanentNotPredicate(new PermanentIsLandPredicate())),
                TargetPredicates.spellOnStack()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
