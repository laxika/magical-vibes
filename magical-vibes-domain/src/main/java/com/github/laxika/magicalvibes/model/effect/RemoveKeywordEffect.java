package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot or continuous keyword removal for permanents selected by {@code scope}.
 *
 * <p>Also a {@link CombatOpponentReferencingEffect}: with {@link GrantScope#TARGET} on
 * {@code ON_BLOCK} / {@code ON_BECOMES_BLOCKED} (PER_BLOCKER) the combat opponent is carried
 * as the trigger's non-targeting target (Talruum Champion).
 */
public record RemoveKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter,
                                  EffectDuration duration)
        implements CardEffect, CombatOpponentReferencingEffect {

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
    public boolean referencesCombatOpponent() {
        return scope == GrantScope.TARGET;
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
