package com.github.laxika.magicalvibes.model.effect;

/** Searches the controller's library for one card, then applies a face-down exile/free-cast flow. */
public record SearchLibraryForCardToExileFaceDownAndMayCastOrPutIntoHandEffect(int maxManaValue)
        implements CardEffect {
}
