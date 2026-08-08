package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles target permanent, then searches its controller's graveyard, hand, and library for all
 * cards with the same name as that permanent and exiles them. Then that player shuffles.
 * <p>
 * The search is fully automatic (no "any number" choice). The exiled permanent itself goes to
 * exile rather than the graveyard, so it is not caught a second time by the search.
 * <p>
 * {@code targetPredicate} narrows the legal target (an enchantment for Scour, a nonblack creature
 * for Eradicate) and is used both as the declarative target spec and as the card's target filter.
 * <p>
 * Used by: Scour, Eradicate
 */
public record ExileTargetPermanentAndAllWithSameNameFromZonesEffect(PermanentPredicate targetPredicate)
        implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
