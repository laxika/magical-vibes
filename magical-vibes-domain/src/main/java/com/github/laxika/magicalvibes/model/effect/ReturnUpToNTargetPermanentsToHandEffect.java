package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Return up to {@code maxCount} permanents to their owners' hands." Modeled as a
 * resolution-time multi-select over matching permanents on the battlefield (the controller may
 * choose up to {@code maxCount}) rather than a targeted spell, so it can ride on non-targeting
 * resolution paths such as a cycling trigger (Resounding Wave). Resolved by
 * {@code ReturnUpToNTargetPermanentsToHandEffectHandler}, completed via
 * {@code MultiPermanentChoiceContext.ReturnTargetPermanentsToHand}.
 *
 * @param maxCount  maximum number of permanents to return
 * @param filter    optional source-relative filter for the permanents offered to the chooser
 * @param thenEffect optional effect inserted after the choice, but only if a permanent was
 *                   actually returned
 */
public record ReturnUpToNTargetPermanentsToHandEffect(int maxCount, PermanentPredicate filter,
                                                       CardEffect thenEffect) implements CardEffect {

    public ReturnUpToNTargetPermanentsToHandEffect(int maxCount) {
        this(maxCount, null, null);
    }
}
