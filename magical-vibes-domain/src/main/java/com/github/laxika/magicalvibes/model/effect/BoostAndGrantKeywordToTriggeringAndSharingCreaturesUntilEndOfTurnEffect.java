package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Gives the triggering permanent and other creatures controlled by the ability controller that
 * share a creature type with it a temporary power/toughness boost and keyword(s).
 */
public record BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect(
        int powerBoost,
        int toughnessBoost,
        Set<Keyword> keywords
) implements CardEffect {

    public BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect {
        keywords = Set.copyOf(keywords);
    }
}
