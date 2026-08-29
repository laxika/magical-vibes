package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule every permanent created earlier in this same resolution for exile at the controller's
 * next end step.
 */
public record ExileCreatedPermanentsAtControllerEndStepEffect() implements CardEffect {
}
