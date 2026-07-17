package com.github.laxika.magicalvibes.model.effect;

/**
 * "Return up to {@code maxCount} target permanents to their owners' hands." Modeled as a
 * resolution-time multi-select over every permanent on the battlefield (the controller may
 * choose up to {@code maxCount}) rather than a targeted spell, so it can ride on non-targeting
 * resolution paths such as a cycling trigger (Resounding Wave). Resolved by
 * {@code ReturnUpToNTargetPermanentsToHandEffectHandler}, completed via
 * {@code MultiPermanentChoiceContext.ReturnTargetPermanentsToHand}.
 */
public record ReturnUpToNTargetPermanentsToHandEffect(int maxCount) implements CardEffect {
}
