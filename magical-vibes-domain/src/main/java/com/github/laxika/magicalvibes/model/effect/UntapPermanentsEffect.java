package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untaps permanent(s) as an effect. The {@link TapUntapScope} selects which permanent(s) are
 * affected; the optional {@link PermanentPredicate} narrows the scanned scopes
 * ({@link TapUntapScope#CONTROLLED}, {@link TapUntapScope#OTHER_CONTROLLED_CREATURES}) or, for
 * {@link TapUntapScope#TARGET}, restricts what can be targeted (exposed via
 * {@link #targetPredicate()}).
 *
 * <p>Replaces the former {@code UntapTargetPermanentEffect}, {@code UntapAllTargetPermanentsEffect},
 * {@code UntapSelfEffect}, {@code UntapAllControlledPermanentsEffect},
 * {@code UntapEachOtherCreatureYouControlEffect} and {@code UntapAttackedCreaturesEffect}.
 *
 * @param scope  which permanent(s) to untap
 * @param filter optional predicate narrowing the scanned scopes, or the targeting restriction for
 *               {@link TapUntapScope#TARGET} (null = no restriction)
 */
public record UntapPermanentsEffect(TapUntapScope scope, PermanentPredicate filter) implements CardEffect {

    public UntapPermanentsEffect(TapUntapScope scope) {
        this(scope, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return scope == TapUntapScope.TARGET || scope == TapUntapScope.ALL_TARGETS;
    }

    @Override
    public boolean isSelfTargeting() {
        return scope == TapUntapScope.SELF;
    }

    @Override
    public PermanentPredicate targetPredicate() {
        return scope == TapUntapScope.TARGET ? filter : null;
    }
}
