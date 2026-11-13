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
 * When {@code matchAtUntap} is true, the target-player and controlled scopes store a player-wide
 * restriction and evaluate matching permanents during that player's upcoming untap steps, including
 * later arrivals (the affected set is not fixed at resolution, CR 611.2c).
 *
 * The {@link TapUntapScope#OPPONENTS_PERMANENTS} scope marks the matching permanents each opponent of
 * the controller currently controls and, when {@code matchAtUntap} is true, also registers the
 * restriction for each opponent's next untap step so permanents arriving later are held too
 * (Pollen Lullaby, CR 611.2c).
 *
 * <p>With {@link TapUntapScope#TARGET} on a {@code Triggers.blocks()} ability whose card declares no target
 * for it, the target is "that creature", the blocked attacker, which the collector records as the entry's
 * non-targeting target: a creature's own (Wall of Frost) or an Aura's or Equipment's (Shield of the
 * Righteous). Other scopes ignore the entry's target.
 *
 * @param scope      which permanent(s) to keep tapped through their next untap step
 * @param filter     optional predicate narrowing the scanned scopes (null = no restriction)
 * @param untapSteps number of upcoming untap steps to skip
 * @param matchAtUntap whether the target player's matching permanents are determined at untap
 * @param allPlayersAtUntap whether matching permanents controlled by any player are affected
 *                          during the target player's untap step
 * @param controllerStepOnly whether the restriction expires during the resolving controller's
 *                           next untap step, even if the permanent changes controllers (exert)
 * @param activationCost whether a self restriction is paid as exert during activation and
 *                       omitted from the ability's resolution effects
 */
public record SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter, int untapSteps,
                                  boolean matchAtUntap, boolean allPlayersAtUntap, boolean controllerStepOnly,
                                  boolean activationCost)
        implements CardEffect {

    public SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter, int untapSteps,
                               boolean matchAtUntap, boolean allPlayersAtUntap, boolean controllerStepOnly) {
        this(scope, filter, untapSteps, matchAtUntap, allPlayersAtUntap, controllerStepOnly, false);
    }

    public SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter, int untapSteps,
                               boolean matchAtUntap, boolean allPlayersAtUntap) {
        this(scope, filter, untapSteps, matchAtUntap, allPlayersAtUntap, false);
    }

    public SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter, int untapSteps,
                               boolean matchAtUntap) {
        this(scope, filter, untapSteps, matchAtUntap, false);
    }

    public SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter, int untapSteps) {
        this(scope, filter, untapSteps, false);
    }

    public SkipNextUntapEffect(TapUntapScope scope) {
        this(scope, null, 1);
    }

    public SkipNextUntapEffect(TapUntapScope scope, PermanentPredicate filter) {
        this(scope, filter, 1);
    }

    public SkipNextUntapEffect(TapUntapScope scope, int untapSteps) {
        this(scope, null, untapSteps);
    }

    /**
     * The exertion of "You may exert this creature as it attacks" (CR 701.43a): the source doesn't untap during
     * the exerting player's next untap step. The attack collector asks it for a creature with {@code
     * ExertAsItAttacksEffect} as the creature attacks; the "when you do" is {@code Triggers.exertedAsItAttacks()}.
     */
    public static SkipNextUntapEffect exert() {
        return new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true);
    }

    public SkipNextUntapEffect {
        if (matchAtUntap && scope != TapUntapScope.TARGET_PLAYERS_PERMANENTS
                && scope != TapUntapScope.CONTROLLED && scope != TapUntapScope.OPPONENTS_PERMANENTS) {
            throw new IllegalArgumentException("Matching at untap requires a target player");
        }
        if (allPlayersAtUntap && !matchAtUntap) {
            throw new IllegalArgumentException("All-player matching requires matching at untap");
        }
        if (untapSteps < 1) {
            throw new IllegalArgumentException("untapSteps must be positive");
        }
    }

    /** Whether this is the exertion of "you may exert this creature as it attacks" ({@link #exert()}). */
    public boolean exertsAsItAttacks() {
        return scope == TapUntapScope.SELF && controllerStepOnly && !activationCost;
    }

    @Override
    public TargetSpec targetSpec() {
        if (scope == TapUntapScope.TARGET) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (scope == TapUntapScope.TARGET_PLAYERS_PERMANENTS) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
