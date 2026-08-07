package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Permanent scheduled to phase out when combat ends — "Whenever a creature you control attacks, it
 * phases out at end of combat." (Teferi's Veil). Scheduled per attacker by
 * {@code PhaseOutAttackingCreatureAtEndOfCombatEffect} and drained in
 * {@code CombatService.processEndOfCombatPhaseOuts()}; a permanent that already left the
 * battlefield is skipped.
 */
public record PhaseOutAtEndOfCombat(UUID permanentId) implements DelayedAction {
}
