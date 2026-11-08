package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Trigger-condition record for "Whenever equipped creature deals damage to a blocking creature, this Equipment deals
 * that much damage to each other creature defending player controls" (Kusari-Gama). Registered in
 * {@code Triggers.creatureYouControlDealsDamageToCreature()}
 * on the Equipment, which fires only while the Equipment is attached to the damage source.
 *
 * <p>The collector puts {@link #triggeredEffect()} on the stack with the Equipment as its source, the defending
 * player as its non-targeting player, the damaged creature as its triggering permanent and the damage dealt as its
 * event value, so the record itself is never resolved.
 */
public record EquipmentDamagesOtherDefendingCreaturesEffect() implements CardEffect {

    /** "That much damage to each other creature defending player controls", read from the stack entry. */
    public CardEffect triggeredEffect() {
        return new DealDamageToEachMatchingPermanentEffect(new EventValue(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTriggeringPermanentPredicate()))),
                EachPermanentScope.TARGET_PLAYER);
    }
}
