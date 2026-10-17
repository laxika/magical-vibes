package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;

/**
 * Matches permanents that currently have an activated ability whose effects (costs included) are exactly
 * the given list, such as the "Sacrifice this creature: Add {C}." ability of an Eldrazi Scion.
 */
public record PermanentHasActivatedAbilityWithEffectsPredicate(List<CardEffect> effects)
        implements PermanentPredicate {

    public PermanentHasActivatedAbilityWithEffectsPredicate {
        effects = List.copyOf(effects);
    }
}
