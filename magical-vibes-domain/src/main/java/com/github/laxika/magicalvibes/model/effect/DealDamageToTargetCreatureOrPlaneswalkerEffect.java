package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals a fixed amount of damage to target creature or planeswalker (not player).
 * Used by Chandra, Bold Pyromancer's -3 ability.
 *
 * <p>An optional {@code targetRestriction} narrows the legal target (e.g. red only for
 * Chandra's Defeat); {@code null} means any creature or planeswalker is legal.
 */
public record DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage, PermanentPredicate targetRestriction) implements CardEffect {

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage) {
        this(damage, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE_OR_PLANESWALKER, targetRestriction);
    }
}
