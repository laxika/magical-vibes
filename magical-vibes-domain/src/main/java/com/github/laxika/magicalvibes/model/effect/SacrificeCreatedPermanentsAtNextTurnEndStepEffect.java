package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule every permanent created earlier in this same resolution to be sacrificed at the
 * beginning of the end step on the resolving controller's next turn.
 */
public record SacrificeCreatedPermanentsAtNextTurnEndStepEffect() implements CardEffect {
}
