package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record RemoveKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter,
                                  EffectDuration duration) implements CardEffect {

    public RemoveKeywordEffect(Keyword keyword, GrantScope scope) {
        this(keyword, scope, null, EffectDuration.UNTIL_END_OF_TURN);
    }

    public RemoveKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter) {
        this(keyword, scope, filter, EffectDuration.UNTIL_END_OF_TURN);
    }

    public RemoveKeywordEffect(Keyword keyword, GrantScope scope, EffectDuration duration) {
        this(keyword, scope, null, duration);
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
