package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * The creature the source Aura is attached to gets -X/-Y as a continuous layer-7c bonus
 * (CR 613.4c), where X is {@code amount} and Y is X capped so the toughness reduction can never
 * take the creature below 1 toughness ("Y is equal to X or to enchanted creature's toughness
 * minus 1, whichever is smaller" — Snowblind).
 *
 * <p>This is the toughness-floored sibling of {@link AttachedBoostEffect}, and it differs from it
 * in one more way that the wording of such cards demands: {@code amount} is evaluated with the
 * <em>enchanted creature</em> as the amount source and its controller as the amount controller, so
 * {@link com.github.laxika.magicalvibes.model.amount.IfSourceAttacking} reads the enchanted
 * creature's combat state, {@code DEFENDING_PLAYER} reads the player it is attacking, and
 * {@code CONTROLLER} reads "its controller" rather than the Aura's controller.
 */
public record ShrinkEnchantedCreatureEffect(DynamicAmount amount) implements CardEffect {
}
