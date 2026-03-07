package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: as long as the controller has 0 or less life, all damage dealt to the controller
 * is dealt as though its source had infect (i.e. damage results in poison counters instead of life loss).
 */
public record DamageDealtAsInfectBelowZeroLifeEffect() implements CardEffect {
}
