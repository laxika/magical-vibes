package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot delayed trigger: "when you next activate an exhaust ability that isn't a
 * mana ability this turn, copy it. You may choose new targets for the copy."
 */
public record CopyNextExhaustAbilityThisTurnEffect() implements CardEffect {
}
