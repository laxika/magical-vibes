package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the source permanent to be returned to its owner's hand at the beginning of the next
 * end step. Operates on the source, so it carries no target.
 */
public record ReturnSelfToHandAtEndStepEffect() implements CardEffect {
}
