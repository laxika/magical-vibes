package com.github.laxika.magicalvibes.model.effect;

/**
 * The spell's target player exiles a card from their own hand, chosen by that player (Ruthless
 * Negotiation). Nothing is revealed to the caster. If the target's hand is empty, nothing happens.
 */
public record TargetPlayerExilesCardFromHandEffect() implements CardEffect {
}
