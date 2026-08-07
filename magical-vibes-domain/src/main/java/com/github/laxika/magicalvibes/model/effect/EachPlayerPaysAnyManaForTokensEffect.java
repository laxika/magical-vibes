package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may pay any amount of mana; then each player creates a number of {@code token}
 * copies equal to the amount of mana they paid this way.
 * <p>
 * Single pass, not round-robin: every player is prompted exactly once, in APNAP order (CR 101.4 —
 * the active player chooses first, then the remaining players in turn order). Each choice is an
 * X-value mana-payment prompt capped by that player's potential mana, so a player may tap sources
 * while the prompt is open. Used by Liege of the Hollows.
 *
 * @param token a single-token template; each player creates as many copies as the mana they paid
 */
public record EachPlayerPaysAnyManaForTokensEffect(CreateTokenEffect token) implements CardEffect {
}
