package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes a player skip their next combat phase (Blinding Angel). Modelled by incrementing a
 * per-player counter ({@code GameData.skipNextCombatPhaseCount}) which the turn engine reads
 * when the affected player would leave their precombat main phase, and decrements as each
 * combat phase is skipped.
 *
 * <p>Non-targeting: as an {@code ON_COMBAT_DAMAGE_TO_PLAYER} trigger the affected player (the
 * player dealt combat damage) is baked in as the stack entry's {@code targetId}.
 */
public record SkipNextCombatPhaseEffect() implements CardEffect {
}
