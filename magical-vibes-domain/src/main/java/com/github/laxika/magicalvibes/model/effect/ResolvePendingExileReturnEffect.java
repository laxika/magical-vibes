package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.action.PendingExileReturn;

/** Resolves a scheduled exile return after players can respond to its delayed ability. */
public record ResolvePendingExileReturnEffect(PendingExileReturn pending, long expectedExileEntryVersion)
        implements CardEffect {

    public ResolvePendingExileReturnEffect(PendingExileReturn pending) {
        this(pending, -1);
    }
}
