package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import java.util.UUID;

/**
 * Siren's Call: at the beginning of the next end step, destroy all non-Wall creatures the given
 * player controls that didn't attack this turn. Creatures that player didn't control continuously
 * since the beginning of the turn (summoning sick) are ignored. The delayed trigger is controlled by
 * {@code controllerId}, the player who controlled the spell or ability that created it.
 */
public record DestroyNonAttackersAtEndStep(UUID playerId, Card sourceCard,
                                          boolean excludeWallsFromAttack,
                                          boolean excludeSummoningSickFromDestruction,
                                          UUID controllerId) implements DelayedAction {
}
