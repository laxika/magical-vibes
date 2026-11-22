package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Put the rest on the bottom of their owners' libraries", "put that card on the bottom of its owner's library": each
 * card {@code cards} names that is in a graveyard or in exile goes there, all as one event, each owner ordering their
 * own (CR 401.4). A card the subject finds in another zone stays there.
 *
 * @param cards the cards put on the bottom
 */
public record PutCardsOnBottomOfLibraryEffect(CardSubject cards) implements CardEffect {

    public PutCardsOnBottomOfLibraryEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names cards it doesn't target: " + cards);
        }
    }
}
