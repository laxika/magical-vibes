package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect used in the may ability prompt for Sphinx Ambassador's "you may put it onto
 * the battlefield under your control" step. The actual card is stored in
 * {@code GameData.pendingSphinxAmbassadorChoice}.
 */
public record SphinxAmbassadorPutOnBattlefieldEffect() implements CardEffect {
}
