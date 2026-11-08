package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Trigger-condition record for {@code Triggers.creatureYouControlCrewsVehicle()}: "Whenever [a matching creature] you
 * control crews a Vehicle, that Vehicle perpetually gets +P/+T" (Tiana, Angelic Mechanic). Never resolved directly: the
 * crew collector judges the crewing creature and puts {@link #triggeredEffect()} on the stack with the Vehicle as the
 * entry's triggering permanent.
 */
public record PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect(
        PermanentPredicate crewingCreaturePredicate,
        int powerBoost,
        int toughnessBoost
) implements CardEffect {

    /** The boost that goes on the stack: the triggering Vehicle's card. */
    public CardEffect triggeredEffect() {
        return PerpetuallyBoostCardEffect.ofTriggeringPermanent(powerBoost, toughnessBoost);
    }
}
