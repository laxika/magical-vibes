package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot replacement for the affected player's next draw this turn: they exile the
 * top card of their library instead and may play it this turn (Urabrask, Heretic Praetor).
 *
 * <p>The affected player is the stack entry's {@code targetId}, which is the active player for an
 * {@code Triggers.upkeep(ActivePlayer.OPPONENT)} ability. Each registration is a floating effect on that
 * player that lasts until end of turn and is consumed by {@code DrawService}.
 */
public record RegisterNextDrawExileTopCardMayPlayThisTurnEffect() implements CardEffect {
}
