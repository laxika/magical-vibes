package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player may pay life or sacrifice a nonland permanent to cast a spell or
 * activate an ability (Yasharn, Implacable Earth). Only costs of spells and abilities are
 * restricted; life payments and permanent sacrifices demanded by a resolving effect are
 * unaffected.
 */
public record PlayersCantPayLifeOrSacrificeNonlandPermanentsEffect() implements CardEffect {
}
