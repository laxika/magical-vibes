package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile card(s) from hand as an alternate casting cost (e.g. Scars of the Veteran:
 * "You may exile a white card from your hand rather than pay this spell's mana cost").
 *
 * @param predicate         optional filter the exiled cards must match (null = any card)
 * @param label             human-readable quality for prompts/errors (e.g. "white")
 * @param count             number of cards that must be exiled
 * @param manaValueEqualsX  whether each exiled card's mana value must equal the X chosen for the
 *                          spell (the Shoal cycle: "exile a white card with mana value X")
 */
public record ExileCardsFromHandCastingCost(CardPredicate predicate, String label, int count,
                                            boolean manaValueEqualsX) implements CastingCost {

    public ExileCardsFromHandCastingCost {
        if (count < 1) {
            throw new IllegalArgumentException("exile count must be >= 1");
        }
    }

    public ExileCardsFromHandCastingCost(CardPredicate predicate, String label, int count) {
        this(predicate, label, count, false);
    }

    public ExileCardsFromHandCastingCost(CardPredicate predicate, String label) {
        this(predicate, label, 1);
    }

    /** "Exile a [label] card with mana value X from your hand rather than pay this spell's mana cost." */
    public static ExileCardsFromHandCastingCost withManaValueX(CardPredicate predicate, String label) {
        return new ExileCardsFromHandCastingCost(predicate, label, 1, true);
    }

    public ExileCardsFromHandCastingCost(CardPredicate predicate, String label, boolean manaValueEqualsX) {
        this(predicate, label, 1, manaValueEqualsX);
    }
}
