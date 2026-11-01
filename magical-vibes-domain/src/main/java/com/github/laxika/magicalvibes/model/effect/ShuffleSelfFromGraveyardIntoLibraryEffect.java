package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered-ability effect: "shuffle it into its owner's library." Resolves the source card
 * from its owner's graveyard back into their library. Used together with the
 * "when this is put into a graveyard from anywhere" ability for cards like Purity
 * ("When Purity is put into a graveyard from anywhere, shuffle it into its owner's library.").
 *
 * <p>Distinct from {@code ShuffleIntoLibraryReplacementEffect}: this is a triggered ability —
 * the card first goes to the graveyard (firing dies/graveyard triggers), then this trigger
 * shuffles it in on resolution — rather than a replacement that keeps it out of the graveyard.
 */
public record ShuffleSelfFromGraveyardIntoLibraryEffect() implements CardEffect {
}
