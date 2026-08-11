package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement behavior that modifies how a face-down permanent is turned face up.
 *
 * <p>These effects are applied during the turn-face-up action and never go on the stack.
 */
public interface TurnFaceUpReplacementEffect extends ReplacementEffect {

    int counterCount();
}
