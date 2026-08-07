package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker for Uba Mask: "If a player would draw a card, that player exiles that card face up
 * instead. Each player may play lands and cast spells from among cards they exiled with this
 * artifact this turn." Detected in {@code DrawService.resolveDrawCard} for every player's draw — the
 * exiled card gets an end-of-turn play permission for the player who would have drawn it.
 */
public record UbaMaskDrawReplacementEffect() implements CardEffect {
}
