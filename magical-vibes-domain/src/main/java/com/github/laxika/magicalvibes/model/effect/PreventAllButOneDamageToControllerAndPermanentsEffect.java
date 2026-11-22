package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static prevention: "If a source would deal damage to you or a [permanent] you control, prevent all but 1 of that
 * damage" (Ajani Steadfast's emblem: planeswalkers; Hyperion, Supreme Hero: Heroes). Each damage event to its
 * controller, or to a permanent they control matching {@code permanents}, is reduced to at most 1.
 *
 * @param permanents the permanents of its controller that it protects besides the controller
 */
public record PreventAllButOneDamageToControllerAndPermanentsEffect(PermanentPredicate permanents)
        implements CardEffect {
}
