package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Seeks a matching card into hand and binds that exact card to exile at the next end step. */
public record SeekLibraryToHandAndRegisterExileAtNextEndStepEffect(CardPredicate filter)
        implements CardEffect {
}
