package com.github.laxika.magicalvibes.model.effect;

/**
 * The creature this Aura is attached to fights the target creature: each deals damage equal to its
 * power to the other (CR 701.12). The fighting creature is re-derived from the Aura source's
 * attachment at resolution, so it is the <em>enchanted</em> creature — not the Aura permanent
 * itself, whose power is 0. Protection is checked against each creature's own color, and if either
 * creature is gone at resolution no damage is dealt. Used by Cartouche of Strength.
 */
public record EnchantedCreatureFightsTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
