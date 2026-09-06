package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the base power and toughness of each creature that dealt damage to the source permanent this
 * turn. The affected creatures are still on the battlefield when this effect resolves.
 *
 * @param power     the new base power
 * @param toughness the new base toughness
 */
public record SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect(int power, int toughness)
        implements CardEffect {
}
