package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to a secondary target stored in {@code targetIds.get(0)}.
 * The target can be a player or permanent — resolved like any-target damage.
 *
 * <p>Used when a spell has a primary target (in {@code targetId}) and an additional
 * secondary target (in {@code targetIds}), such as kicked spells that add a second target.
 *
 * @param damage the amount of damage to deal
 */
public record DealDamageToSecondaryTargetEffect(int damage) implements CardEffect {

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
