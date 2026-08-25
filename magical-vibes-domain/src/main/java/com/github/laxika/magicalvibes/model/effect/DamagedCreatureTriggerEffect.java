package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker for a trigger that expands into an effect applied to the creature just dealt damage by the
 * source creature.
 */
public interface DamagedCreatureTriggerEffect extends CardEffect {

    CardEffect triggeredEffect();

    default PermanentPredicate damagedCreatureFilter() {
        return null;
    }

    default boolean equipmentScoped() {
        return false;
    }

    default boolean combatDamageOnly() {
        return false;
    }
}
