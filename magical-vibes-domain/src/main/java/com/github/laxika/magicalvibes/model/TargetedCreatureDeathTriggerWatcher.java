package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** A turn-scoped delayed trigger watching one specific creature permanent die with a counter. */
public record TargetedCreatureDeathTriggerWatcher(
        UUID watchedPermanentId,
        CounterType counterType,
        UUID controllerId,
        Card sourceCard,
        CardEffect effect
) {
}
