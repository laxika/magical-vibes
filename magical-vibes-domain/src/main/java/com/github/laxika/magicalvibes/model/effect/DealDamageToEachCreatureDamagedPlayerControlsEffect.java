package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage equal to the combat damage dealt (stored in xValue on the stack entry)
 * to each creature the damaged player controls (stored in targetPermanentId on the stack entry).
 * Used by Balefire Dragon.
 */
public record DealDamageToEachCreatureDamagedPlayerControlsEffect() implements CardEffect {
}
