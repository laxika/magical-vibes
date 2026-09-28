package com.github.laxika.magicalvibes.model.effect;

import java.util.OptionalInt;

/**
 * Grants protection from all sources whose mana value is at most {@code maxManaValue}.
 *
 * @param maxManaValue the inclusive upper bound of protected mana values
 */
public record ProtectionFromManaValueAtMostEffect(int maxManaValue) implements ProtectionGrantingEffect {

    @Override
    public OptionalInt protectionFromManaValueAtMost() {
        return OptionalInt.of(maxManaValue);
    }
}
