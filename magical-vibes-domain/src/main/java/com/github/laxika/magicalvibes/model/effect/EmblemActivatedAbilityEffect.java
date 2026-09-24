package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * An activated ability granted directly to the controller of an emblem.
 *
 * <p>The ability is kept as an emblem payload, alongside the existing emblem
 * trigger and static-effect markers.</p>
 */
public record EmblemActivatedAbilityEffect(ActivatedAbility ability) implements CardEffect {
}
