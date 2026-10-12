package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles target permanent, then searches its controller's graveyard, hand, and library for all
 * cards with the same name as that permanent and exiles them. Then that player shuffles.
 * <p>
 * By default the search is fully automatic. The optional subtype and any-number parameters can
 * instead make the search conditional on the exiled permanent's subtype and offer the controller
 * a choice of any number of matching cards. The optional draw flag makes the searched player draw
 * a card for each matching card exiled from their hand. The optional hidden-zone flag models "all
 * cards with the same name": every graveyard copy is exiled automatically, while the hand and library
 * copies are offered as a choice because a hidden zone need not be searched exhaustively. The exiled
 * permanent itself goes to exile
 * rather than the graveyard, so it is not caught a second time by the search.
 * <p>
 * {@code targetPredicate} narrows the legal target (an enchantment for Scour, a nonblack creature
 * for Eradicate) and is used both as the declarative target spec and as the card's target filter.
 * <p>
 * Used by: Scour, Eradicate, Deicide, The End
 */
public record ExileTargetPermanentAndAllWithSameNameFromZonesEffect(
        PermanentPredicate targetPredicate, CardSubtype requiredTargetSubtype, boolean chooseAnyNumber,
        boolean drawForHandExiled, boolean hiddenZonesOptional)
        implements RemovalEffect {

    public ExileTargetPermanentAndAllWithSameNameFromZonesEffect(PermanentPredicate targetPredicate) {
        this(targetPredicate, null, false, false, false);
    }

    public ExileTargetPermanentAndAllWithSameNameFromZonesEffect(
            PermanentPredicate targetPredicate, CardSubtype requiredTargetSubtype, boolean chooseAnyNumber,
            boolean drawForHandExiled) {
        this(targetPredicate, requiredTargetSubtype, chooseAnyNumber, drawForHandExiled, false);
    }

    /** Creates the "all cards with the same name" form, whose hand and library copies are optional. */
    public static ExileTargetPermanentAndAllWithSameNameFromZonesEffect allCopiesHiddenZonesOptional(
            PermanentPredicate targetPredicate) {
        return new ExileTargetPermanentAndAllWithSameNameFromZonesEffect(targetPredicate, null, false, false, true);
    }

    public ExileTargetPermanentAndAllWithSameNameFromZonesEffect(
            PermanentPredicate targetPredicate, CardSubtype requiredTargetSubtype, boolean chooseAnyNumber) {
        this(targetPredicate, requiredTargetSubtype, chooseAnyNumber, false, false);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
