package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Trigger descriptor for a scheme being set in motion. */
public record SchemeSetInMotionTriggerEffect(
        CardPredicate schemeFilter,
        List<CardEffect> resolvedEffects
) implements CardEffect {

    public SchemeSetInMotionTriggerEffect(List<CardEffect> resolvedEffects) {
        this(null, resolvedEffects);
    }

    public SchemeSetInMotionTriggerEffect {
        resolvedEffects = List.copyOf(resolvedEffects);
    }
}
