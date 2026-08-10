package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose color or mana value matches the card imprinted on the evaluating source
 * permanent. The source permanent is required by the engine-side evaluator.
 */
public record StackEntrySharesColorOrManaValueWithImprintedCardPredicate()
        implements StackEntryPredicate {
}
