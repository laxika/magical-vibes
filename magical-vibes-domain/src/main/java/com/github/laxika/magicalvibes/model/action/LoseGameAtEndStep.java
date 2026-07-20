package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger that makes {@code playerId} lose the game at the beginning of that player's own
 * next end step (e.g. Last Chance: "Take an extra turn after this one. At the beginning of that turn's
 * end step, you lose the game."; Glorious End: "End the turn. At the beginning of your next end step,
 * you lose the game.").
 *
 * <p>{@code registeredTurnNumber} is the turn number when the spell resolved. The action is drained
 * only once {@code gameData.turnNumber} exceeds it <em>and</em> {@code playerId} is the active player,
 * so it skips both the scheduling turn's own end step and every opponent's end step, firing at the
 * scheduling player's own next end step ("your next end step").
 */
public record LoseGameAtEndStep(UUID playerId, Card sourceCard, int registeredTurnNumber) implements DelayedAction {
}
