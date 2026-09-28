package com.github.laxika.magicalvibes.model.effect;

/** Adds additional combat/main phase pairs, optionally with an effect triggered at each added combat's beginning. */
public record AdditionalCombatMainPhaseEffect(int count, CardEffect additionalCombatBeginningEffect,
                                             boolean onlyDuringMainPhase,
                                             boolean onlyTargetCreaturesCanAttack)
        implements CardEffect {

    public AdditionalCombatMainPhaseEffect(int count, CardEffect additionalCombatBeginningEffect) {
        this(count, additionalCombatBeginningEffect, false, false);
    }

    public AdditionalCombatMainPhaseEffect(int count, CardEffect additionalCombatBeginningEffect,
                                           boolean onlyDuringMainPhase) {
        this(count, additionalCombatBeginningEffect, onlyDuringMainPhase, false);
    }

    public AdditionalCombatMainPhaseEffect(int count) {
        this(count, null, false, false);
    }
}
