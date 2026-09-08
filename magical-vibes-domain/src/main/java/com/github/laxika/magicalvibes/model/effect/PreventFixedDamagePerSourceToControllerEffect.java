package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Static effect: "If a source would deal damage to you, prevent N of that damage." (Urza's Armor)
 *
 * <p>When either source restriction is set, the prevention applies only to sources matching that
 * restriction. This covers "If a creature would deal damage to you" (Orbs of Warding) and
 * "If an artifact would deal damage to you" (Sphere of Purity).
 *
 * <p>Modeled per damage event on the controller of the permanent carrying this effect: up to
 * {@code amount} is prevented from each source that would deal damage to them. Optional flags
 * restrict the effect to combat damage and/or untapped source permanents. Applied in
 * {@code DamageSupport.dealDamageToPlayer} and {@code CombatDamageService.accumulatePlayerDamage}
 * via {@code DamagePreventionService.applyControllerFixedPerSourceDamagePrevention}.
 */
public record PreventFixedDamagePerSourceToControllerEffect(
        int amount,
        boolean creatureSourcesOnly,
        boolean artifactSourcesOnly,
        Set<CardColor> sourceColors,
        boolean combatOnly,
        boolean requiresUntappedSource
) implements CardEffect {

    public PreventFixedDamagePerSourceToControllerEffect(int amount, boolean creatureSourcesOnly) {
        this(amount, creatureSourcesOnly, false, null, false, false);
    }

    public PreventFixedDamagePerSourceToControllerEffect(int amount, boolean creatureSourcesOnly,
                                                         boolean artifactSourcesOnly,
                                                         Set<CardColor> sourceColors) {
        this(amount, creatureSourcesOnly, artifactSourcesOnly, sourceColors, false, false);
    }

    /** "If a source would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromAnySource(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, false, false, null, false, false);
    }

    /** "If a creature would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromCreatures(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, true, false, null, false, false);
    }

    /** "If a creature would deal [combat] damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromCreatures(
            int amount, boolean combatOnly, boolean requiresUntappedSource) {
        return new PreventFixedDamagePerSourceToControllerEffect(
                amount, true, false, null, combatOnly, requiresUntappedSource);
    }

    /** "If an artifact would deal damage to you, prevent {@code amount} of that damage." */
    public static PreventFixedDamagePerSourceToControllerEffect fromArtifacts(int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, false, true, null, false, false);
    }

    /** "If a source of one of these colors would deal damage to you, prevent {@code amount}." */
    public static PreventFixedDamagePerSourceToControllerEffect fromColors(Set<CardColor> colors, int amount) {
        return new PreventFixedDamagePerSourceToControllerEffect(amount, false, false, colors, false, false);
    }
}
