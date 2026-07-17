package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Combat trigger: schedule the source permanent (the creature this effect is on) to have one
 * counter of {@code counterType} removed at end of combat. "At end of combat, if this creature
 * attacked or blocked this combat, remove a [type] counter from it." (e.g. Clockwork Beast). Put on
 * the ON_ATTACK and/or ON_BLOCK effect slot; the "attacked or blocked this combat" condition is
 * satisfied by only scheduling from those slots. Delaying to end of combat keeps the creature at
 * full power for the combat damage step. At resolution a delayed
 * {@link com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat} is queued
 * for the source permanent; it is drained in {@code CombatService.processEndOfCombatCounterRemovals()}.
 */
public record RemoveCounterFromSourceAtEndOfCombatEffect(CounterType counterType) implements CardEffect {
}
