package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that doubles triggered abilities caused by a creature matching the given
 * predicate entering the battlefield under the same controller (e.g. Naban, Dean of Iteration
 * with {@code CardSubtypePredicate(WIZARD)}).
 *
 * <p>Per CR 603.2c, if multiple copies of this effect are present on the controller's battlefield,
 * each adds one additional trigger (e.g. two Nabans → ability triggers three times total).</p>
 */
public record ETBDoubleTriggerEffect(CardPredicate predicate) implements CardEffect {
}
