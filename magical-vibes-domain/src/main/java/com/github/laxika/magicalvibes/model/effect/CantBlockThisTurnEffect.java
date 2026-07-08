package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marks creature(s) so they can't block for the remainder of the turn (a one-shot effect that
 * sets {@code Permanent.cantBlockThisTurn}). The {@link TapUntapScope} selects which creature(s)
 * are affected; the optional {@link PermanentPredicate} narrows the scanned scopes
 * ({@link TapUntapScope#ALL_CREATURES}).
 *
 * <p>Replaces the former {@code TargetCreatureCantBlockThisTurnEffect} ({@code TARGET}),
 * {@code TargetPlayerCreaturesCantBlockThisTurnEffect} ({@code TARGET_PLAYERS_PERMANENTS}) and the
 * predicate-filtered mass {@code CantBlockThisTurnEffect(PermanentPredicate)} ({@code ALL_CREATURES}).
 *
 * <p>Not to be confused with the static {@code CantBlockEffect} ("this creature can't block",
 * read continuously by the combat services) or {@code CantBlockSourceEffect} ("can't block the
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
    public boolean canTargetPermanent() {
        return scope == TapUntapScope.TARGET;
    }

    @Override
    public boolean canTargetPlayer() {
        return scope == TapUntapScope.TARGET_PLAYERS_PERMANENTS;
    }
}
