package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds additional combat/main phase pairs, optionally with an effect triggered at each added combat's beginning.
 * When {@code afterSecondMainPhase} is set the pairs are added directly after the turn's second main phase
 * (CR 500.8) even if the spell was cast in the first main phase, and do nothing when cast after that point.
 */
public record AdditionalCombatMainPhaseEffect(int count, CardEffect additionalCombatBeginningEffect,
                                             boolean onlyDuringMainPhase,
                                             boolean onlyTargetCreaturesCanAttack,
                                             boolean afterSecondMainPhase)
        implements CardEffect {

    public AdditionalCombatMainPhaseEffect(int count, CardEffect additionalCombatBeginningEffect,
                                           boolean onlyDuringMainPhase, boolean onlyTargetCreaturesCanAttack) {
        this(count, additionalCombatBeginningEffect, onlyDuringMainPhase, onlyTargetCreaturesCanAttack, false);
    }

    public AdditionalCombatMainPhaseEffect(int count, CardEffect additionalCombatBeginningEffect) {
        this(count, additionalCombatBeginningEffect, false, false, false);
    }

    public AdditionalCombatMainPhaseEffect(int count, CardEffect additionalCombatBeginningEffect,
                                           boolean onlyDuringMainPhase) {
        this(count, additionalCombatBeginningEffect, onlyDuringMainPhase, false, false);
    }

    public AdditionalCombatMainPhaseEffect(int count) {
        this(count, null, false, false, false);
    }
}
