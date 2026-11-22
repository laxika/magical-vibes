package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Discards the identified card, or the cards {@code cards} names ("discard those cards" after a seek), those still in
 * the resolving controller's hand, as one event.
 *
 * @param cardId the card, bound by the effect that made this one; {@code null} when {@code cards} names them
 * @param cards  the cards an earlier step put into the hand
 */
public record DiscardSpecificCardEffect(UUID cardId, CardSubject cards) implements CardEffect {

    public DiscardSpecificCardEffect {
        if ((cardId == null) == (cards == null)) {
            throw new IllegalArgumentException("Either a card or a card subject names what is discarded");
        }
    }

    /** "Discard that card", of a bound card. */
    public DiscardSpecificCardEffect(UUID cardId) {
        this(cardId, null);
    }

    /** "Discard those cards." */
    public static DiscardSpecificCardEffect of(CardSubject cards) {
        return new DiscardSpecificCardEffect(null, cards);
    }
}
