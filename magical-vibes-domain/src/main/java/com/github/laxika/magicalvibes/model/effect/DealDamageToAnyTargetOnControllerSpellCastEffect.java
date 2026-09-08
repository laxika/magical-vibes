package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Emblem marker for a fixed amount of damage to any target whenever its controller casts a
 * matching spell, optionally followed by additional effects on the same triggered ability.
 */
public record DealDamageToAnyTargetOnControllerSpellCastEffect(
        int damage, CardPredicate spellFilter, List<CardEffect> additionalEffects) implements CardEffect {

    public DealDamageToAnyTargetOnControllerSpellCastEffect {
        additionalEffects = List.copyOf(additionalEffects);
    }

    public DealDamageToAnyTargetOnControllerSpellCastEffect(int damage) {
        this(damage, null, List.of());
    }

    public DealDamageToAnyTargetOnControllerSpellCastEffect(int damage, List<CardEffect> additionalEffects) {
        this(damage, null, additionalEffects);
    }
}
