package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player shuffles their hand and graveyard — and, when {@code includeOwnedPermanents} is set,
 * every permanent they own across all battlefields — into their library.
 *
 * <p>The no-argument form is the Timetwister family (Time Reversal, Diminishing Returns, Memory,
 * Jace, the Living Guildpact). {@code new EachPlayerShufflesZonesIntoLibraryEffect(true)} adds the
 * permanent sweep, which is Sway of the Stars. Pair either with an
 * {@link EachPlayerDrawsCardEffect} for the "then draws seven cards" half.
 *
 * @param includeOwnedPermanents whether owned permanents are shuffled in along with the two zones
 */
public record EachPlayerShufflesZonesIntoLibraryEffect(boolean includeOwnedPermanents) implements CardEffect {

    /** Hand and graveyard only (Timetwister family). */
    public EachPlayerShufflesZonesIntoLibraryEffect() {
        this(false);
    }
}
