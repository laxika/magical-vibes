package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for the first spell with the source permanent's chosen name cast by its
 * controller each turn.
 */
public record ChosenNameSpellCastTriggerEffect(List<CardEffect> resolvedEffects) implements CardEffect {

    public ChosenNameSpellCastTriggerEffect {
        resolvedEffects = List.copyOf(resolvedEffects);
    }
}
