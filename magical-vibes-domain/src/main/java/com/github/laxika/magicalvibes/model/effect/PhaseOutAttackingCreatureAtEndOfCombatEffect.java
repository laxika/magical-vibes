package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a creature you control attacks, it phases out at end of combat." (Teferi's Veil).
 * Scheduled from an attack trigger slot, where the triggering attacker is stored as the stack
 * entry's non-targeting {@code targetId} — nothing is chosen, so this effect declares no
 * {@link TargetSpec}.
 *
 * <p>Resolution queues a {@link com.github.laxika.magicalvibes.model.action.PhaseOutAtEndOfCombat}
 * delayed action drained in {@code CombatService.processEndOfCombatPhaseOuts()}, so the creature is
 * still around to deal its combat damage. The phasing itself goes through {@code PhasingService},
 * which takes attachments along indirectly (CR 702.26g) and removes the permanent from combat
 * (CR 506.4); because it phased out directly it phases in during its controller's next untap step
 * even without the phasing keyword (CR 702.26a).
 *
 * <p>The immediate, non-delayed siblings are {@link PhaseOutEffect} and
 * {@link PhaseOutSelfAndCombatOpponentEffect}.
 */
public record PhaseOutAttackingCreatureAtEndOfCombatEffect() implements CardEffect {
}
