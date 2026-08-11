package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that adds one trigger when a permanent matching the given predicate enters the
 * battlefield. The optional controller restriction models effects such as Naban, Dean of Iteration.
 *
 * <p>Per CR 603.2c, if multiple copies of this effect are present on the controller's battlefield,
 * each adds one additional trigger (e.g. two Nabans → ability triggers three times total).</p>
 */
public record ETBDoubleTriggerEffect(CardPredicate predicate, boolean requiresEnteringControllerMatch)
        implements CardEffect {

    public ETBDoubleTriggerEffect(CardPredicate predicate) {
        this(predicate, true);
    }
}
