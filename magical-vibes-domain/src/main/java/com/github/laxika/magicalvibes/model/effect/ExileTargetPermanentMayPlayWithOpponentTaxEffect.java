package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Exiles the target nonland permanent and lets its owner play it for as long as it remains exiled.
 * Spells cast by an opponent of this effect's controller using that permission cost
 * {@code opponentTax} more. A tax of zero grants the permission without an additional cost.
 */
public record ExileTargetPermanentMayPlayWithOpponentTaxEffect(int opponentTax) implements RemovalEffect {

    public ExileTargetPermanentMayPlayWithOpponentTaxEffect() {
        this(2);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
