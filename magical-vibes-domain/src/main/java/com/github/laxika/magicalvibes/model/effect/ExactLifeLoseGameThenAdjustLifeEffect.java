package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player with exactly {@code exactLife} life loses the game, then each player gains/loses
 * {@code |lifeDelta|} life." The lose-game check resolves first (simultaneous): if every remaining
 * player loses, the game is a draw; if exactly one loses, that player loses and the life adjustment
 * is skipped (game already over). {@code lifeDelta} &gt; 0 = each player gains that much;
 * {@code lifeDelta} &lt; 0 = each player loses that much. Non-targeting. Triskaidekaphobia.
 */
public record ExactLifeLoseGameThenAdjustLifeEffect(int exactLife, int lifeDelta) implements CardEffect {
}
