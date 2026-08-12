package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns the source Aura attached to a legal creature, or returns it as a non-Aura enchantment
 * with a fixed-player upkeep damage ability when no legal creature exists.
 */
public record ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect(
        UUID enchantedCreatureControllerId
) implements CardEffect {

    public ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect() {
        this(null);
    }
}
