package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger that makes {@code playerId} lose the game at the beginning of an end step
 * <em>after</em> the turn it was scheduled on (e.g. Last Chance: "Take an extra turn after this one.
 * At the beginning of that turn's end step, you lose the game.").
 *
 * <p>{@code registeredTurnNumber} is the turn number when the spell resolved. The action is drained
 * only once {@code gameData.turnNumber} exceeds it, so it skips the current turn's own end step and
 * fires at the following (extra) turn's end step.
 */
public record LoseGameAtEndStep(UUID playerId, Card sourceCard, int registeredTurnNumber) implements DelayedAction {
}
