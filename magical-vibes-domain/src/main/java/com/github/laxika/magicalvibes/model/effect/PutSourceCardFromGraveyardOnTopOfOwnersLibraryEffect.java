package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved as a death trigger, puts the dying creature's card from its owner's
 * graveyard on top of its owner's library. Uses the stack entry's card (the source of
 * the trigger) to identify which card to move.
 *
 * <p>This is the library-top analogue of
 * {@link ReturnSourceCardFromGraveyardToOwnerHandEffect}. Used by Undying Beast.
 */
public record PutSourceCardFromGraveyardOnTopOfOwnersLibraryEffect() implements CardEffect {
}
