package com.github.laxika.magicalvibes.model.effect;

/**
 * A static effect that determines how much of a damage event to its own permanent is prevented.
 */
public interface SelfDamagePreventionEffect extends CardEffect {

    /**
     * Returns the amount prevented from a single damage event.
     *
     * @param damage the damage that would currently be dealt
     */
    int preventedDamage(int damage);
}
