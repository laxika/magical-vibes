package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A delayed trigger watching one specific creature permanent die with a counter. It has no stated
 * duration, so it triggers once, at the next death of that permanent (CR 603.7b), and is removed
 * when that death event is processed.
 */
public record TargetedCreatureDeathTriggerWatcher(
        UUID watchedPermanentId,
        CounterType counterType,
        UUID controllerId,
        Card sourceCard,
        CardEffect effect
) {
}
