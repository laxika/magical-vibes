package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * On resolution, prompts for a basic land type and grants the matching landwalk until end of
 * turn to the permanent selected by {@code scope}.
 */
public record GrantChosenLandwalkEffect(GrantScope scope) implements KeywordGrantingEffect {

    public GrantChosenLandwalkEffect {
        if (scope != GrantScope.SELF && scope != GrantScope.TARGET) {
            throw new IllegalArgumentException("GrantChosenLandwalkEffect supports only SELF and TARGET, got " + scope);
        }
    }

    @Override
    public Set<Keyword> keywords() {
        return Keyword.LANDWALK_MAP.keySet();
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }
}
