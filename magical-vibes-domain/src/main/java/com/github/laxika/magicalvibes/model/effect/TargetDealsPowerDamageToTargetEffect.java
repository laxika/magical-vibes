package com.github.laxika.magicalvibes.model.effect;

/**
 * The creature chosen for one target group deals damage equal to its power to the creature
 * chosen for another target group (one-sided fight, e.g. Wing Puncture). The dealing
 * creature's color is used for protection checks, not the spell's color. If either chosen
 * creature is gone at resolution, no damage is dealt.
 *
 * <p>Like {@link FightTargetsEffect}, this effect inherently reads two targets, so the groups
 * live in DATA: {@code sourceTargetGroup}/{@code victimTargetGroup} are indices into the
 * card's {@code target(...)} declarations (see {@code StackEntry.targetsForGroup}). For
 * activated abilities, which declare targets via a flat multi-target filter list instead, the
 * indices address flat target positions. The default groups are 0 and 1.</p>
 */
public record TargetDealsPowerDamageToTargetEffect(int sourceTargetGroup, int victimTargetGroup) implements CardEffect {

    /** "Target creature deals damage equal to its power to another target" — groups 0 and 1. */
    public TargetDealsPowerDamageToTargetEffect() {
        this(0, 1);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
