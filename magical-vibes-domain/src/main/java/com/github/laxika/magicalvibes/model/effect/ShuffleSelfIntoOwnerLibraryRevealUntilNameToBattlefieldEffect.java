package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles the source permanent into its owner's library. If that was done, reveals cards
 * from the top of the owner's library until a card with the specified name is found. That card
 * is put onto the battlefield under the owner's control, and all other revealed cards are put
 * into the owner's graveyard.
 * <p>
 * If no card with the specified name is found, all revealed cards go to the graveyard.
 * <p>
 * Used by Mirror-Mad Phantasm.
 */
public record ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect(
        String cardName
) implements CardEffect {
}
