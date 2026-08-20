package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * The target player reveals their hand and this effect deals damage to that player equal to the
 * number of revealed cards of the chosen color. A non-null fixed color skips the choice.
 */
public record DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect(CardColor fixedColor)
        implements CombatDamageTriggerContextEffect {

    public DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
