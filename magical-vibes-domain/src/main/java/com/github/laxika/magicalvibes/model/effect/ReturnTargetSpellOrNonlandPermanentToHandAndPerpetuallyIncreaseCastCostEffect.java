package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Returns a target spell or nonland permanent to its owner's hand. The returned card is taxed
 * perpetually by {@code amount} generic mana only when {@code perpetualCondition} is met at resolution;
 * a {@code null} condition always taxes it (Nightclub Bouncer), while Talion's Throneguard passes
 * {@code Kicked} so the tax applies only when it was bargained.
 */
public record ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect(int amount,
                                                                                           Condition perpetualCondition)
        implements RemovalEffect {

    public ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect() {
        this(2, null);
    }

    public ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect(int amount) {
        this(amount, null);
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
