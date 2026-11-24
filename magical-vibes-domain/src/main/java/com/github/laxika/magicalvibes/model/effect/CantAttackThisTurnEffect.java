package com.github.laxika.magicalvibes.model.effect;

/**
 * "... can't attack this turn". A named permanent ({@code target()}, multi-target-group) gets this record as a floating
 * effect for {@code duration}. A set ({@code controlledBy(players, filter)}) is a rule on each of its players, found as
 * it resolves: the restriction doesn't change characteristics, so it covers whatever creatures that player controls
 * that match the set's filter whenever attackers are declared, those arriving later included (CR 611.2c). The set
 * but the permanents another subject names ({@code except(controlledBy(activePlayer(), creature), thisWay(CHOOSE))}:
 * "only creatures in the pile of their choice can attack this turn", Fight or Flight) is the same rule without those
 * permanents, found as it resolves and left out whatever they become. Attack-side
 * sibling of {@link CantBlockThisTurnEffect}; combine the two for "target creature can't attack or block this turn".
 *
 * <p>Not to be confused with the static restriction effects ({@code CantAttackUnlessEffect},
 * {@code EnchantedCreatureCantAttackEffect}) or with {@link OtherCreaturesCantAttackThisTurnEffect},
 * which installs a turn-scoped lock exempting only the target.
 *
 * @param subject  which creature(s) can't attack
 * @param duration how long ({@code UNTIL_END_OF_TURN} or {@code UNTIL_YOUR_NEXT_TURN})
 */
public record CantAttackThisTurnEffect(PermanentSubject subject, Duration duration) implements CardEffect {

    public CantAttackThisTurnEffect {
        if (!(subject instanceof PermanentSubject.Target || subject instanceof PermanentSubject.ControlledBy
                || subject instanceof PermanentSubject.Except except
                        && except.permanents() instanceof PermanentSubject.ControlledBy)) {
            throw new IllegalArgumentException("Unsupported can't-attack subject: " + subject);
        }
        if (duration != Duration.UNTIL_END_OF_TURN && duration != Duration.UNTIL_YOUR_NEXT_TURN) {
            throw new IllegalArgumentException("Unsupported attack restriction duration: " + duration);
        }
    }

    public CantAttackThisTurnEffect(PermanentSubject subject) {
        this(subject, Duration.UNTIL_END_OF_TURN);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.benign(TargetPredicates.creature());
        }
        if (subject instanceof PermanentSubject.ControlledBy set && set.players().declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
