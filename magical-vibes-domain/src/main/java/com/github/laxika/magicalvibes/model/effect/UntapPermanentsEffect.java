package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untaps permanent(s) as an effect. The {@link TapUntapScope} selects which permanent(s) are
 * affected; the optional {@link PermanentPredicate} narrows the scanned scopes
 * ({@link TapUntapScope#CONTROLLED}, {@link TapUntapScope#OTHER_CONTROLLED_CREATURES}) or, for
 * {@link TapUntapScope#TARGET}, restricts what can be targeted (carried on the
 * {@link #targetSpec()} predicate).
 *
 * <p>Replaces the former {@code UntapTargetPermanentEffect}, {@code UntapAllTargetPermanentsEffect},
 * {@code UntapSelfEffect}, {@code UntapAllControlledPermanentsEffect},
 * {@code UntapEachOtherCreatureYouControlEffect}, {@code UntapAttackedCreaturesEffect},
 * {@code UntapEquippedCreatureEffect} and {@code UntapUpToControlledPermanentsEffect}.
 *
 * <p>With {@link TapUntapScope#CONTROLLED} or {@link TapUntapScope#ALL_PERMANENTS},
 * {@code chosenCount} of 0 untaps every matching tapped permanent in scope; a positive
 * {@code chosenCount} instead lets the controller pick <em>up to</em> that many of them to untap
 * (Rewind, Unwind, Treachery), mirroring
 * {@link TapPermanentsEffect#chosenCount()}.
 *
 * @param scope       which permanent(s) to untap
 * @param filter      optional predicate narrowing the scanned scopes, or the targeting restriction
 *                    for {@link TapUntapScope#TARGET} (null = no restriction)
 * @param chosenCount 0 = untap every permanent in scope; &gt;0 = the controller chooses up to N
 */
public record UntapPermanentsEffect(TapUntapScope scope, PermanentPredicate filter, int chosenCount)
        implements CardEffect {

    public UntapPermanentsEffect(TapUntapScope scope) {
        this(scope, null, 0);
    }

    public UntapPermanentsEffect(TapUntapScope scope, PermanentPredicate filter) {
        this(scope, filter, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            // TARGET untaps a single chosen permanent (filter narrows what may be targeted).
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            // ALL_TARGETS is a live multi-target scope (Garruk Wildspeaker's "untap two target lands"):
            // its targets ride entry.getTargetIds() and are validated on the multi-target path, so the
            // spec must stay a no-op (PLAYER_OR_PERMANENT) to preserve that path's null tolerance.
            case ALL_TARGETS -> TargetSpec.benign(TargetPredicates.playerOrPermanent());
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
