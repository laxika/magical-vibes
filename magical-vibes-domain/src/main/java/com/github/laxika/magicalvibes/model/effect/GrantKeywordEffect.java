package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Grants keyword(s) to permanents selected by {@code subject} (and, for the multi-permanent
 * scopes, {@code filter}).
 *
 * <p>{@code grantCondition} is an OPTIONAL predicate checked against the selected permanent
 * <em>at resolution</em>: if set and the permanent does not match, the keyword grant is
 * skipped. It is NOT a targeting restriction — the target stays legal either way; only the
 * grant is conditional (e.g. Vampire's Zeal: "target creature gets +2/+2; if it's a Vampire,
 * it also gains first strike"). This is intentionally distinct from {@code filter}: {@code filter}
 * feeds the {@link #targetSpec()} predicate (targeting restriction) for the TARGET subject and selects the
 * eligible permanents for the multi-permanent scopes, whereas {@code grantCondition} only gates
 * the keyword grant. Use {@link #toTargetIf(Keyword, PermanentPredicate)} to build one.
 */
public record GrantKeywordEffect(Set<Keyword> keywords, PermanentSubject subject, PermanentPredicate filter,
                                 Duration duration, PermanentPredicate grantCondition)
        implements KeywordGrantingEffect {

    public GrantKeywordEffect(Keyword keyword, PermanentSubject subject) {
        this(Set.of(keyword), subject, null, Duration.UNTIL_END_OF_TURN, null);
    }

    public GrantKeywordEffect(Keyword keyword, PermanentSubject subject, PermanentPredicate filter) {
        this(Set.of(keyword), subject, filter, Duration.UNTIL_END_OF_TURN, null);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, PermanentSubject subject) {
        this(keywords, subject, null, Duration.UNTIL_END_OF_TURN, null);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, PermanentSubject subject, PermanentPredicate filter) {
        this(keywords, subject, filter, Duration.UNTIL_END_OF_TURN, null);
    }

    public GrantKeywordEffect(Keyword keyword, PermanentSubject subject, Duration duration) {
        this(Set.of(keyword), subject, null, duration, null);
    }

    public GrantKeywordEffect(Set<Keyword> keywords, PermanentSubject subject, Duration duration) {
        this(keywords, subject, null, duration, null);
    }

    /**
     * Grant {@code keyword} to the targeted permanent until end of turn, but only if the
     * target matches {@code grantCondition} at resolution. The target itself is unrestricted
     * (any legal target of the spell/ability); only the keyword grant is conditional.
     */
    public static GrantKeywordEffect toTargetIf(Keyword keyword, PermanentPredicate grantCondition) {
        return new GrantKeywordEffect(Set.of(keyword), Subjects.target(), null, Duration.UNTIL_END_OF_TURN, grantCondition);
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.AttachedAndSharingCreatureType;
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            case PermanentSubject.WithCreaturesSharingAColor sharing when sharing.declaresTarget() ->
                    TargetSpec.benign(TargetPredicates.permanent(), filter);
            case PermanentSubject.ControlledBy set when set.declaresPlayerTarget() ->
                    TargetSpec.benign(TargetPredicates.player());
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
