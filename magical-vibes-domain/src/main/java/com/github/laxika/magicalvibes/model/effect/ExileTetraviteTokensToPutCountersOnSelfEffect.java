package com.github.laxika.magicalvibes.model.effect;

/**
 * Tetravus, second upkeep trigger: "you may exile any number of tokens created with this creature.
 * If you do, put that many +1/+1 counters on this creature."
 *
 * <p>Resolves by prompting the source's controller (via a
 * {@link com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.ExileTetraviteTokensPutCountersOnSource}
 * multi-permanent choice) to pick any number of the tokens this permanent created and still on the
 * battlefield (tracked in {@code GameData.tetravusCreatedTokens}); the chosen tokens are exiled and
 * that many +1/+1 counters are put on the source. Selecting none is the "you may … decline" case.
 * Paired with {@link RemoveCountersToCreateTetraviteTokensEffect}.
 */
public record ExileTetraviteTokensToPutCountersOnSelfEffect() implements CardEffect {
}
