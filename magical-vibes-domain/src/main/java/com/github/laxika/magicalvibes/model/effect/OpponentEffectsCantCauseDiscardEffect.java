package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells and abilities the controller's opponents control can't cause the
 * controller to discard cards. Discards caused by the controller's own effects or paid as costs
 * are unaffected.
 */
public record OpponentEffectsCantCauseDiscardEffect() implements CardEffect {
}
