package com.github.laxika.magicalvibes.model.effect;

/**
 * One permanent (the {@code dealer}) deals damage equal to its power to each other creature {@code recipients} names:
 * "target creature you control deals damage equal to its power to each other creature" (the default), "that creature
 * deals damage equal to its power to each other creature" after a return ({@code returnedThisResolution()}), "target
 * creature an opponent controls deals damage equal to its power to each other creature that player controls"
 * ({@code controlledBy(controllerOf(target()), creature)}). The dealer is the source of every damage event (CR 120.1)
 * and is never dealt damage itself; a dealer that is not a target and left the battlefield deals the damage as it last
 * existed there (CR 608.2h). The recipients are found once, as the effect resolves (CR 608.2h).
 *
 * @param dealer     {@code target()} (declares the creature target) or a non-targeting one-permanent subject
 * @param recipients a non-targeting set of creatures
 */
public record TargetCreatureDealsPowerDamageToEachOtherCreatureEffect(PermanentSubject dealer,
                                                                      PermanentSubject recipients)
        implements BoardWipeEffect {

    public TargetCreatureDealsPowerDamageToEachOtherCreatureEffect {
        if (dealer.declaresTarget() && !(dealer instanceof PermanentSubject.Target target && target.group() == null)) {
            throw new IllegalArgumentException("Unsupported dealer: " + dealer);
        }
        if (recipients.declaresTarget()) {
            throw new IllegalArgumentException("The recipients are not targeted: " + recipients);
        }
    }

    public TargetCreatureDealsPowerDamageToEachOtherCreatureEffect() {
        this(Subjects.target(), Subjects.allCreatures());
    }

    /**
     * A target dealer declares a creature target: harmful when the dealer's own controller's creatures are the ones
     * dealt damage ("each other creature that player controls", Alpha Brawl), else benign (the controller's own
     * creature dealing damage to every other creature).
     */
    @Override
    public TargetSpec targetSpec() {
        if (!(dealer instanceof PermanentSubject.Target)) {
            return TargetSpec.NONE;
        }
        boolean dealersControllerHit = recipients instanceof PermanentSubject.ControlledBy controlledBy
                && controlledBy.players() instanceof PlayerSubject.ControllerOf controllerOf
                && controllerOf.permanent().equals(dealer);
        return dealersControllerHit
                ? TargetSpec.harmful(TargetPredicates.creature())
                : TargetSpec.benign(TargetPredicates.creature());
    }

    /** A dealer the controller didn't target (a returned creature) sweeps the board as the AI sees it. */
    @Override
    public boolean sweepsBoard() {
        return !dealer.declaresTarget();
    }
}
