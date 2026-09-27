package com.github.laxika.magicalvibes.model.effect;

/**
 * The creature chosen for one target group deals damage equal to a multiple of its power to
 * the permanent chosen for another target group (one-sided fight, e.g. Wing Puncture). The
 * dealing creature's color is used for protection checks, not the spell's color. If either
 * chosen permanent is gone at resolution, no damage is dealt.
 *
 * <p>Like {@link FightTargetsEffect}, this effect inherently reads two targets, so the groups
 * live in DATA: {@code sourceTargetGroup}/{@code victimTargetGroup} are indices into the
 * card's {@code target(...)} declarations (see {@code StackEntry.targetsForGroup}). For
 * activated abilities, which declare targets via a flat multi-target filter list instead, the
 * indices address flat target positions. The default groups are 0 and 1.</p>
 */
public record TargetDealsPowerDamageToTargetEffect(int sourceTargetGroup,
                                                   int victimTargetGroup,
                                                   int powerMultiplier,
                                                   boolean recordExcessDamage) implements CardEffect {

    public TargetDealsPowerDamageToTargetEffect {
        if (powerMultiplier < 1) {
            throw new IllegalArgumentException("Power multiplier must be positive");
        }
    }

    public TargetDealsPowerDamageToTargetEffect(int sourceTargetGroup, int victimTargetGroup) {
        this(sourceTargetGroup, victimTargetGroup, 1, false);
    }

    /** "Target creature deals damage equal to its power to another target" — groups 0 and 1. */
    public TargetDealsPowerDamageToTargetEffect() {
        this(0, 1, 1, false);
    }

    public TargetDealsPowerDamageToTargetEffect(int powerMultiplier) {
        this(0, 1, powerMultiplier, false);
    }

    public TargetDealsPowerDamageToTargetEffect(int sourceTargetGroup, int victimTargetGroup,
                                                int powerMultiplier) {
        this(sourceTargetGroup, victimTargetGroup, powerMultiplier, false);
    }

    /** Bite variant that records excess damage for a following {@link com.github.laxika.magicalvibes.model.amount.EventValue}. */
    public static TargetDealsPowerDamageToTargetEffect recordingExcessDamage() {
        return new TargetDealsPowerDamageToTargetEffect(0, 1, 1, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return recordExcessDamage
                ? TargetSpec.harmful(TargetPredicates.creature())
                : TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
