package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for an ally-creature-death effect that filters graveyard cards by the dying
 * creature's mana value.
 */
public interface DyingCreatureManaValueAwareEffect {

    CardEffect snapshotDyingCreatureManaValue(int dyingCreatureManaValue);
}
