package com.github.laxika.magicalvibes.model.effect;

/**
 * "... can't block this turn". A named permanent ({@code target()}, {@code attached()}) gets a floating
 * {@link CantBlockEffect} until end of turn. A set ({@code controlledBy(players, filter)}) is a rule, not a set of
 * objects: the restriction doesn't change characteristics, so it also reaches creatures that come under those players'
 * control later in the turn (CR 611.2c). Its players are found as it resolves ("target player", "that creature's
 * controller"); which creatures it covers is checked whenever blockers are declared.
 *
 * <p>Forms: {@code target()} (target creature, multi-target-group), {@code attached()} (enchanted creature),
 * {@code controlledBy(targetPlayer())} ("creatures target player controls"), {@code controlledBy(eachPlayer(), filter)}
 * ("creatures without flying can't block this turn"), {@code controlledBy(controllerOf(target()), creature ∧
 * ¬PermanentIsTargetCardPredicate)} ("other creatures that player controls", Mark for Death).
 *
 * <p>Not to be confused with a static {@code CantBlockEffect} ("this creature can't block", an ability) or
 * {@code CantBlockSourceEffect} ("can't block the source permanent"), which remain separate.
 *
 * @param subject which creature(s) can't block this turn
 */
public record CantBlockThisTurnEffect(PermanentSubject subject) implements CardEffect {

    public CantBlockThisTurnEffect {
        if (!(subject instanceof PermanentSubject.Target || subject instanceof PermanentSubject.Attached
                || subject instanceof PermanentSubject.ControlledBy)) {
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
