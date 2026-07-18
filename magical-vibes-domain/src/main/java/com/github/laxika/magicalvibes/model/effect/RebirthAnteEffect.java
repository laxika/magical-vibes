package com.github.laxika.magicalvibes.model.effect;

/**
 * Rebirth: "Each player may ante the top card of their library. If a player does, that player's
 * life total becomes 20."
 *
 * <p>Resolution seeds one accept/decline {@code PendingMayAbility} per player (APNAP order, active
 * player first); the accept branch antes that player's top library card and sets their life to 20.
 * Ante is not a real zone in this engine, so — as with {@link TempestEfreetAnteExchangeEffect} — a
 * player who antes has the top card of their library removed from the game (modelled as the single
 * observable zone movement of moving it to exile).
 *
 * <p>Non-targeting; both the seeding resolution ({@code normalfx}) and the per-player accept branch
 * ({@code mayfx}) are keyed off this one effect type.
 */
public record RebirthAnteEffect() implements CardEffect {
}
