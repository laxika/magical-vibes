package com.github.laxika.magicalvibes.model.effect;

/**
 * Each enchantment on the battlefield deals {@code damage} damage to its controller, then each Aura
 * attached to a creature deals {@code damage} damage to the creature it's attached to (Aura Barbs).
 *
 * <p>Non-targeting and symmetric. Every enchantment is its own damage source, so a player controlling
 * three enchantments takes {@code 3 * damage}, and an Aura attached to a creature does both halves —
 * it damages its controller and then the creature it enchants. Like the other mass-damage effects it
 * deliberately does not implement {@link DamageDealingEffect}, which models a single amount aimed at
 * one target category.</p>
 */
public record EachEnchantmentDealsDamageToControllerThenEachAuraToEnchantedCreatureEffect(int damage)
        implements CardEffect {
}
