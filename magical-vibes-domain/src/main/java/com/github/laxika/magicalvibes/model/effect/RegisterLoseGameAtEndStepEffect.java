package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed triggered ability: at the beginning of the next turn's end step, the resolving
 * controller loses the game (Last Chance). The delayed action skips the current turn's own end step
 * so it fires at the extra turn's end step. Non-targeting.
 */
public record RegisterLoseGameAtEndStepEffect() implements CardEffect {
}
