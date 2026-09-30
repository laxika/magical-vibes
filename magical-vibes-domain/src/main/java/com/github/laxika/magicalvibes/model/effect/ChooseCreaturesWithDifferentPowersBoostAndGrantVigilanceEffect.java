package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Chooses any number of creatures with different effective powers, then gives each of them
 * +X/+X and vigilance until end of turn, where X is the source creature's power.
 */
public record ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffect()
        implements KeywordGrantingEffect {

    @Override
    public Set<Keyword> keywords() {
        return Set.of(Keyword.VIGILANCE);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGETS;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
