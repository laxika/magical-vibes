package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Perpetually gives the source card the supported keywords of the creature that caused its trigger. */
public record PerpetuallyGainKeywordsOfTriggeringCreatureEffect(Set<Keyword> keywordsAtTrigger)
        implements CardEffect {

    public static final Set<Keyword> SUPPORTED_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HASTE,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    public PerpetuallyGainKeywordsOfTriggeringCreatureEffect() {
        this(Set.of());
    }

    public PerpetuallyGainKeywordsOfTriggeringCreatureEffect {
        keywordsAtTrigger = keywordsAtTrigger == null ? Set.of() : Set.copyOf(keywordsAtTrigger);
    }
}
