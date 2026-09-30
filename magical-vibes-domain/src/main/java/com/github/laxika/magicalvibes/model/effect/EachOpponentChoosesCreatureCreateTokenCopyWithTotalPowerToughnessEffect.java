package com.github.laxika.magicalvibes.model.effect;

/**
 * For each opponent, choose one creature that player controls, then create one modified token
 * copy of one of the chosen creatures using the chosen creatures' total power and toughness.
 */
public record EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect()
        implements CardEffect {
}
