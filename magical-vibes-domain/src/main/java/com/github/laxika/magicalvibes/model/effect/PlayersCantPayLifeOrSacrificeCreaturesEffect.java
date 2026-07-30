package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player may pay life or sacrifice a creature to cast a spell or activate an
 * ability (Angel of Jubilation). Only costs of spells and abilities are restricted — life payments
 * and creature sacrifices demanded by a resolving effect are unaffected.
 */
public record PlayersCantPayLifeOrSacrificeCreaturesEffect() implements CardEffect {
}
