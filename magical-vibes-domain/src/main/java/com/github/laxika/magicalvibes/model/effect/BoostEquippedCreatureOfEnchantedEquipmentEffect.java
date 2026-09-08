package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Static power/toughness boost that follows an Aura attached to an Equipment to that Equipment's
 * equipped creature.
 */
public record BoostEquippedCreatureOfEnchantedEquipmentEffect(int powerBoost, int toughnessBoost)
        implements StaticCreatureBoostEffect {

    @Override
    public Set<Keyword> grantedKeywords() {
        return Set.of();
    }

    @Override
    public GrantScope scope() {
        return GrantScope.EQUIPPED_CREATURE;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
