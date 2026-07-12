package com.github.laxika.magicalvibes.model.effect;

/**
 * Each of the controller's opponents loses life equal to the life that opponent has lost so far this
 * turn (read per-opponent from {@code GameData.lifeLostThisTurn}, which counts damage as loss of life).
 * Used by Wound Reflection as an end-step triggered ability. Life loss is applied through the shared
 * life-loss primitive and feeds "loses life" triggers, never through damage plumbing (CR 118.2).
 */
public record EachOpponentLosesLifeEqualToLifeLostThisTurn() implements CardEffect {
}
