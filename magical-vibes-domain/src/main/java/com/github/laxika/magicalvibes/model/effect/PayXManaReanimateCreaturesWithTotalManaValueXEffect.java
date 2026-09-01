package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller may pay {X}. When they do, a reflexive triggered ability is put
 * on the stack to return any number of target creature cards from the damaged player's graveyard
 * with total mana value X or less under the controller's control.
 */
public record PayXManaReanimateCreaturesWithTotalManaValueXEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
