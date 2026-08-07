package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller "no maximum hand size" for {@code duration}.
 * Unlike {@link NoMaximumHandSizeEffect} (a static effect that lasts while its permanent is on the
 * battlefield), this is a one-shot spell effect that modifies the game rules for a player.
 */
public record GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration duration) implements CardEffect {
}
