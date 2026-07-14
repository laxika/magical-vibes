package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Grants keyword(s) to permanents selected by {@code scope} (and, for the multi-permanent
 * scopes, {@code filter}).
 *
 * <p>{@code grantCondition} is an OPTIONAL predicate checked against the selected permanent
 * <em>at resolution</em>: if set and the permanent does not match, the keyword grant is
 * skipped. It is NOT a targeting restriction — the target stays legal either way; only the
 * grant is conditional (e.g. Vampire's Zeal: "target creature gets +2/+2; if it's a Vampire,
 * it also gains first strike"). This is intentionally distinct from {@code filter}: {@code filter}
 * feeds {@link #targetPredicate()} (targeting restriction) for the TARGET scope and selects the
 * eligible permanents for the multi-permanent scopes, whereas {@code grantCondition} only gates
 * the keyword grant. Use {@link #toTargetIf(Keyword, PermanentPredicate)} to build one.
 */
public record GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope, PermanentPredicate filter,
                                 GrantDuration duration, PermanentPredicate grantCondition) implements KeywordGrantingEffect {

    public GrantKeywordEffect(Keyword keyword, GrantScope scope) {
        this(Set.of(keyword), scope, null, GrantDuration.END_OF_TURN, null);
    }

    public GrantKeywordEffect(Keyword keyword, GrantScope scope, PermanentPredicate filter) {
        this(Set.of(keyword), scope, filter, GrantDuration.END_OF_TURN, null);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope) {
        this(keywords, scope, null, GrantDuration.END_OF_TURN, null);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope, PermanentPredicate filter) {
        this(keywords, scope, filter, GrantDuration.END_OF_TURN, null);
    }

    public GrantKeywordEffect(Keyword keyword, GrantScope scope, GrantDuration duration) {
        this(Set.of(keyword), scope, null, duration, null);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, GrantScope scope, GrantDuration duration) {
        this(keywords, scope, null, duration, null);
    }

    /**
     * Grant {@code keyword} to the targeted permanent until end of turn, but only if the
     * target matches {@code grantCondition} at resolution. The target itself is unrestricted
     * (any legal target of the spell/ability); only the keyword grant is conditional.
     */
    public static GrantKeywordEffect toTargetIf(Keyword keyword, PermanentPredicate grantCondition) {
        return new GrantKeywordEffect(Set.of(keyword), GrantScope.TARGET, null, GrantDuration.END_OF_TURN, grantCondition);
    }

    @Override public boolean canTargetPermanent() { return scope == GrantScope.TARGET; }

    @Override public boolean canTargetPlayer() { return scope == GrantScope.TARGET_PLAYERS_CREATURES; }

    @Override public boolean isSelfTargeting() { return scope == GrantScope.SELF; }

    @Override public PermanentPredicate targetPredicate() { return scope == GrantScope.TARGET ? filter : null; }
}
