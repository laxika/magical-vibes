package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller chooses and pays generic X, then the damaged player reveals X
 * cards of their choice from hand and the controller chooses one of those cards for that player to
 * discard. Choosing X=0 means the controller declines.
 */
public record PayXManaRevealCardsChooseOneToDiscardEffect()
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
