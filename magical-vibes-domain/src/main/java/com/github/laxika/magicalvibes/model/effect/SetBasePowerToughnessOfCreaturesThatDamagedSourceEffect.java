package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the base power and toughness of each creature that dealt damage to the source permanent this
 * turn (Brine Hag). The affected creatures are still on the battlefield when this effect resolves. On a death
 * trigger the creatures are the ones that dealt damage to the stack entry's triggering permanent, the source as it
 * last existed on the battlefield (CR 603.10a).
 *
 * @param power     the new base power
 * @param toughness the new base toughness
 */
public record SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect(int power, int toughness)
        implements CardEffect {
}
