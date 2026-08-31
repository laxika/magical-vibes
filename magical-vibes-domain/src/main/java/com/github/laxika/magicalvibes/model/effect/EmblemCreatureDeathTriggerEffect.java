package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Marker stored on an emblem for an ability that triggers whenever a creature dies.
 * The trigger collector puts the payload effects on the stack and uses the filter when choosing
 * any targets for that trigger.
 */
public record EmblemCreatureDeathTriggerEffect(List<CardEffect> effects, TargetFilter targetFilter)
        implements CardEffect {

    public EmblemCreatureDeathTriggerEffect {
        effects = List.copyOf(effects);
    }
}
