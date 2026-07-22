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
 */
public record DiscardCardTypeCost(CardPredicate predicate, String label, boolean manaValueEqualsX, int count)
        implements CostEffect {

    public DiscardCardTypeCost {
        if (count < 1) {
            throw new IllegalArgumentException("discard count must be >= 1");
        }
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
