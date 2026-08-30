package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Static self-effect: the source gains each configured keyword found on a creature card exiled
 * with it, optionally carrying over fixed protection abilities represented by
 * {@link ProtectionGrantingEffect} as granted effects as well.
 */
public record GainKeywordsOfCreatureCardsExiledWithSourceEffect(
        Set<Keyword> watchedKeywords,
        boolean copyProtectionEffects
) implements CardEffect {

    private static final Set<Keyword> DEATH_MASK_DUPLICANT_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FEAR,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.HASTE,
            Keyword.FORESTWALK,
            Keyword.MOUNTAINWALK,
            Keyword.ISLANDWALK,
            Keyword.SWAMPWALK,
            Keyword.PLAINSWALK,
            Keyword.TRAMPLE
    );

    public GainKeywordsOfCreatureCardsExiledWithSourceEffect() {
        this(DEATH_MASK_DUPLICANT_KEYWORDS, true);
    }

    public GainKeywordsOfCreatureCardsExiledWithSourceEffect(
            Set<Keyword> watchedKeywords,
            boolean copyProtectionEffects
    ) {
        this.watchedKeywords = Set.copyOf(watchedKeywords);
        this.copyProtectionEffects = copyProtectionEffects;
    }
}
