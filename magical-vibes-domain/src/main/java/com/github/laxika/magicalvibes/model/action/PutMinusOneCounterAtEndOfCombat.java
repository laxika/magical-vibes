package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Permanent scheduled to receive {@code amount} -1/-1 counters when combat ends (e.g. Wicker
 * Warcrawler's "Whenever this creature attacks or blocks, put a -1/-1 counter on it at end of
 * combat"). Delaying to end of combat keeps the creature at its full toughness for the combat
 * damage step. Drained in {@code CombatService.processEndOfCombatSourceCounters()}.
 */
public record PutMinusOneCounterAtEndOfCombat(UUID permanentId, int amount) implements DelayedAction {
}
