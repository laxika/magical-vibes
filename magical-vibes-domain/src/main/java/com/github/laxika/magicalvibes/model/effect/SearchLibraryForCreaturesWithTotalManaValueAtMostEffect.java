package com.github.laxika.magicalvibes.model.effect;

/** Searches the controller's library for any number of creatures with a bounded total mana value. */
public record SearchLibraryForCreaturesWithTotalManaValueAtMostEffect(int maxTotalManaValue)
        implements CardEffect {
}
