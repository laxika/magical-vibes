package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to target creature or planeswalker (not player).
 * Used by Chandra, Bold Pyromancer's -3 ability.
 */
public record DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE_OR_PLANESWALKER);
    }
}
