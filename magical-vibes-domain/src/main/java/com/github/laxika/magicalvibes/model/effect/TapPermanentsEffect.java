package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Taps permanent(s) as an effect (fires "becomes tapped" triggers). The {@link TapUntapScope}
 * selects which permanent(s) are affected; the optional {@link PermanentPredicate} narrows the
 * scanned scopes ({@link TapUntapScope#TARGET_PLAYERS_PERMANENTS},
 * {@link TapUntapScope#ALL_CREATURES}).
 *
 * <p>Replaces the former {@code TapTargetPermanentEffect}, {@code TapSelfEffect},
 * {@code TapEnchantedCreatureEffect}, {@code TapPermanentsOfTargetPlayerEffect},
 * {@code TapAllAttackingCreaturesEffect} and {@code TapCreaturesEffect}.
 *
 * @param scope  which permanent(s) to tap
 * @param filter optional predicate narrowing the scanned scopes (null = no restriction)
 */
public record TapPermanentsEffect(TapUntapScope scope, PermanentPredicate filter) implements CardEffect {

    public TapPermanentsEffect(TapUntapScope scope) {
        this(scope, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return scope == TapUntapScope.TARGET || scope == TapUntapScope.ALL_TARGETS;
    }

    @Override
    public boolean canTargetPlayer() {
        return scope == TapUntapScope.TARGET_PLAYERS_PERMANENTS;
    }

    @Override
    public boolean isSelfTargeting() {
        return scope == TapUntapScope.SELF;
    }
}
