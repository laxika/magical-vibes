package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile card(s) from hand as an alternate casting cost (e.g. Scars of the Veteran:
 * "You may exile a white card from your hand rather than pay this spell's mana cost").
 *
 * @param predicate optional filter the exiled cards must match (null = any card)
 * @param label     human-readable quality for prompts/errors (e.g. "white")
 * @param count     number of cards that must be exiled
 */
public record ExileCardsFromHandCastingCost(CardPredicate predicate, String label, int count) implements CastingCost {

    public ExileCardsFromHandCastingCost {
        if (count < 1) {
            throw new IllegalArgumentException("exile count must be >= 1");
        }
    }

    public ExileCardsFromHandCastingCost(CardPredicate predicate, String label) {
        this(predicate, label, 1);
    }
}
