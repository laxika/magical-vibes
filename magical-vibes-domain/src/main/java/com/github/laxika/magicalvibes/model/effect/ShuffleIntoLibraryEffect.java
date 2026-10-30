package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles the resolving card into its owner's library. When {@code transformed} is set the card goes in
 * with its back face showing (for example "shuffle this spell into its owner's library transformed").
 */
public record ShuffleIntoLibraryEffect(boolean transformed) implements CardEffect {

    public ShuffleIntoLibraryEffect() {
        this(false);
    }
}
