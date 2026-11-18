package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marks creature(s) so they can't block for the remainder of the turn (a floating {@link CantBlockEffect} on each
 * until end of turn). The {@link TapUntapScope} selects which creature(s)
 * are affected; the optional {@link PermanentPredicate} narrows the scanned scopes
 * ({@link TapUntapScope#ALL_CREATURES}).
 *
 * <p>Replaces the former {@code TargetCreatureCantBlockThisTurnEffect} ({@code TARGET}),
 * {@code TargetPlayerCreaturesCantBlockThisTurnEffect} ({@code TARGET_PLAYERS_PERMANENTS}) and the
 * predicate-filtered mass {@code CantBlockThisTurnEffect(PermanentPredicate)} ({@code ALL_CREATURES}).
 * The {@link TapUntapScope#ENCHANTED} form marks the permanent attached to the source Aura.
 *
 * <p>Not to be confused with a static {@code CantBlockEffect} ("this creature can't block", an ability) or {@code CantBlockSourceEffect} ("can't block the
 * source permanent"), which remain separate.
 *
 * @param scope  which creature(s) can't block this turn
 * @param filter optional predicate narrowing the scanned scopes (null = no restriction)
 */
public record CantBlockThisTurnEffect(TapUntapScope scope, PermanentPredicate filter) implements CardEffect {

    public CantBlockThisTurnEffect(TapUntapScope scope) {
        this(scope, null);
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return scope == TapUntapScope.ENCHANTED;
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetPredicates.creature());
            case TARGET_PLAYERS_PERMANENTS -> TargetSpec.benign(TargetPredicates.playerOrPlaneswalker());
            case TARGET_CONTROLLERS_OTHER_CREATURES -> TargetSpec.benign(TargetPredicates.creature());
            default -> TargetSpec.NONE;
        };
    }
}
