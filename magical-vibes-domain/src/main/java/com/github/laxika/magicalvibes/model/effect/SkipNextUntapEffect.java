package com.github.laxika.magicalvibes.model.effect;

/**
 * "... doesn't untap during its controller's next untap step". A named permanent ({@code target()}, {@code source()},
 * {@code attached()}, {@code triggering()}) and each permanent of a set found as it resolves
 * ({@code controlledBy(players, filter)} without {@code matchAtUntap}: "tap all creatures target player controls. Those
 * creatures don't untap ...", "each attacking creature doesn't untap ...", whose creatures are fixed as it resolves, as
 * Tangle's ruling says) get a {@link DoesntUntapDuringNextUntapStepsEffect} floating effect, which the untap step uses
 * up. With {@code source()} it keeps the source itself tapped (Lead Golem's attack trigger, exert).
 *
 * <p>With {@code matchAtUntap} the effect is a rule ("lands you control don't untap during your next untap step"): it
 * is registered on each player of the set, found as it resolves, and the set's filter is checked against that
 * player's permanents as their untap step begins, so permanents arriving later are held too and a permanent that left
 * their control is not (CR 611.2c). With {@code allPlayersAtUntap} too it holds every matching permanent during that
 * player's untap step, whoever controls it.
 *
 * <p>With {@code target()} on a {@code Triggers.blocks()} ability whose card declares no target for it, the target is
 * "that creature", the blocked attacker, which the collector records as the entry's non-targeting target: a creature's
 * own (Wall of Frost) or an Aura's or Equipment's (Shield of the Righteous).
 *
 * @param subject            which permanent(s) to keep tapped through their next untap step
 * @param untapSteps         number of upcoming untap steps to skip
 * @param matchAtUntap       whether the set's permanents are determined at each player's untap step
 * @param allPlayersAtUntap  whether matching permanents controlled by any player are affected during the set's
 *                           players' untap step
 * @param controllerStepOnly whether the restriction expires during the resolving controller's next untap step, even
 *                           if the permanent changes controllers (exert)
 * @param activationCost     whether a {@code source()} restriction is paid as exert during activation and omitted from
 *                           the ability's resolution effects
 */
public record SkipNextUntapEffect(PermanentSubject subject, int untapSteps, boolean matchAtUntap,
                                  boolean allPlayersAtUntap, boolean controllerStepOnly, boolean activationCost)
        implements CardEffect {

    public SkipNextUntapEffect(PermanentSubject subject, int untapSteps, boolean matchAtUntap,
                               boolean allPlayersAtUntap, boolean controllerStepOnly) {
        this(subject, untapSteps, matchAtUntap, allPlayersAtUntap, controllerStepOnly, false);
    }

    public SkipNextUntapEffect(PermanentSubject subject, int untapSteps, boolean matchAtUntap,
                               boolean allPlayersAtUntap) {
        this(subject, untapSteps, matchAtUntap, allPlayersAtUntap, false);
    }

    public SkipNextUntapEffect(PermanentSubject subject, int untapSteps, boolean matchAtUntap) {
        this(subject, untapSteps, matchAtUntap, false);
    }

    public SkipNextUntapEffect(PermanentSubject subject, int untapSteps) {
        this(subject, untapSteps, false);
    }

    public SkipNextUntapEffect(PermanentSubject subject) {
        this(subject, 1);
    }

    /**
     * The exertion of "You may exert this creature as it attacks" (CR 701.43a): the source doesn't untap during
     * the exerting player's next untap step. The attack collector asks it for a creature with {@code
     * ExertAsItAttacksEffect} as the creature attacks; the "when you do" is {@code Triggers.exertedAsItAttacks()}.
     */
    public static SkipNextUntapEffect exert() {
        return new SkipNextUntapEffect(Subjects.source(), 1, false, false, true);
    }

    public SkipNextUntapEffect {
        if (matchAtUntap && !(subject instanceof PermanentSubject.ControlledBy)) {
            throw new IllegalArgumentException("Matching at untap requires a set of some players' permanents");
        }
        if (allPlayersAtUntap && !matchAtUntap) {
            throw new IllegalArgumentException("All-player matching requires matching at untap");
        }
        if (activationCost && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Only the source is exerted as an activation cost");
        }
        if (untapSteps < 1) {
            throw new IllegalArgumentException("untapSteps must be positive");
        }
    }

    /** Whether this is the exertion of "you may exert this creature as it attacks" ({@link #exert()}). */
    public boolean exertsAsItAttacks() {
        return subject instanceof PermanentSubject.Source && controllerStepOnly && !activationCost;
    }

    /** This restriction with one untap step fewer to go, for a rule registered on a player. */
    public SkipNextUntapEffect withOneStepFewer() {
        return new SkipNextUntapEffect(subject, untapSteps - 1, matchAtUntap, allPlayersAtUntap, controllerStepOnly,
                activationCost);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (subject instanceof PermanentSubject.ControlledBy set && set.players().declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
