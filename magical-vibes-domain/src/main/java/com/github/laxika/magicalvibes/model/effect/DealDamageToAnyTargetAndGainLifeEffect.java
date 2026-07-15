package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to any target (creature, player, or planeswalker) and the controller gains life.
 *
 * <p>If the spell fizzles (e.g. the target is removed before resolution), no life is gained.
 *
 * <p>Example cards: Essence Drain, Dark Nourishment.
 *
 * @param damage   the amount of damage to deal to the target
 * @param lifeGain the amount of life the controller gains
 */
public record DealDamageToAnyTargetAndGainLifeEffect(int damage, int lifeGain) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }
}
