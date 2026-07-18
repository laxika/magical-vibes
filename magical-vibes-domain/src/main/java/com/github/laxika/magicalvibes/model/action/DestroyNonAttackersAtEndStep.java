package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Siren's Call: at the beginning of the next end step, destroy all non-Wall creatures the given
 * player controls that didn't attack this turn. Creatures that player didn't control continuously
 * since the beginning of the turn (summoning sick) are ignored.
 */
public record DestroyNonAttackersAtEndStep(UUID playerId) implements DelayedAction {
}
