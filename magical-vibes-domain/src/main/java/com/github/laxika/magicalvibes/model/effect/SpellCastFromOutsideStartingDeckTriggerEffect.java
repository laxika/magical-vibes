package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Trigger descriptor for "whenever you cast a spell that isn't from your starting deck". */
public record SpellCastFromOutsideStartingDeckTriggerEffect(List<CardEffect> resolvedEffects)
        implements CardEffect {

    public SpellCastFromOutsideStartingDeckTriggerEffect {
        resolvedEffects = List.copyOf(resolvedEffects);
    }
}
