package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells and abilities the controller's opponents control can't cause the controller
 * to sacrifice permanents (Sigarda, Host of Herons). Only sacrifices demanded by an opponent's
 * resolving spell or ability are restricted — the controller's own effects, and sacrifices the
 * controller chooses to make as a cost, are unaffected.
 */
public record OpponentEffectsCantCauseSacrificeEffect() implements CardEffect {
}
