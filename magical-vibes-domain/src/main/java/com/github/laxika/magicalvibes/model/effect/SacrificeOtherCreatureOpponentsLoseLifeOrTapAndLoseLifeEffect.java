package com.github.laxika.magicalvibes.model.effect;

/**
 * At the beginning of your upkeep, sacrifice a creature other than this creature,
 * then each opponent loses life equal to the sacrificed creature's power.
 * If you can't sacrifice a creature, tap this creature and you lose {@code lifeLoss} life.
 */
public record SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect(int lifeLoss) implements CardEffect {
}
