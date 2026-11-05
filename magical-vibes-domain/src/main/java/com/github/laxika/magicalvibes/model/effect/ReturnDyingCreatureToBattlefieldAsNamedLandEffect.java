package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the dying creature's card, the stack entry's triggering card, to the battlefield tapped under the
 * ability controller's control, then makes it a land with the configured name and grants it a colorless mana
 * ability. The returned permanent retains its other characteristics and abilities.
 */
public record ReturnDyingCreatureToBattlefieldAsNamedLandEffect(String landName) implements CardEffect {
}
