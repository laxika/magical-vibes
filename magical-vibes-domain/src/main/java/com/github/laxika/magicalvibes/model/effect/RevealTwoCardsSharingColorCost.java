package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that reveals two cards from the controller's hand that share a color with each other
 * (Illuminated Folio). The revealed cards stay in hand — the reveal only gates the ability, so
 * payment is resolved automatically against any qualifying pair (mirroring the auto-detected reveal
 * gate of {@link IncreaseOwnCastCostUnlessRevealSubtypeEffect}). The controller must have at least
 * two color-sharing cards in hand to activate; a colorless card shares no color and never qualifies.
 */
public record RevealTwoCardsSharingColorCost() implements CostEffect {
}
