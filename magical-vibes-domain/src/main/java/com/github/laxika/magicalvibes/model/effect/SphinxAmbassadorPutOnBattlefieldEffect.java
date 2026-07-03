package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect used in the may ability prompt for Sphinx Ambassador's "you may put it onto
 * the battlefield under your control" step. The actual card is stored in
 * the queued {@code PendingSphinxAmbassadorChoice} interaction.
 */
public record SphinxAmbassadorPutOnBattlefieldEffect() implements CardEffect {
}
