package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record GrantKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter) implements CardEffect {

    public GrantKeywordEffect(Keyword keyword, GrantScope scope) {
        this(keyword, scope, null);
    }

    @Override public boolean canTargetPermanent() { return scope == GrantScope.TARGET; }
}
