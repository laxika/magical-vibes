package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Offers a resolution-time choice of one captured card to copy until end of turn. */
public record BecomeCopyOfOneOfCardsUntilEndOfTurnEffect(List<Card> cards) implements CardEffect {

    public BecomeCopyOfOneOfCardsUntilEndOfTurnEffect {
        cards = List.copyOf(cards);
    }
}
