package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes all creatures controlled by the target player (or the controller of the targeted planeswalker)
 * unable to block this turn. Uses the shared target from the stack entry — does not add its own targeting.
 */
public record TargetPlayerCreaturesCantBlockThisTurnEffect() implements CardEffect {
}
