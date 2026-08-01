package com.github.laxika.magicalvibes.model.effect;

/**
 * Defending player activates a mana ability of each land they control and loses all unspent mana;
 * the controller schedules an equal amount of {@code {C}} at the beginning of their next main phase
 * this turn. Reads the defending player off the stack entry's {@code targetId}.
 *
 * <p>Designed for {@code ON_ATTACKS_UNBLOCKED} (Pygmy Hippo). Wrap in a {@link MayEffect} for
 * "you may", and pair with {@link AssignNoCombatDamageEffect} inside a {@link SequenceEffect} for
 * the "if you do, this creature assigns no combat damage this turn" rider. Contrast
 * {@link DrainTargetPlayersLandManaEffect}, which adds the lost mana immediately in the same
 * colors.</p>
 */
public record DrainDefendingPlayerLandManaDelayedColorlessEffect() implements CardEffect {
}
