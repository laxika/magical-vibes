package com.github.laxika.magicalvibes.model.effect;

/** Marker stored in an emblem for an entering creature to deal damage equal to its power to any target. */
public interface DealDamageToAnyTargetOnAllyCreatureEntersEffect extends CardEffect {

    record Marker() implements DealDamageToAnyTargetOnAllyCreatureEntersEffect {
    }
}
