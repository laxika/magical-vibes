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
        CardEffect wrapped,
        boolean gatesSource
) implements CardEffect, BlockedCreatureTriggerEffect {

    /**
     * Gate for an ordinary "whenever [matching creature] enters" wording. In a self-or-ally slot
     * ("this creature or another [type]") the source's own entry is not subject to the gate.
     */
    public TriggeringCardConditionalEffect(CardPredicate predicate, CardEffect wrapped) {
        this(predicate, wrapped, false);
    }

    /**
     * Gate that also applies to the source's own entry in a self-or-ally slot, for wordings such as
     * "whenever a Dragon you control enters" where the source only counts if it matches itself.
     */
    public static TriggeringCardConditionalEffect gatingSource(CardPredicate predicate, CardEffect wrapped) {
        return new TriggeringCardConditionalEffect(predicate, wrapped, true);
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return wrapped.hasAbilityResolutionCondition();
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
