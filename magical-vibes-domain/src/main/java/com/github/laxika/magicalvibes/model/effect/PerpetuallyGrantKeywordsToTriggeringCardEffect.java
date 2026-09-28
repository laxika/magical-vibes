package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Perpetually grants keywords to the card that caused the surrounding trigger. */
public record PerpetuallyGrantKeywordsToTriggeringCardEffect(Set<Keyword> keywords)
        implements CardEffect {

    public PerpetuallyGrantKeywordsToTriggeringCardEffect {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword is required");
        }
        keywords = Set.copyOf(keywords);
    }
}
