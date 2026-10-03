package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;

/** Resolves a scheduled permanent zone change after its delayed ability uses the stack. */
public record ResolveDelayedPermanentActionEffect(DelayedPermanentAction action) implements CardEffect {
}
