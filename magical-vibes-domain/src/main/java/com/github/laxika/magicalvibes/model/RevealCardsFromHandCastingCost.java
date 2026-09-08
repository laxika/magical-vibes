package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from hand as part of an alternate casting cost without moving those cards.
 *
 * @param revealEntireHand whether the whole remaining hand is revealed instead of one matching card
 */
public record RevealCardsFromHandCastingCost(CardPredicate predicate, String label,
                                             boolean revealEntireHand) implements CastingCost {

    public RevealCardsFromHandCastingCost(CardPredicate predicate, String label) {
        this(predicate, label, false);
    }

    /** Reveals the caster's entire remaining hand. */
    public static RevealCardsFromHandCastingCost entireHand() {
        return new RevealCardsFromHandCastingCost(null, null, true);
    }
}
