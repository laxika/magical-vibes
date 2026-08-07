package com.github.laxika.magicalvibes.model.effect;

/**
 * Which of a player's permanents a mass-damage effect reaches, for
 * {@link DealDamageToPermanentsTargetControlsEffect}.
 */
public enum DamagedPermanentScope {

    /** "each creature that player controls" (Radiating Lightning, Simoon, Aggravate). */
    CREATURES,

    /**
     * "each creature and planeswalker that player controls" (Chandra, Bold Pyromancer's −7). The
     * planeswalker half reads the printed type line rather than the layer-aware
     * {@code GameQueryService.isPlaneswalker}, matching {@code DamageSupport.dealCreatureDamage}'s
     * loyalty branch, which keys off the printed line too.
     */
    CREATURES_AND_PLANESWALKERS
}
