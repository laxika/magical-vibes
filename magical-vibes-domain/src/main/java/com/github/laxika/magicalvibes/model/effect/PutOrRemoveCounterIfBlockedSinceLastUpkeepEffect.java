package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Put a counter of the given type on this permanent if it has blocked or been blocked since your
 * last upkeep. Otherwise, remove one from it." (Wiitigo.)
 * <p>
 * The "since your last upkeep" window is tracked by {@code Permanent.blockedOrWasBlockedSinceLastUpkeep},
 * set whenever the creature is declared as a blocker or is blocked, and cleared by this effect when
 * it resolves — so each upkeep sees only the blocks that happened since the previous one.
 */
public record PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect(CounterType counterType) implements CardEffect {
}
