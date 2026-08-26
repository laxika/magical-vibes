package com.github.laxika.magicalvibes.model.effect;

/**
 * Each creature chosen for one target group deals damage equal to its power to the any target
 * (creature, planeswalker, or player) chosen for another target group — e.g. Soul's Fire.
 * The dealing creature is the damage source (CR 608.2h): its power, color (protection checks),
 * "can't deal damage" restrictions and damage triggers key off it, not the spell.
 *
 * <p>Like {@link TargetDealsPowerDamageToTargetEffect}, this effect inherently reads two target groups,
 * so the groups live in DATA: {@code sourceTargetGroup}/{@code victimTargetGroup} are indices into
 * the card's {@code target(...)} declarations. The default groups are 0 (creature source) and
 * 1 (any target).</p>
 */
public record TargetCreatureDealsPowerDamageToAnyTargetEffect(int sourceTargetGroup, int victimTargetGroup,
                                                               boolean allowPlayerTarget) implements CardEffect {

    public TargetCreatureDealsPowerDamageToAnyTargetEffect(int sourceTargetGroup, int victimTargetGroup) {
        this(sourceTargetGroup, victimTargetGroup, true);
    }

    /** "Target creature you control deals damage equal to its power to any target" — groups 0 and 1. */
    public TargetCreatureDealsPowerDamageToAnyTargetEffect() {
        this(0, 1);
    }

    /** "Target creature deals damage equal to its power to target permanent". */
    public static TargetCreatureDealsPowerDamageToAnyTargetEffect toTargetPermanent() {
        return new TargetCreatureDealsPowerDamageToAnyTargetEffect(0, 1, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return allowPlayerTarget
                ? TargetSpec.harmful(TargetPredicates.anyTarget())
                : TargetSpec.harmful(TargetPredicates.permanent());
    }
}
