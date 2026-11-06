package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the counters a leaving creature had onto the permanent carrying the trigger. The leaving creature is the
 * stack entry's triggering permanent, as it last existed on the battlefield (CR 603.10a).
 */
public record PutLeavingCreatureCountersOnSourceEffect() implements CardEffect {
}
