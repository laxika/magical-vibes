package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals a fixed amount of damage to each matching creature controlled by the player being attacked.
 * The attacked player or planeswalker is preserved as non-targeting combat context on the stack
 * entry, so no player target is chosen. A {@code null} filter matches every defending creature.
 */
public record DealDamageToDefendingPlayerCreaturesEffect(int damage, PermanentPredicate filter)
        implements CardEffect {

    public DealDamageToDefendingPlayerCreaturesEffect(int damage) {
        this(damage, null);
    }
}
