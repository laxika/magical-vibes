package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller "no maximum hand size" for the rest of the game.
 * Unlike {@link NoMaximumHandSizeEffect} (a static effect on a permanent),
 * this is a one-shot spell effect that permanently modifies the game rules for a player.
 */
public record GrantPermanentNoMaxHandSizeEffect() implements CardEffect {
}
