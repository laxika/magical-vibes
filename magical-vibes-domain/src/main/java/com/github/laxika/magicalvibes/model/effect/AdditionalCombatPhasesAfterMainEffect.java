package com.github.laxika.magicalvibes.model.effect;

/**
 * Inserts additional combat phases immediately after the resolving main phase, with no main phase
 * between them. The turn engine returns to the phase that would normally follow that main phase
 * after the inserted combats.
 *
 * @param count                         how many additional combat phases are inserted
 * @param onlyTargetCreaturesCanAttack  whether only the resolving spell's chosen creatures can attack
 *                                      during each inserted combat (Last Night Together)
 * @param additionalCombatBeginningEffect optional effect for each inserted combat: a delayed triggered ability at its
 *                                      beginning (Moraug, Fury of Akoum's "untap all creatures you control"), or a
 *                                      restriction on who can attack in it, which applies during it
 */
public record AdditionalCombatPhasesAfterMainEffect(int count, boolean onlyTargetCreaturesCanAttack,
                                                    CardEffect additionalCombatBeginningEffect)
        implements CardEffect {

    public AdditionalCombatPhasesAfterMainEffect(int count, boolean onlyTargetCreaturesCanAttack) {
        this(count, onlyTargetCreaturesCanAttack, null);
    }

    public AdditionalCombatPhasesAfterMainEffect(int count, CardEffect additionalCombatBeginningEffect) {
        this(count, false, additionalCombatBeginningEffect);
    }

    public AdditionalCombatPhasesAfterMainEffect(int count) {
        this(count, false, null);
    }
}
