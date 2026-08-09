package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a source would deal damage to you, prevent N of that damage." (Urza's Armor)
 *
 * <p>When either source restriction is set, the prevention applies only to sources matching that
 * restriction. This covers "If a creature would deal damage to you" (Orbs of Warding) and
 * "If an artifact would deal damage to you" (Sphere of Purity).
 *
 * <p>Modeled per damage event on the controller of the permanent carrying this effect: up to
 * {@code amount} is prevented from each source that would deal damage to them. Applies to both
 * combat and noncombat damage. Applied in {@code DamageSupport.dealDamageToPlayer} (noncombat)
 * and {@code CombatDamageService.accumulatePlayerDamage} (combat, per attacker) via
 * {@code DamagePreventionService.applyControllerFixedPerSourceDamagePrevention}.
 */
public record PreventFixedDamagePerSourceToControllerEffect(
        int amount,
        boolean creatureSourcesOnly,
        boolean artifactSourcesOnly
) implements CardEffect {

    public PreventFixedDamagePerSourceToControllerEffect(int amount, boolean creatureSourcesOnly) {
        this(amount, creatureSourcesOnly, false);
    }

    /** "If a source would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromAnySource(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, false, false);
    }

    /** "If a creature would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromCreatures(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, true, false);
    }

    /** "If an artifact would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromArtifacts(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, false, true);
    }
}
