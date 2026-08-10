package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a trigger that expands into an effect applied to the creature just dealt damage by the
 * source creature.
 */
public interface DamagedCreatureTriggerEffect extends CardEffect {

    CardEffect triggeredEffect();

    default boolean equipmentScoped() {
        return false;
    }
}
