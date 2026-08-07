package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: as long as the source's controller controls {@code minEnchantments} or more
 * enchantments, each OTHER non-Aura enchantment that player controls is a creature in addition to
 * its other types with base power and base toughness each equal to its mana value
 * (Starfield of Nyx).
 *
 * <p>Enchantment sibling of {@link AnimateNoncreatureArtifactsEffect}: the type change lands in
 * layer 4 and the mana-value base P/T in sublayer 7b, under one timestamp (CR 613.4). Unlike the
 * artifact version it does not skip permanents that are already creatures — an enchantment
 * creature you control still has its base P/T set to its mana value.
 *
 * <p>The enchantment-count threshold is a field rather than a {@link ConditionalEffect} wrapper
 * because the layered pass only admits conditional wrappers for layer 4; wrapping would drop the
 * sublayer-7b base P/T.
 *
 * @param minEnchantments how many enchantments the controller must control for the effect to apply
 */
public record AnimateControlledEnchantmentsEffect(int minEnchantments) implements CardEffect {
}
