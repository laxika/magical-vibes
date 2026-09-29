package com.github.laxika.magicalvibes.model.effect;

/**
 * If the creature that caused the enter trigger was cast, exiles it tracked with the source
 * permanent and immediately returns every other card exiled with that source under its owner's
 * control. The triggering creature remains exiled until the source leaves the battlefield.
 */
public record ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect()
        implements CardEffect {
}
