package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Exile it" of cards an earlier step put into a hand ("if that card is in your hand, exile it" after a seek): those
 * still in the resolving controller's hand.
 *
 * @param cards the cards
 */
public record ExileSpecificCardFromHandEffect(CardSubject cards) implements CardEffect {

    public ExileSpecificCardFromHandEffect {
        Objects.requireNonNull(cards, "cards");
    }
}
