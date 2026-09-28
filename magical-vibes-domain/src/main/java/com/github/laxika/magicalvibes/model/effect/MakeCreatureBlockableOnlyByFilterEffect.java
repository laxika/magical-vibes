package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Gives the triggering creature a duration-scoped "can be blocked only by" restriction. */
public record MakeCreatureBlockableOnlyByFilterEffect(
        PermanentPredicate blockerPredicate,
        String allowedBlockersDescription,
        EffectDuration duration
) implements CardEffect {
}
