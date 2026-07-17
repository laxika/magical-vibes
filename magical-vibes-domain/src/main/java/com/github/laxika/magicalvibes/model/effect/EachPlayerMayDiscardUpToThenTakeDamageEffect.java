package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may discard up to {@code amount} cards, then is dealt damage equal to
 * {@code amount} minus the number of cards they discarded this way.
 * <p>
 * Resolves in APNAP order. Each player in turn chooses how many cards to discard
 * (0 through {@code min(amount, hand size)}) via an X-value choice, discards exactly that many
 * (their own selection), then is dealt {@code amount - discarded} damage, before the next
 * player takes their turn. A player who chooses (or is forced by an empty hand) to discard
 * nothing simply takes the full {@code amount}. Used by Mind Bomb ({@code amount} = 3).
 */
public record EachPlayerMayDiscardUpToThenTakeDamageEffect(int amount) implements CardEffect {
}
