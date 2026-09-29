package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Seeks matching cards into hand and registers those exact cards for discard at the next end step. */
public record SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(
        DynamicAmount count, CardPredicate filter)
        implements CardEffect {

    public SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(CardPredicate filter) {
        this(new Fixed(1), filter);
    }

    public SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect {
        Objects.requireNonNull(count, "count");
    }
}
