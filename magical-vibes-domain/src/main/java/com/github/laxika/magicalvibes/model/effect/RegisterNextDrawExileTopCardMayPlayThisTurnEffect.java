package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot replacement for the affected player's next draw this turn: they exile the
 * top card of their library instead and may play it this turn (Urabrask, Heretic Praetor).
 *
 * <p>The affected player is the stack entry's {@code targetId}, which is the active player for an
 * {@code OPPONENT_UPKEEP_TRIGGERED} ability. Registrations are counted in
 * {@code GameData.pendingNextDrawExileTopCard} and consumed by {@code DrawService}.
 */
public record RegisterNextDrawExileTopCardMayPlayThisTurnEffect() implements CardEffect {
}
