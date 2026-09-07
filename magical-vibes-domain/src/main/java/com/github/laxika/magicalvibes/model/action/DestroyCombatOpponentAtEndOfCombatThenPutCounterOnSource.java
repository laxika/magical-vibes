package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Schedules combat-opponent destruction and remembers the creature that receives the successful
 * destruction rider at the next end step (Infinite Authority).
 */
public record DestroyCombatOpponentAtEndOfCombatThenPutCounterOnSource(
        UUID opponentId,
        UUID sourcePermanentId,
        UUID controllerId,
        Card sourceCard,
        boolean cannotBeRegenerated
) implements DelayedAction {
}
