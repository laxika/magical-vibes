package com.github.laxika.magicalvibes.model.effect;

/** Static effect: the source controller can't cast spells after attacking with creatures this turn. */
public record ControllerCantCastSpellsIfAttackedThisTurnEffect() implements ControllerTurnRestrictionEffect {

    @Override
    public boolean preventsCastingAfterAttack() {
        return true;
    }
}
