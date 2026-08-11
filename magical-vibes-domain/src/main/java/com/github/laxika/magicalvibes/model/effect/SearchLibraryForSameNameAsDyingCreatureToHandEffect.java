package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a card with the same name as the creature that died,
 * reveals it, and puts it into the controller's hand.
 *
 * <p>The no-argument constructor is used by the card definition. The death-trigger collector binds
 * the dying creature's name before the optional ability is queued, preserving last-known name
 * information even if the dead card leaves its graveyard before resolution.</p>
 *
 * @param dyingCreatureName the snapshotted name, or {@code null} in the card definition
 */
public record SearchLibraryForSameNameAsDyingCreatureToHandEffect(String dyingCreatureName)
        implements CardEffect, DyingCreatureNameAwareEffect {

    public SearchLibraryForSameNameAsDyingCreatureToHandEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCreatureName(String dyingCreatureName) {
        return new SearchLibraryForSameNameAsDyingCreatureToHandEffect(dyingCreatureName);
    }
}
