package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "Whenever equipped creature deals damage to a blocking creature, this Equipment deals
 * that much damage to each other creature defending player controls" (Kusari-Gama). Registered in
 * {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE}
 * on the Equipment, which fires only while the Equipment is attached to the damage source.
 *
 * <p>Expanded at trigger-collection time into a {@link DealDamageToEachMatchingPermanentEffect}
 * ({@link EachPermanentScope#TARGET_PLAYER}, excluding the creature that was damaged) whose source
 * is the Equipment, so it is never resolved directly.
 */
public record EquipmentDamagesOtherDefendingCreaturesEffect() implements CardEffect {
}
