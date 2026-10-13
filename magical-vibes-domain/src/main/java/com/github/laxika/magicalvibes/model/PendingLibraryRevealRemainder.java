package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * The rest of a library reveal-and-put choice (Collected Company and similar), parked while a placed
 * card's "enter as a copy" choice is open. That choice is a replacement effect (CR 614.1c) made as the
 * card enters, so the unchosen cards are handled only after it is answered.
 *
 * @param choice         the answered reveal choice, which says where the unchosen cards go
 * @param selectedCards  the cards put onto the battlefield
 * @param remainingCards the cards still to be handled
 */
public record PendingLibraryRevealRemainder(PendingInteraction.LibraryRevealChoice choice,
                                            List<Card> selectedCards,
                                            List<Card> remainingCards) implements PendingInteraction {

    public PendingLibraryRevealRemainder {
        selectedCards = List.copyOf(selectedCards);
        remainingCards = List.copyOf(remainingCards);
    }
}
