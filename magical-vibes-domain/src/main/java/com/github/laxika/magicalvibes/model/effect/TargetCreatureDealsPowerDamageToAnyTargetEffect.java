package com.github.laxika.magicalvibes.model.effect;

/**
 * Each creature chosen for one target group deals damage equal to its power to the target chosen for another target
 * group, the excess over lethal damage dealt to that creature's controller instead when the dealing creature has
 * trample (Ram Through, CR 120.4a). The dealing creature is the damage source (CR 608.2h): its power, colour
 * (protection), "can't deal damage" restrictions and damage triggers key off it, not the spell.
 *
 * <p>Only the trample-excess form is this record ({@link #withTrampleExcessToController()}); the plain bite of a
 * creature to any target or target permanent is {@link DealDamageEffect#toAnyTargetInGroup} /
 * {@link DealDamageEffect#toTargetPermanentOfGroup} with {@link DealDamageEffect#dealtBy}. It waits for the excess
 * redirect's fold ({@code DealDamageToTargetCreatureWithExcessToControllerEffect}).</p>
 */
public record TargetCreatureDealsPowerDamageToAnyTargetEffect(int sourceTargetGroup, int victimTargetGroup,
                                                               boolean allowPlayerTarget,
                                                               boolean excessDamageToControllerIfSourceHasTrample)
        implements CardEffect {

    /** Target creature deals power damage to a creature, with its trample excess to that creature's controller. */
    public static TargetCreatureDealsPowerDamageToAnyTargetEffect withTrampleExcessToController() {
        return new TargetCreatureDealsPowerDamageToAnyTargetEffect(0, 1, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return allowPlayerTarget
                ? TargetSpec.harmful(TargetPredicates.anyTarget())
                : TargetSpec.harmful(TargetPredicates.permanent());
    }
}
