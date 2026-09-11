package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/** Trigger-only wrapper that fires its payload when a ninjutsu ability is activated. */
public record TriggeringNinjutsuAbilityConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public CardEffect resolveForActivatedAbility(ActivatedAbility ability) {
        return ability != null && ability.isNinjutsuAbility() ? wrapped : null;
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
