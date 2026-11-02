package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "Whenever equipped creature deals damage to a blocking creature, this Equipment deals
 * that much damage to each other creature defending player controls" (Kusari-Gama). Registered in
 * {@code Triggers.creatureYouControlDealsDamageToCreature()}
 * on the Equipment, which fires only while the Equipment is attached to the damage source.
 *
 * <p>Expanded at trigger-collection time into a {@link DealDamageToEachMatchingPermanentEffect}
 * ({@link EachPermanentScope#TARGET_PLAYER}, excluding the creature that was damaged) whose source
 * is the Equipment, so it is never resolved directly.
 */
public record EquipmentDamagesOtherDefendingCreaturesEffect() implements CardEffect {
}
