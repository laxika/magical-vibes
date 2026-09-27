package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Seeks a matching card into hand and registers that exact card for discard at the next end step. */
public record SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(CardPredicate filter)
        implements CardEffect {
}
