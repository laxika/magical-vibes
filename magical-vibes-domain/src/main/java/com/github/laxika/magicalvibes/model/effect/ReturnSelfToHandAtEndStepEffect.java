package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the source permanent to be returned to its owner's hand at the beginning of the next
 * end step. The delayed action does nothing if the source has already left the battlefield.
 */
public record ReturnSelfToHandAtEndStepEffect() implements CardEffect {
}
