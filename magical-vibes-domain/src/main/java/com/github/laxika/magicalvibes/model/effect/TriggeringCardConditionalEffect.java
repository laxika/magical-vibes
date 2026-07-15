package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Conditional wrapper: the wrapped effect only fires if the card that caused
 * the trigger matches {@code predicate}.
 */
public record TriggeringCardConditionalEffect(
        CardPredicate predicate,
        CardEffect wrapped
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
