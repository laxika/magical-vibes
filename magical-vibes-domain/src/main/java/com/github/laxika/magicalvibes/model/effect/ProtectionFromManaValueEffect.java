package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants protection from all sources whose mana value is at least {@code minManaValue}
 * (e.g. Mistmeadow Skulk: "protection from mana value 3 or greater"). Static, permanent.
 * Checked by {@code GameQueryService.hasProtectionFromSourceManaValue()}.
 *
 * @param minManaValue the inclusive lower bound of protected mana values
 */
public record ProtectionFromManaValueEffect(int minManaValue) implements CardEffect {
}
