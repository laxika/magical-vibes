package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a creature-death trigger that fires once for a simultaneous event containing one or
 * more matching creatures.
 */
public interface BatchedCreatureDeathTriggerEffect extends CardEffect {

    CardEffect wrapped();
}
