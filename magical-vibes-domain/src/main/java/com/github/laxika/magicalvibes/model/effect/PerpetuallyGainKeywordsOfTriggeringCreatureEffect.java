package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Perpetually gives the source card the supported keywords the triggering creature has as the ability resolves, or had
 * as it last existed on the battlefield (the stack entry's triggering permanent).
 */
public record PerpetuallyGainKeywordsOfTriggeringCreatureEffect() implements CardEffect {

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
}
