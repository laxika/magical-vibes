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
    public TargetSpec targetSpec() {
        return switch (scope) {
            // TARGET untaps a single chosen permanent (filter narrows what may be targeted).
            case TARGET -> TargetSpec.benign(TargetCategory.PERMANENT, filter);
            // ALL_TARGETS is a live multi-target scope (Garruk Wildspeaker's "untap two target lands"):
            // its targets ride entry.getTargetIds() and are validated on the multi-target path, so the
            // spec must stay a no-op (PLAYER_OR_PERMANENT) to preserve that path's null tolerance.
            case ALL_TARGETS -> TargetSpec.benign(TargetCategory.PLAYER_OR_PERMANENT);
            case SELF -> new TargetSpec(TargetCategory.NONE, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
