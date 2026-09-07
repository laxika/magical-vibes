package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * Trigger-only wrapper that fires its payload only for activated abilities whose costs tap their
 * source permanent.
 */
public record TriggeringTapAbilityConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public CardEffect resolveForActivatedAbility(ActivatedAbility ability) {
        return ability != null && ability.isRequiresTap() ? wrapped : null;
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
