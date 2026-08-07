package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;

/**
 * On resolution, prompts the controller to choose one keyword from {@code options}, then grants
 * that keyword until end of turn to the permanent selected by {@code scope}.
 * <p>
 * {@link GrantScope#SELF} is the non-targeted "this creature gains your choice of …" shape
 * (Urza's Avenger, Illusionary Presence) and resolves against the ability's source permanent.
 * {@link GrantScope#TARGET} is "target creature gains your choice of …" (Golem Artisan,
 * Practiced Offense). No other scope is supported.
 */
public record GrantChosenKeywordEffect(List<Keyword> options, GrantScope scope) implements CardEffect {

    public GrantChosenKeywordEffect {
        if (scope != GrantScope.SELF && scope != GrantScope.TARGET) {
            throw new IllegalArgumentException("GrantChosenKeywordEffect supports only SELF and TARGET, got " + scope);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }
}
