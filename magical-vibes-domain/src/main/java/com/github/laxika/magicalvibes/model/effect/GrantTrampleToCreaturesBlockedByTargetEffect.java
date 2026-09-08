package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Grants trample to the creatures currently blocked by the targeted blocking creature. */
public record GrantTrampleToCreaturesBlockedByTargetEffect() implements CardEffect, KeywordGrantingEffect {

    @Override
    public Set<Keyword> keywords() {
        return Set.of(Keyword.TRAMPLE);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.TARGET;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsBlockingPredicate());
    }
}
