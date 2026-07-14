package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes a player skip their next combat phase (Blinding Angel, False Peace). Modelled by
 * incrementing a per-player counter ({@code GameData.skipNextCombatPhaseCount}) which the turn
 * engine reads when the affected player would leave their precombat main phase, and decrements as
 * each combat phase is skipped.
 *
 * <p>{@code targetsPlayer} distinguishes the two ways the affected player is chosen:
 * <ul>
 *   <li>{@code false} — as an {@code ON_COMBAT_DAMAGE_TO_PLAYER} trigger the affected player (the
 *   player dealt combat damage) is baked in as the stack entry's {@code targetId} (Blinding Angel).</li>
 *   <li>{@code true} — a targeted spell/ability where the caster chooses the affected player
 *   (False Peace: "Target player skips all combat phases of their next turn").</li>
 * </ul>
 */
public record SkipNextCombatPhaseEffect(boolean targetsPlayer)
        implements CombatDamageTriggerContextEffect {

    /** Non-targeting form (the affected player is baked in as {@code targetId}). */
    public SkipNextCombatPhaseEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPlayer() {
        return targetsPlayer;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
