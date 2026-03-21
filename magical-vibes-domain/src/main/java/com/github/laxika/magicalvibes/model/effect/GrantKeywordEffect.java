package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

public record GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope, PermanentPredicate filter) implements CardEffect {

    public GrantKeywordEffect(Keyword keyword, GrantScope scope) {
        this(Set.of(keyword), scope, null);
    }

    public GrantKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter) {
        this(Set.of(keyword), scope, filter);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope) {
        this(keywords, scope, null);
    }

    @Override public boolean canTargetPermanent() { return scope == GrantScope.TARGET; }

    @Override public boolean isSelfTargeting() { return scope == GrantScope.SELF; }

    @Override public PermanentPredicate targetPredicate() { return scope == GrantScope.TARGET ? filter : null; }
}
