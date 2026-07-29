package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost effect that requires the player to exile card(s) from their hand ("Exile a card from your
 * hand: ..."). Unlike {@link DiscardCardTypeCost} the paid cards are exiled, so no discard
 * triggers fire.
 *
 * @param predicate optional predicate cards must match (null = any card)
 * @param label     human-readable label for the card quality, used in UI messages (may be null)
 * @param count     number of cards that must be exiled (default 1)
 */
public record ExileCardFromHandCost(CardPredicate predicate, String label, int count) implements HandCardCost {

    public ExileCardFromHandCost {
        if (count < 1) {
            throw new IllegalArgumentException("exile count must be >= 1");
        }
    }

    public ExileCardFromHandCost() {
        this(null, null, 1);
    }

    public ExileCardFromHandCost(CardPredicate predicate, String label) {
        this(predicate, label, 1);
    }

    @Override
    public boolean exilesPaidCards() {
        return true;
    }
}
