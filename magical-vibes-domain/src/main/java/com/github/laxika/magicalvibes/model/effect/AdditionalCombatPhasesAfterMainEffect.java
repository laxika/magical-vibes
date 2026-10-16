package com.github.laxika.magicalvibes.model.effect;

/**
 * Inserts additional combat phases immediately after the resolving main phase, with no main phase
 * between them. The turn engine returns to the phase that would normally follow that main phase
 * after the inserted combats.
 *
 * @param count                         how many additional combat phases are inserted
 * @param onlyTargetCreaturesCanAttack  whether only the resolving spell's chosen creatures can attack
 *                                      during each inserted combat (Last Night Together)
 */
public record AdditionalCombatPhasesAfterMainEffect(int count, boolean onlyTargetCreaturesCanAttack)
        implements CardEffect {

    public AdditionalCombatPhasesAfterMainEffect(int count) {
        this(count, false);
    }
}
