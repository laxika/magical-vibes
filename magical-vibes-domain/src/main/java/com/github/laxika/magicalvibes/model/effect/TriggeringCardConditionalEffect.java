package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Conditional wrapper: the wrapped effect only fires if the card that caused
 * the trigger matches {@code predicate}. Trigger collectors may evaluate this at trigger time;
 * when it is part of an already-collected stack entry, normal resolution evaluates it using
 * the entry's {@code triggeringCardId}.
 */
public record TriggeringCardConditionalEffect(
        CardPredicate predicate,
        CardEffect wrapped
) implements CardEffect, BlockedCreatureTriggerEffect {

    @Override
    public boolean hasAbilityResolutionCondition() {
        return wrapped.hasAbilityResolutionCondition();
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
