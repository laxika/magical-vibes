package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * "At the beginning of each of that player's upkeeps, if that card is exiled, remove a delay
 * counter from it. If the card has no delay counters on it, the player puts it onto the stack as a
 * copy of the original spell." (Ertai's Meddling)
 *
 * <p>The effect of the delayed triggered ability {@link ExileTargetSpellWithDelayCountersEffect} creates. Putting the
 * card back onto the stack is not casting it, so cast triggers do not fire.</p>
 *
 * @param cardId        the exiled card carrying the delay counters
 * @param originalEntry a snapshot of the stack entry the spell had, put back onto the stack when the last counter is
 *                      removed so the spell keeps its X value, targets and costs paid (CR 707.10); never changed
 */
public record RemoveDelayCounterFromExiledSpellEffect(UUID cardId, StackEntry originalEntry) implements CardEffect {
}
