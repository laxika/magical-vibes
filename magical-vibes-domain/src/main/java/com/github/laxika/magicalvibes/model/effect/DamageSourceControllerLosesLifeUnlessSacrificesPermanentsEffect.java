package com.github.laxika.magicalvibes.model.effect;

/**
 * Archfiend of Spite's damage trigger: that source's controller loses that much life unless they sacrifice that many
 * permanents. "That much" is the damage dealt, the stack entry's event value; the source's controller is the controller
 * of the entry's triggering permanent as the ability resolves, or its last known controller (CR 608.2h), or the
 * controller the entry recorded for a source that was never a permanent.
 */
public record DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect() implements CardEffect {
}
