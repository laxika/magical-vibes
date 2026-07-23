package com.github.laxika.magicalvibes.model.effect;

/**
 * Enduring Renewal: if you would draw a card, reveal the top card of your library instead.
 * If it's a creature card, put it into your graveyard. Otherwise, draw a card.
 */
public record EnduringRenewalDrawReplacementEffect()
        implements RevealTopCreatureToGraveyardElseDrawReplacementEffect {
}
