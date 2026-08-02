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
 * <p>With {@link TapUntapScope#TARGET_PLAYERS_PERMANENTS}, {@code chosenCount} of 0 taps every
 * matching permanent that player controls; a positive {@code chosenCount} instead lets the
 * resolving controller pick <em>up to</em> that many of them to tap (Yosei, the Morning Star).
 *
 * @param scope       which permanent(s) to tap
 * @param filter      optional predicate narrowing the scanned scopes (null = no restriction)
 * @param chosenCount 0 = tap every permanent in scope; &gt;0 = the controller chooses up to N
 */
public record TapPermanentsEffect(TapUntapScope scope, PermanentPredicate filter, int chosenCount)
        implements CardEffect {

    public TapPermanentsEffect(TapUntapScope scope) {
        this(scope, null, 0);
    }

    public TapPermanentsEffect(TapUntapScope scope, PermanentPredicate filter) {
        this(scope, filter, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            // TARGET taps a chosen permanent; ALL_TARGETS is unused by tap (dead path) but keeps
            // canTargetPermanent=true exactly. The single-target validation runs requireBattlefieldTarget.
            case TARGET, ALL_TARGETS -> TargetSpec.benign(TargetCategory.PERMANENT);
            case TARGET_PLAYERS_PERMANENTS -> TargetSpec.benign(TargetCategory.PLAYER);
            case SELF -> new TargetSpec(TargetCategory.NONE, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
