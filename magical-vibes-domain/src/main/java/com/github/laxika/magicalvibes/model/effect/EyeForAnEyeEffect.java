package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to you this turn, instead that source deals
 * that much damage to you and Eye for an Eye deals that much damage to that source's controller."
 * (Eye for an Eye). The source is chosen on resolution (any battlefield permanent). One-shot: only the
 * next damage event from that source is reflected, then the shield is consumed. The damage to the
 * controller is NOT reduced; an equal reflected damage event is dealt back at the source's controller.
 */
public record EyeForAnEyeEffect() implements CardEffect {
}
