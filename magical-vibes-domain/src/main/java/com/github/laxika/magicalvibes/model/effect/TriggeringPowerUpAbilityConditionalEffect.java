package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/** Trigger-only wrapper that fires its payload when a Power-up ability is activated. */
public record TriggeringPowerUpAbilityConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public CardEffect resolveForActivatedAbility(ActivatedAbility ability) {
        return ability != null && ability.isPowerUpAbility() ? wrapped : null;
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
