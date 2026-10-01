package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Randomly seeks one card from the controller's library into their hand, constrained to have a
 * strictly greater or lesser mana value than the evaluated reference.
 */
public record SeekFromLibraryToHandWithRelativeManaValueEffect(
        DynamicAmount referenceManaValue, boolean greaterThan) implements CardEffect {

    public SeekFromLibraryToHandWithRelativeManaValueEffect {
        Objects.requireNonNull(referenceManaValue, "referenceManaValue");
    }
}
