package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a color, then the target player reveals their hand and this effect deals
 * damage to that player equal to the number of revealed cards of the chosen color.
 */
public record DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
