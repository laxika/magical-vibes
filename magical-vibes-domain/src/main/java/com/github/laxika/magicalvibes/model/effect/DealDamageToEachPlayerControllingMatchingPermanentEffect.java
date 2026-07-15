package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals a fixed amount of damage to each player who controls at least one permanent matching
 * {@code predicate}. Non-targeting — iterates over all players' battlefields.
 *
 * <p>Player-damage sibling of {@link DealDamageToEachMatchingPermanentEffect}. Used by cards like
 * Disorder ("deals 2 damage to each white creature and each player who controls a white creature"),
 * where this effect handles the player half (pair it with a
 * {@code DealDamageToEachMatchingPermanentEffect} for the creature half). The predicate must fully
 * describe the matching permanent (e.g. {@code AllOf(IsCreature, ColorWhite)}).</p>
 */
public record DealDamageToEachPlayerControllingMatchingPermanentEffect(int damage,
                                                                       PermanentPredicate predicate)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, true, null, false, 1);
    }
}
