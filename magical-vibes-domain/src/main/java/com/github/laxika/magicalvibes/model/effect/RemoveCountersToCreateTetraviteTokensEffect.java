package com.github.laxika.magicalvibes.model.effect;

/**
 * Tetravus, first upkeep trigger: "you may remove any number of +1/+1 counters from this creature.
 * If you do, create that many 1/1 colorless Tetravite artifact creature tokens. They each have
 * flying and 'This token can't be enchanted.'"
 *
 * <p>Resolves by prompting the source's controller for how many of its +1/+1 counters (0..the count
 * present) to remove via a {@link com.github.laxika.magicalvibes.model.ChoiceContext.TetravusCounterRemoval}
 * number choice; on the answer that many counters are removed and that many tokens are created from
 * {@code tokenTemplate}, each recorded in {@code GameData.tetravusCreatedTokens} as "created with"
 * the source so the paired {@link ExileTetraviteTokensToPutCountersOnSelfEffect} can shuttle them
 * back. Choosing 0 is the "you may … decline" case, so no {@code MayEffect} wrapper is needed.
 *
 * @param tokenTemplate blueprint for the Tetravite token (its count is supplied at creation time)
 */
public record RemoveCountersToCreateTetraviteTokensEffect(CreateTokenEffect tokenTemplate) implements CardEffect {
}
