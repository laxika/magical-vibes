package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Searches the controller's library for two cards: puts one into hand and the other into the
 * graveyard, then shuffles. First pick is to hand (no shuffle yet); the second rides
 * {@code LibrarySearchFollowUp.cardToGraveyard}.
 *
 * <p>{@code ()} — Final Parting: two unrestricted cards, mandatory, unrevealed.
 * {@link #upToCreaturesRevealed()} — Jarad's Orders: up to two creature cards, revealed
 * (finding one puts it in hand; finding zero is allowed).
 */
public record SearchLibraryForCardToHandAndCardToGraveyardEffect(
        CardPredicate filter,
        boolean canFailToFind,
        boolean reveals
) implements CardEffect {

    /** Final Parting: two unrestricted cards, mandatory, unrevealed. */
    public SearchLibraryForCardToHandAndCardToGraveyardEffect() {
        this(null, false, false);
    }

    /** Jarad's Orders: up to two creature cards, revealed. */
    public static SearchLibraryForCardToHandAndCardToGraveyardEffect upToCreaturesRevealed() {
        return new SearchLibraryForCardToHandAndCardToGraveyardEffect(
                new CardTypePredicate(CardType.CREATURE), true, true);
    }
}
