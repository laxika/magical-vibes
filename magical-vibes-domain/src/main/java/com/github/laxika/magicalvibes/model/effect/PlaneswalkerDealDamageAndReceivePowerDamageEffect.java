package com.github.laxika.magicalvibes.model.effect;

/**
 * The source planeswalker deals {@code damage} damage to target creature.
 * That creature then deals damage equal to its power to the source planeswalker
 * (removing loyalty counters). Used by Garruk Relentless's 0-loyalty ability.
 *
 * <p>This is NOT fight — the planeswalker deals a fixed amount of damage,
 * not damage equal to its power. The creature's reciprocal damage removes
 * loyalty counters from the source planeswalker rather than dealing combat damage.</p>
 *
 * @param damage the fixed damage the planeswalker deals to the target creature
 */
public record PlaneswalkerDealDamageAndReceivePowerDamageEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
