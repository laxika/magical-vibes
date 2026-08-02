package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost effect that requires the player to discard card(s) matching a predicate.
 *
 * @param predicate        optional predicate cards must match (null = any card)
 * @param label            human-readable label for the card quality (e.g. "land", "historic"), used in UI messages
 * @param manaValueEqualsX when true, the discarded card's mana value must equal the ability's chosen X
 *                         (e.g. Knollspine Invocation "Discard a card with mana value X")
 * @param count            number of cards that must be discarded (default 1; Haunted Dead = 2)
 * @param sameName         when true, all discarded cards must share the same name
 *                         (e.g. Sphinx of the Chimes "Discard two nonland cards with the same name")
 * @param trackManaValue   when true, snapshot the discarded card's mana value into the stack entry's
 *                         {@code xValue} at payment (Mercurial Chemister)
 * @param imprintOnSource  when true, imprint the discarded card on the source card so the ability's own
 *                         effects can inspect it at resolution via the {@code ImprintedCardMatches}
 *                         condition ("If the discarded card was a Zombie card" — Necromancer's Stockpile)
 */
public record DiscardCardTypeCost(CardPredicate predicate, String label, boolean manaValueEqualsX, int count,
                                  boolean sameName, boolean trackManaValue, boolean imprintOnSource)
        implements HandCardCost {

    public DiscardCardTypeCost {
        if (count < 1) {
            throw new IllegalArgumentException("discard count must be >= 1");
        }
    }

    public DiscardCardTypeCost(CardPredicate predicate, String label, boolean manaValueEqualsX, int count,
                               boolean sameName, boolean trackManaValue) {
        this(predicate, label, manaValueEqualsX, count, sameName, trackManaValue, false);
    }

    public DiscardCardTypeCost(CardPredicate predicate, String label, boolean manaValueEqualsX, int count,
                               boolean sameName) {
        this(predicate, label, manaValueEqualsX, count, sameName, false);
    }

    public DiscardCardTypeCost(CardPredicate predicate, String label, boolean manaValueEqualsX, int count) {
        this(predicate, label, manaValueEqualsX, count, false);
    }

    public DiscardCardTypeCost(CardPredicate predicate, String label, int count, boolean sameName) {
        this(predicate, label, false, count, sameName);
    }

    public DiscardCardTypeCost(CardPredicate predicate, String label) {
        this(predicate, label, false, 1);
    }

    public DiscardCardTypeCost(CardPredicate predicate, String label, boolean manaValueEqualsX) {
        this(predicate, label, manaValueEqualsX, 1);
    }

    public DiscardCardTypeCost(CardPredicate predicate, String label, int count) {
        this(predicate, label, false, count);
    }
}
