package com.github.laxika.magicalvibes.model.effect;

/**
 * "... can't block this turn" / "... can't block this combat". A named permanent ({@code target()}, {@code attached()},
 * {@code source()}) gets a floating {@link CantBlockEffect} for {@code duration}. A set ({@code controlledBy(players, filter)}) is a rule, not a set of
 * objects: the restriction doesn't change characteristics, so it also reaches creatures that come under those players'
 * control later in the turn (CR 611.2c). Its players are found as it resolves ("target player", "that creature's
 * controller"); which creatures it covers is checked whenever blockers are declared.
 *
 * <p>Forms: {@code target()} (target creature, multi-target-group), {@code attached()} (enchanted creature),
 * {@code source()} ("this creature can't block this turn" in its own ability; Ydwen Efreet's lost flip),
 * {@code controlledBy(targetPlayer())} ("creatures target player controls"), {@code controlledBy(eachPlayer(), filter)}
 * ("creatures without flying can't block this turn"), {@code controlledBy(controllerOf(target()), creature ∧
 * ¬PermanentIsTargetCardPredicate)} ("other creatures that player controls", Mark for Death),
 * {@code thisWay(DEAL_DAMAGE, creature)} ("a creature dealt damage this way can't block this turn", Ballista Wielder),
 * {@code except(controlledBy(targetPlayer(), creature), thisWay(CHOOSE))} ("other creatures they control" after
 * "target opponent chooses a creature they control", Goblin War Cry): the set's rule without the permanents the
 * exception names as it resolves. {@code controlledBy(eachPlayer(), creature ∧ PermanentHasChosenParityPredicate)}
 * ("creatures with mana value of that quality", Twins of Discord) binds the parity chosen this resolution into the
 * rule. {@code Duration.UNTIL_END_OF_COMBAT} is "this creature / that creature can't block this combat" (Forgestoker
 * Dragon, Flummoxed Cyclops).
 *
 * <p>Not to be confused with a static {@code CantBlockEffect} ("this creature can't block", an ability) or
 * {@code CantBlockSourceEffect} ("can't block the source permanent"), which remain separate.
 *
 * @param subject  which creature(s) can't block
 * @param duration how long: {@code UNTIL_END_OF_TURN} or {@code UNTIL_END_OF_COMBAT}
 */
public record CantBlockThisTurnEffect(PermanentSubject subject, Duration duration) implements CardEffect {

    /** "... can't block this turn". */
    public CantBlockThisTurnEffect(PermanentSubject subject) {
        this(subject, Duration.UNTIL_END_OF_TURN);
    }

    public CantBlockThisTurnEffect {
        if (duration != Duration.UNTIL_END_OF_TURN && duration != Duration.UNTIL_END_OF_COMBAT) {
            throw new IllegalArgumentException("Unsupported can't-block duration: " + duration);
        }
        if (!(subject instanceof PermanentSubject.Target || subject instanceof PermanentSubject.Attached
                || subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.ControlledBy || subject instanceof PermanentSubject.ThisWay
                || subject instanceof PermanentSubject.Except except
                        && except.permanents() instanceof PermanentSubject.ControlledBy)) {
            throw new IllegalArgumentException("Unsupported can't-block subject: " + subject);
        }
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
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
