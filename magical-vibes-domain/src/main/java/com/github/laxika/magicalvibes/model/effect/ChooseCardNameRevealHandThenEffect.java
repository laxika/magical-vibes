package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * The controller chooses a card name, then the damaged player reveals their hand. If a card with
 * the chosen name is revealed, {@code followUpEffect} is inserted into the resolving ability.
 */
public record ChooseCardNameRevealHandThenEffect(CardEffect followUpEffect)
        implements CombatDamageTriggerContextEffect {

    public ChooseCardNameRevealHandThenEffect {
        Objects.requireNonNull(followUpEffect, "followUpEffect");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
