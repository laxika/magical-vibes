package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "You may reveal a sorcery card you own from outside the game and put it into your hand", "you may put a card you
 * own from outside the game into your hand", "you may put a card you own from outside the game on top of your
 * library": the controller may take one matching card from their outside-the-game pool, revealed only when the text
 * says so (CR 400.2), into their hand or on top of their library.
 *
 * @param filter      which cards; {@code null} for any card
 * @param reveals     whether the card is revealed as it is taken
 * @param destination {@link LibrarySearchDestination#HAND} or {@link LibrarySearchDestination#TOP_OF_LIBRARY}
 */
public record SearchOutsideGameForCardToHandEffect(CardPredicate filter, boolean reveals,
                                                   LibrarySearchDestination destination) implements CardEffect {

    public SearchOutsideGameForCardToHandEffect {
        Objects.requireNonNull(destination, "destination");
        if (destination != LibrarySearchDestination.HAND && destination != LibrarySearchDestination.TOP_OF_LIBRARY) {
            throw new IllegalArgumentException("A card from outside the game goes to a hand or a library's top: "
                    + destination);
        }
    }

    /** "You may reveal a [filter] card you own from outside the game and put it into your hand." */
    public SearchOutsideGameForCardToHandEffect(CardPredicate filter) {
        this(filter, true, LibrarySearchDestination.HAND);
    }

    /** "You may put a [filter] card you own from outside the game into your hand": not revealed. */
    public static SearchOutsideGameForCardToHandEffect unrevealed(CardPredicate filter) {
        return new SearchOutsideGameForCardToHandEffect(filter, false, LibrarySearchDestination.HAND);
    }

    /** "You may put a card you own from outside the game on top of your library": not revealed. */
    public static SearchOutsideGameForCardToHandEffect onTopOfLibrary() {
        return new SearchOutsideGameForCardToHandEffect(null, false, LibrarySearchDestination.TOP_OF_LIBRARY);
    }
}
