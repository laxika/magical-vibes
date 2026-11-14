package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Exiles the target permanent and lets its owner play it for as long as it remains exiled.
 * Spells cast by an opponent of this effect's controller using that permission cost
 * {@code opponentTax} more. A tax of zero grants the permission without an additional cost.
 *
 * @param opponentTax       the added cost of an opponent's spell cast with the permission
 * @param targetRestriction the permanents it may target: a nonland permanent by default (Soul Partition, Aerial
 *                          Extortionist), a creature for "up to X target creatures" (Extraordinary Journey)
 */
public record ExileTargetPermanentMayPlayWithOpponentTaxEffect(int opponentTax, PermanentPredicate targetRestriction)
        implements RemovalEffect {

    public ExileTargetPermanentMayPlayWithOpponentTaxEffect {
        Objects.requireNonNull(targetRestriction, "targetRestriction");
    }

    public ExileTargetPermanentMayPlayWithOpponentTaxEffect(int opponentTax) {
        this(opponentTax, new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    public ExileTargetPermanentMayPlayWithOpponentTaxEffect() {
        this(2);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetRestriction);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
