package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for an instant card with mana value 3, reveals it, and puts it
 * into their hand; then repeats for mana values 2 and 1. The library is shuffled once after every
 * mana value has been searched. A mana value with no matching instant in the library is skipped, and
 * the controller may always fail to find. Used by Firemind's Foresight.
 */
public record SearchLibraryForInstantsWithManaValuesToHandEffect() implements CardEffect {
}
