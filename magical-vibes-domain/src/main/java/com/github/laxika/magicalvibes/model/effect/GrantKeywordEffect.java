package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

public record GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope, PermanentPredicate filter,
                                 GrantDuration duration) implements CardEffect {

    public GrantKeywordEffect(Keyword keyword, GrantScope scope) {
        this(Set.of(keyword), scope, null, GrantDuration.END_OF_TURN);
    }

    public GrantKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter) {
        this(Set.of(keyword), scope, filter, GrantDuration.END_OF_TURN);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope) {
        this(keywords, scope, null, GrantDuration.END_OF_TURN);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope, PermanentPredicate filter) {
        this(keywords, scope, filter, GrantDuration.END_OF_TURN);
    }

    public GrantKeywordEffect(Keyword keyword, GrantScope scope, GrantDuration duration) {
        this(Set.of(keyword), scope, null, duration);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope, GrantDuration duration) {
        this(keywords, scope, null, duration);
    }

    @Override public boolean canTargetPermanent() { return scope == GrantScope.TARGET; }

    @Override public boolean isSelfTargeting() { return scope == GrantScope.SELF; }

    @Override public PermanentPredicate targetPredicate() { return scope == GrantScope.TARGET ? filter : null; }
}
