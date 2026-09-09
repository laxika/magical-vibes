package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static self-effect: the source gains the current activated abilities of each creature its
 * controller controls that matches the supplied predicate.
 */
public record GainActivatedAbilitiesOfOwnCreaturesEffect(PermanentPredicate filter)
        implements CardEffect {
}
