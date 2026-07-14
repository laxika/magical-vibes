package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marks permanent(s) so they don't untap during their controller's next untap step (a one-shot
 * "skip next untap", modelled by incrementing {@code Permanent.skipUntapCount}, which the untap step
 * reads and decrements). The {@link TapUntapScope} selects which permanent(s) are affected; the
 * optional {@link PermanentPredicate} narrows the scanned scopes ({@link TapUntapScope#ALL_CREATURES},
 * {@link TapUntapScope#TARGET_PLAYERS_PERMANENTS}). The {@link TapUntapScope#SELF} scope keeps the
 * source permanent itself tapped through its next untap step (e.g. an attack trigger on Lead Golem).
 *
 * <p>Replaces the former {@code SkipNextUntapOnTargetEffect}, {@code SkipNextUntapAllAttackingCreaturesEffect}
 * and {@code SkipNextUntapPermanentsOfTargetPlayerEffect}.
 *
 * @param scope  which permanent(s) to keep tapped through their next untap step
 * @param filter optional predicate narrowing the scanned scopes (null = no restriction)
 */
public record SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter) implements CardEffect {

    public SkipNextUntapEffect(TapUntapScope scope) {
        this(scope, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return scope == TapUntapScope.TARGET;
    }

    @Override
    public boolean canTargetPlayer() {
        return scope == TapUntapScope.TARGET_PLAYERS_PERMANENTS;
    }
}
