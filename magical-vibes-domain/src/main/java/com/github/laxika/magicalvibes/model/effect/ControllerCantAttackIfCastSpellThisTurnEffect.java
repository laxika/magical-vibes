package com.github.laxika.magicalvibes.model.effect;

/** Static effect: the source controller can't attack with creatures after casting a spell this turn. */
public record ControllerCantAttackIfCastSpellThisTurnEffect() implements ControllerTurnRestrictionEffect {

    @Override
    public boolean preventsAttackingAfterSpellCast() {
        return true;
    }
}
