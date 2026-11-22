package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Exile it" of cards an earlier step named in a hand: a sought card ("if that card is in your hand, exile it") or
 * the card chosen from a revealed hand ("exile the chosen cards", Dreams of Steel and Oil), from the hand it is in.
 *
 * @param cards the cards
 */
public record ExileSpecificCardFromHandEffect(CardSubject cards) implements CardEffect {

    public ExileSpecificCardFromHandEffect {
        Objects.requireNonNull(cards, "cards");
    }
}
