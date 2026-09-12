package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for carnage or homage. A carnage
 * majority sacrifices the source artifact and destroys all nonland permanents; otherwise the
 * effect controller draws a card.
 */
public record CoercivePortalEffect() implements CardEffect {
}
