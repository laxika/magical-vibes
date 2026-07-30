package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Creature scheduled to be tapped when combat ends and to skip its controller's next untap step —
 * "At end of combat, tap all creatures that blocked this creature this turn. They don't untap during
 * their controller's next untap step." (Joven's Ferrets). Scheduled per blocker by
 * {@code TapCombatOpponentAtEndOfCombatEffect} and drained in
 * {@code CombatService.processEndOfCombatTaps()}; a permanent that already left the battlefield is
 * skipped.
 */
public record TapAndSkipUntapAtEndOfCombat(UUID permanentId) implements DelayedAction {
}
