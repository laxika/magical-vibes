package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedules a non-targeting triggered ability to trigger at the beginning of the end-of-combat
 * step. The scheduled ability is independent of the source permanent remaining on the battlefield.
 * Its "that creature" is the scheduling ability's non-targeting permanent, the creature on the other side of a block
 * ("whenever this creature blocks or becomes blocked by an equipped creature, destroy all Equipment attached to that
 * creature at end of combat", Corrosive Ooze: {@code new DelayedEndOfCombatEffect(new
 * DestroyAttachmentsOnTargetCreatureEffect(false, true))}).
 */
public record DelayedEndOfCombatEffect(CardEffect effect) implements CardEffect {
}
