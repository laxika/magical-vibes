package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record RemoveKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter) implements CardEffect {

    public RemoveKeywordEffect(Keyword keyword, GrantScope scope) {
        this(keyword, scope, null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (scope == GrantScope.TARGET) {
            return TargetSpec.benign(TargetCategory.PERMANENT);
        }
        if (scope == GrantScope.SELF) {
            return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
        }
        return TargetSpec.NONE;
    }
}
