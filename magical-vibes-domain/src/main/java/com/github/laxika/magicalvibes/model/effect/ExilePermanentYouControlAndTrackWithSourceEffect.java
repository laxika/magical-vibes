package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Has the controller choose one matching permanent they control to exile and track with the
 * source permanent until the source leaves the battlefield.
 *
 * @param filter permanents that may be chosen
 */
public record ExilePermanentYouControlAndTrackWithSourceEffect(PermanentPredicate filter)
        implements CardEffect {
}
