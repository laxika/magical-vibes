package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Prompts the controller for a creature type and creates an emblem whose static effect applies to
 * creatures of that type.
 */
public record CreateEmblemWithChosenSubtypeEffect(
        int powerBoost,
        int toughnessBoost,
        Set<Keyword> grantedKeywords,
        String reminderText
) implements CardEffect {

    public CreateEmblemWithChosenSubtypeEffect {
        grantedKeywords = Set.copyOf(grantedKeywords);
    }
}
