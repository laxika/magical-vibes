package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;

/**
 * What a triggered ability triggers on: a game event and the filters it must match, or a game
 * state for a state trigger (CR 603.8). Triggers are pure data, like predicates: the engine, not
 * the trigger, decides whether something that happens in the game matches it.
 */
public sealed interface Trigger permits LegacySlotTrigger {

    /**
     * The zones a trigger condition on this trigger functions from when the card doesn't say
     * otherwise: the battlefield (CR 113.6). A trigger that can't trigger from the battlefield
     * returns the zones it can trigger from instead (CR 113.6k).
     */
    default AbilityZones defaultZones() {
        return AbilityZones.BATTLEFIELD;
    }
}
