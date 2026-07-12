package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the controller, each player may pay any amount of life; repeat this process
 * until no one pays life; then each player creates one {@code token} for each 1 life they paid.
 * <p>
 * Resolves round-robin in turn order starting with the controller. Each pass a player chooses an
 * amount of life to pay (0 through their current life total) via an X-value choice. The process
 * repeats until a full round elapses in which no player pays life (per the official ruling, it
 * does not end the first time a single player declines). Used by Plague of Vermin.
 *
 * @param token a single-token template; each player creates as many copies as the life they paid
 */
public record EachPlayerPaysAnyLifeForTokensEffect(CreateTokenEffect token) implements CardEffect {
}
