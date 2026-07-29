package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage equal to {@code xValue} on the stack entry to the enchanted creature's controller.
 * Used by Spiteful Shadows on {@code ON_ENCHANTED_CREATURE_DEALT_DAMAGE} (damage the enchanted creature
 * <em>was dealt</em>) and by Backfire on {@code ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU} (damage the
 * enchanted creature <em>dealt</em> to the aura's controller).
 *
 * <p>{@code auraIsSource} selects which permanent deals the damage. The default ({@code false}) makes the
 * enchanted creature the source ("it deals that much damage to its controller" — Spiteful Shadows);
 * {@code true} makes the Aura itself the source ("this Aura deals that much damage to that creature's
 * controller" — Binding Agony).
 */
public record EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect(boolean auraIsSource)
        implements CardEffect {

    public EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect() {
        this(false);
    }
}
