package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys every permanent matching {@code filter} on the battlefield(s) selected by {@code scope},
 * then optionally resolves a {@code thenEffect} rider.
 *
 * <p>The number of permanents <em>actually destroyed</em> (indestructible and regenerated
 * permanents do not count) is snapshotted onto the rider's {@code StackEntry.eventValue} channel, so
 * "for each permanent destroyed this way" riders are built from existing effects on the
 * {@code EventValue} amount (Fracturing Gust = {@code GainLifeEffect(Scaled(EventValue(), 2))},
 * Phyrexian Rebirth = an X/X token with {@code EventValue} power/toughness). The rider always
 * resolves against the effect's controller, mirroring {@link DestroyTargetPermanentThenEffect}.
 *
 * @param filter                              which permanents are destroyed
 * @param cannotBeRegenerated                 when {@code true} the destruction can't be prevented by regeneration
 * @param scope                               every battlefield, or only the targeted player's (Rain of Daggers)
 * @param thenEffect                          optional rider resolved after destruction with the destroyed count on
 *                                            {@code eventValue}; {@code null} for a plain board wipe
 * @param sparesPermanentsCreatedThisResolution when {@code true}, permanents created earlier in this same
 *                                            resolution (recorded on {@code StackEntry.createdPermanentIds}) are
 *                                            excluded, implementing "destroy all <em>other</em> creatures" where
 *                                            "other" means other than the tokens this spell just made (Martial Coup)
 * @param destroyedCountScope                 which destroyed permanents contribute to the rider's {@code eventValue}
 */
public record DestroyAllPermanentsEffect(
        PermanentPredicate filter,
        boolean cannotBeRegenerated,
        EachPermanentScope scope,
        CardEffect thenEffect,
        boolean sparesPermanentsCreatedThisResolution,
        DestroyedPermanentCountScope destroyedCountScope
) implements BoardWipeEffect {

    public DestroyAllPermanentsEffect(PermanentPredicate filter, boolean cannotBeRegenerated,
                                      EachPermanentScope scope, CardEffect thenEffect,
                                      boolean sparesPermanentsCreatedThisResolution) {
        this(filter, cannotBeRegenerated, scope, thenEffect, sparesPermanentsCreatedThisResolution,
                DestroyedPermanentCountScope.ALL);
    }

    public DestroyAllPermanentsEffect(PermanentPredicate filter) {
        this(filter, false, EachPermanentScope.ALL_PLAYERS, null, false, DestroyedPermanentCountScope.ALL);
    }

    public DestroyAllPermanentsEffect(PermanentPredicate filter, boolean cannotBeRegenerated) {
        this(filter, cannotBeRegenerated, EachPermanentScope.ALL_PLAYERS, null, false,
                DestroyedPermanentCountScope.ALL);
    }

    /** Board wipe with a per-destroyed-count rider ("You gain 2 life for each permanent destroyed this way"). */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, CardEffect thenEffect) {
        this(filter, false, EachPermanentScope.ALL_PLAYERS, thenEffect, false, DestroyedPermanentCountScope.ALL);
    }

    /** Board wipe with a rider whose destroyed-count event value is scoped to one controller. */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, CardEffect thenEffect,
                                      DestroyedPermanentCountScope destroyedCountScope) {
        this(filter, false, EachPermanentScope.ALL_PLAYERS, thenEffect, false, destroyedCountScope);
    }

    /** Scoped destroy-all with a per-destroyed-count rider ("Destroy all creatures target player controls. …"). */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, EachPermanentScope scope, CardEffect thenEffect) {
        this(filter, false, scope, thenEffect, false, DestroyedPermanentCountScope.ALL);
    }

    /**
     * "Destroy all other creatures" style wipe that spares permanents created earlier in this same
     * resolution — e.g. Martial Coup, which makes its Soldier tokens first and then destroys every
     * <em>other</em> creature. Pair with an unconditional {@code CreateTokenEffect} resolving before it.
     */
    public static DestroyAllPermanentsEffect sparingPermanentsCreatedThisResolution(PermanentPredicate filter) {
        return new DestroyAllPermanentsEffect(filter, false, EachPermanentScope.ALL_PLAYERS, null, true,
                DestroyedPermanentCountScope.ALL);
    }

    /** Destroy-all always sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return true;
    }

    /**
     * The {@link EachPermanentScope#TARGET_PLAYER} scope needs the player whose battlefield is
     * swept — "destroy all creatures target opponent controls", "destroy all lands target player
     * controls". Same shape as the sibling scoped effects
     * ({@link DealDamageToEachMatchingPermanentEffect},
     * {@link PutCounterOnEachMatchingPermanentEffect}); the every-battlefield scope targets nothing.
     */
    @Override
    public TargetSpec targetSpec() {
        return scope == EachPermanentScope.TARGET_PLAYER
                ? TargetSpec.harmful(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
