package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

public record GrantKeywordEffect(Keyword keyword, GrantScope scope) implements CardEffect {
    @Override public boolean canTargetPermanent() { return scope == GrantScope.TARGET; }
}
