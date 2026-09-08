package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * Trigger-only wrapper that fires its payload only when the activated ability is marked as boast.
 */
public record TriggeringBoastAbilityConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public CardEffect resolveForActivatedAbility(ActivatedAbility ability) {
        return ability != null && ability.isBoast() ? wrapped : null;
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
