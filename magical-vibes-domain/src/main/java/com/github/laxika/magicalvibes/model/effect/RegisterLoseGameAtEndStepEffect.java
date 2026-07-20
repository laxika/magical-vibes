package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed triggered ability: "at the beginning of your next end step, you lose the game"
 * (Last Chance, Glorious End). The delayed action skips the scheduling turn's own end step and every
 * opponent's end step, firing at the beginning of the resolving controller's own next end step. For
 * the extra-turn cards that is the extra turn's end step; for Glorious End (which ends the turn) it is
 * the controller's next turn's end step. Non-targeting.
 */
public record RegisterLoseGameAtEndStepEffect() implements CardEffect {
}
