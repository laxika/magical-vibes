package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved as a death trigger, puts the dying creature's card from its owner's
 * graveyard into its owner's library at {@code position} cards from the top (0-indexed:
 * 0 = top, 1 = second, 2 = third). If the library has fewer cards than the position, the
 * card is placed on the bottom. Uses the stack entry's card (the source of the trigger)
 * to identify which card to move.
 *
 * <p>This is the graveyard-source analogue of
 * {@link PutTargetPermanentIntoLibraryNFromTopEffect}, and the library sibling of
 * {@link ReturnSourceCardFromGraveyardToOwnerHandEffect}. Used by Undying Beast
 * ({@code position = 0}) and Enigma Sphinx ({@code position = 2}, "third from the top").
 */
public record PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(int position) implements CardEffect {
}
